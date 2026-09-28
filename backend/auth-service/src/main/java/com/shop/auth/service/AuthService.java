package com.shop.auth.service;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.UserResponse;
import com.shop.auth.entity.Role;
import com.shop.auth.entity.User;
import com.shop.auth.exception.EmailAlreadyExistsException;
import com.shop.auth.exception.InvalidPasswordException;
import com.shop.auth.exception.UserNotFoundException;
import com.shop.auth.repository.UserRepository;
import com.shop.auth.security.JwtTokenProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.postgresql.util.PSQLException;

import java.util.Locale;

/**
 * Сервис для аутентификации и регистрации пользователей.
 * <p>
 * Содержит бизнес-логику:
 * - регистрация нового пользователя
 * - вход (логин) с выдачей JWT-токена
 */
@Service
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param userRepository репозиторий для работы с пользователями
     * @param passwordEncoder кодировщик паролей (BCrypt)
     * @param jwtTokenProvider провайдер для работы с JWT
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request DTO с данными для регистрации (email, password, firstName, lastName)
     * @return DTO с данными созданного пользователя (без пароля)
     * @throws EmailAlreadyExistsException если пользователь с таким email уже существует
     */
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        // 1. Проверяем, не занят ли email
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Создаём нового пользователя
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // хешируем пароль
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(Role.USER); // новым пользователям устанавливаем роль USER

        // 3. Пытаемся сохранить в БД. В случае если отработал unique-индекс (если другой параллельный запрос уже вставил этот email), то кидаем ошибку.
        try {
            User savedUser = userRepository.save(user);

            // 4. Формируем и возвращаем ответ (без пароля)
            return mapToUserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            // Если нарушение уникальности (а поле для уникальности только email), то кидаем ошибку о существовании такого email
            if (isEmailUniqueViolation(e)) throw new EmailAlreadyExistsException(request.getEmail());
            // Иначе прокидываем ошибку далее
            throw e;
        }
    }

    /**
     * Проверяет, нарушена ли уникальность email.
     * <p>
     * Тип ошибки определяется не по тексту сообщения, а по стандартному коду SQLState 23505
     * (unique_violation) у корневой PSQLException. Так как у users единственное unique-поле — email,
     * любой 23505 при сохранении означает, что email уже занят.
     *
     * @param e перехваченная ошибка целостности данных
     * @return true, если нарушена уникальность email
     */
    private boolean isEmailUniqueViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause(); // Проваливается по цепочке getCause() до самого глубокого исключения, т.е. до PSQLException. Там Postgres кладет стандартизированный код ошибки
        // Если cause является PSQLException, он неявно приводится к типу и связывается с переменной psql - ее можно использовать далее без ручного приведения
        // Итого получается: если cause является PSQLException, тогда он доступен как psql, и код состояния SQL у него равен "23505", верни true
        return cause instanceof PSQLException psql && "23505".equals(psql.getSQLState());
    }

    /**
     * Аутентификация пользователя (логин).
     *
     * @param request DTO с email и паролем
     * @return DTO с JWT-токеном и данными пользователя
     * @throws RuntimeException если email не найден или пароль неверный
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Ищем пользователя по email
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        // 2. Проверяем пароль
        // matches - берет пароль введенный пользователем и генерирует хеш на основе введенного пароля и соли из хеша, хранящегося в БД для данного пользователя.
        // (берет хеш в БД, вычленяет соль и на основе данной соли генерит хеш с введенным паролем. Сверяет и если они равны - true)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        // 3. Генерируем JWT-токен
        String token = jwtTokenProvider.generateToken(user.getId());

        // 4. Формируем и возвращаем ответ
        return mapToAuthResponse(token, user);
    }

    /**
     * Преобразует сущность User в UserResponse DTO.
     *
     * @param user сущность пользователя
     * @return DTO для ответа клиенту
     */
    public UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        return response;
    }

    /**
     * Преобразует сущность User и токен в AuthResponse DTO.
     *
     * @param token JWT-токен
     * @param user сущность пользователя
     * @return DTO для ответа клиенту
     */
    private AuthResponse mapToAuthResponse(String token, User user) {
        AuthResponse response = mapToAuthResponse(user);
        response.setToken(token);
        return response;
    }

    /**
     * Преобразует сущность User в AuthResponse DTO.
     *
     * @param user сущность пользователя
     * @return DTO для ответа клиенту
     */
    public AuthResponse mapToAuthResponse(User user) {
        AuthResponse response = new AuthResponse();
        response.setType(AuthResponse.BEARER);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        return response;
    }

    /**
     * Приводит email к нижнему регистру (Locale.ROOT) и убирает пробелы по краям.
     * <p>
     * Обеспечивает единообразное хранение и поиск пользователей по email,
     * не зависящее от регистра и лишних пробелов, при вводе адреса при регистрации или входе.
     *
     * @param email email пользователя
     * @return email в нижнем регистре без пробелов по краям
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}