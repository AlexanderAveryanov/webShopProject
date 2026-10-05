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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для {@link AuthService}.
 * <p>
 * Бизнес-логика проверяется в изоляции: все внешние зависимости (репозиторий,
 * кодировщик паролей, провайдер JWT) подменяются моками Mockito, реальная БД
 * и Spring-контекст не поднимаются. Так тесты быстрые и не зависят от окружения.
 * <p>
 * {@link MockitoExtension} сам инициализирует {@code @Mock}-поля и внедряет их
 * в тестируемый объект, помеченный {@code @InjectMocks}.
 * Тест может быть выполнен, не выполнен, с ошибкой (если не смог выполниться из-за неожиданного исключения)
 */
// @ExtendWith(MockitoExtension.class) - входная точка для Mockito в JUnit 6. Без неё @Mock и @InjectMocks не будут работать.
// Что делает:
//  - Перед каждым тестом сканирует класс на поля с @Mock и создает для них загрушки.
//  - Находит поле с @InjectMocks и внедряет в него созданные моки.
//  - После каждого теста автоматически сбрасывает состояние моков (не нужно руками вызывать Mockito.reset())
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    /** Мок репозитория пользователей */
    @Mock // Создает фейковых объект (заглушку). Все методы этого объекта по умолчанию возвращают: null/0/false/пустую коллекцию
    private UserRepository userRepository;

    /** Мок кодировщика паролей (BCrypt) */
    @Mock
    private PasswordEncoder passwordEncoder;

    /** Мок провайдера JWT, чтобы не генерировать настоящий токен. */
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    /** Тестируемый сервис; зависимости-моки внедряются Mockito автоматически. */
    @InjectMocks // Создает экземпляр и передает созданные моки из класса теста (объекты с аннотацией @Mock)
    private AuthService authService;

    /**
     * Проверка успешной регистрации пользователя.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>email нормализуется перед проверкой и сохранением;</li>
     *   <li>возврат UserResponse при регистрации, пароль хешируется, роль - USER, пароль отсутствует</li>
     *   <li>корректность отправленных данных в БД для сохранения</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> пользователь создан, возвращен UserResponse с корректными полями.
     */
    // Наименование методов принято: метод_ожидаемоеПоведение_условие
    @Test // помечает метод как тестовый. JUnit 6 запустит его при прогоне
    void register_shouldCreateUserAndReturnUserResponse() {
        // Подготовка данных
        // Создаем объект request и наполняем его данными. Эмулируем, что к нам пришло.
        RegisterRequest request = new RegisterRequest();
        request.setEmail(" Test@Example.COM "); // email намеренно с пробелами и в разном регистре — проверяем нормализацию
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        // В БД email ещё не занят (сервис проверяет его уже в нормализованном виде)
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false); // когда кто-то вызовет existsByEmail... верни false
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Создание объекта User и наполнение его тестовыми данными, чтобы проверить правильность возвращаемого UserResponse
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setRole(Role.USER);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser); // any(User.class) - любой объект типа User

        // Действие: вызываем тестируемые действия
        UserResponse response = authService.register(request);

        // Проверка правильности возвращаемого UserResponse при регистрации: ответ содержит данные пользователя без пароля
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getRole()).isEqualTo(Role.USER);

        // Дополнительная проверка результата: перехватываем объект, реально ПЕРЕДАННЫЙ в save(), и проверяем, что сервис сохранил нормализованный email и хеш пароля
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User captured = captor.getValue();
        assertThat(captured.getEmail()).isEqualTo("test@example.com");
        assertThat(captured.getPassword()).isEqualTo("encodedPassword");
        assertThat(captured.getFirstName()).isEqualTo("John");
        assertThat(captured.getLastName()).isEqualTo("Doe");
        assertThat(captured.getRole()).isEqualTo(Role.USER);
    }

    /**
     * Проверка регистрации с уже существующим email.
     * <p>
     * <b>Что проверяем:</b>
     * <ul>
     *   <li>сервис выбрасывает {@link EmailAlreadyExistsException};</li>
     *   <li>сервис не вызывает save() — выходит раньше, чем доходит до сохранения.</li>
     * </ul>
     * <p>
     * <b>Ожидаемый результат:</b> исключение выброшено, пользователь не сохранен.
     */
    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Предварительная проверка показала, что email занят
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Проверяем, что при вызове метода регистрации будет брошено исключение типа EmailAlreadyExistsException
        assertThatThrownBy(() -> authService.register(request)) // assertThatThrownBy принимает лямбду (передаваемый метод) и если лямбда выбросила исключение, то оно ловится и оборачивается в объект ThrowableAssert
                // Далее у этого объекта мы проверяем тип ошибки.
                // Если лямбда не выбросила исключение, то assertThatThrownBy падает с AssertionError "Ожидалось, что код выбросит исключение, но он не выбросил". Тест заканчивается с ошибкой.
                .isInstanceOf(EmailAlreadyExistsException.class); // Если тип совпадает, то тест проходит, иначе нет. Учитываются совместимости по типу.

        // save() не должен вызываться вовсе
        // verify - метод Mockito, говорит "Проверь, что было вот такое взаимодействие с моком"
        // never() - режим верификации, говорит, что ожидаемое кол-во вызовов 0 раз
        // .save() - метод который проверяется. Mockito перехватывает все вызовы этого метода через прокси и записывает их в журнал
        // Итого создается журнал вызовов репозитория (мока) и здесь проверяется были ли такие вызовы, и если да, то бросается исключение NeverWantedButInvoked подкласс AssertionError - тест заканчивается с ошибкой.
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Гонка при регистрации: email прошёл проверку, но уникальный индекс БД
     * отклонил вставку (SQLState 23505). Сервис должен распознать это и
     * превратить в {@link EmailAlreadyExistsException}.
     */
    @Test
    // throws SQLException - объявление метода. Говорит компилятору, что метод может выбросить данное исключение и вызывающий должен быть готов к этому. Если может быть несколько разных исключений, они перечисляются через запятую
    void register_shouldThrowOnUniqueViolation23505() throws SQLException {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        // Имитируем ошибку уникальности PostgreSQL: причина завернута в DataIntegrityViolationException
        PSQLException psql = new PSQLException("duplicate key", org.postgresql.util.PSQLState.UNIQUE_VIOLATION);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint", psql);

        when(userRepository.save(any(User.class))).thenThrow(ex);

        // Сервис определяет тип ошибки по SQLState корневой PSQLException
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    /**
     * Прочая ошибка целостности (например, нарушение внешнего ключа, SQLState 23503)
     * не связана с email — она должна пробрасываться дальше без подмены.
     */
    @Test
    void register_shouldRethrowOtherDataIntegrityViolation() throws SQLException {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        PSQLException psql = new PSQLException("other", org.postgresql.util.PSQLState.FOREIGN_KEY_VIOLATION);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint", psql);

        when(userRepository.save(any(User.class))).thenThrow(ex);

        // Ошибка не про уникальность email — ожидаем исходный тип исключения
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Успешный вход: сервис находит пользователя, проверяет пароль и
     * возвращает {@link AuthResponse} с JWT-токеном и данными пользователя.
     */
    @Test
    void login_shouldReturnAuthResponseWithToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail(" Test@Example.COM ");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.USER);

        // Поиск идёт по нормализованному email; пароль совпадает; токен генерируется
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo(AuthResponse.BEARER);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }

    /** Вход по несуществующему email: ожидаем {@link UserNotFoundException}. */
    @Test
    void login_shouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    /** Вход с неверным паролем: ожидаем {@link InvalidPasswordException}. */
    @Test
    void login_shouldThrowWhenPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    /** Публичный маппер сущности в {@link UserResponse}: переносятся все поля, кроме пароля. */
    @Test
    void mapToUserResponse_shouldMapFields() {
        User user = new User();
        user.setId(5L);
        user.setEmail("map@example.com");
        user.setFirstName("A");
        user.setLastName("B");
        user.setRole(Role.ADMIN);

        UserResponse response = authService.mapToUserResponse(user);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getEmail()).isEqualTo("map@example.com");
        assertThat(response.getFirstName()).isEqualTo("A");
        assertThat(response.getLastName()).isEqualTo("B");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    /** Маппер в {@link AuthResponse} без токена: токен не задан, тип — Bearer. */
    @Test
    void mapToAuthResponse_shouldMapFieldsWithoutToken() {
        User user = new User();
        user.setId(2L);
        user.setEmail("auth@example.com");
        user.setFirstName("F");
        user.setLastName("L");
        user.setRole(Role.USER);

        AuthResponse response = authService.mapToAuthResponse(user);

        assertThat(response.getType()).isEqualTo(AuthResponse.BEARER);
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getEmail()).isEqualTo("auth@example.com");
        assertThat(response.getFirstName()).isEqualTo("F");
        assertThat(response.getLastName()).isEqualTo("L");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        // Токен здесь не устанавливается — проверяем, что он остался пустым
        assertThat(response.getToken()).isNull();
    }
}
