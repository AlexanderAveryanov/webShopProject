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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @throws RuntimeException если пользователь с таким email уже существует
     */
    public UserResponse register(RegisterRequest request) {
        // 1. Проверяем, не занят ли email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email уже используется: " + request.getEmail());
        }

        // 2. Создаём нового пользователя
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // хешируем пароль
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(Role.USER); // новым пользователям устанавливаем роль USER

        // 3. Сохраняем в базу данных
        User savedUser = userRepository.save(user);

        // 4. Формируем и возвращаем ответ (без пароля)
        return mapToUserResponse(savedUser);
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
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с Email: " + request.getEmail()));

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
    private UserResponse mapToUserResponse(User user) {
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
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setType(AuthResponse.BEARER);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        return response;
    }
}