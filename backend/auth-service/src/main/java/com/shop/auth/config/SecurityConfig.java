package com.shop.auth.config;

import com.shop.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация безопасности Spring Security.
 * <p>
 * Определяет:
 * - какие URL доступны без аутентификации
 * - какие URL требуют аутентификации
 * - настройки CORS (кросс-доменные запросы)
 * - отключение CSRF (для REST API)
 * - стратегию работы с сессиями (stateless)
 * - кодировщик паролей (BCrypt)
 */
@Configuration // Указывает, что класс содержит настройки (бины) для Spring-контейнера
// Активирует механизмы безопасности Spring Security: фильтры, аутентификацию, авторизацию
// Под капотом регистрирует цепочку фильтров springSecurityFilterChain в контейнере сервлетов
@EnableWebSecurity
@EnableMethodSecurity // Для того, чтобы работали аннотации @PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Конструктор внедрения зависимостей
     * Когда Spring вызывает конструктор конфигурации, смотрит на то, что требует
     * конструктор, ищет в своем контейнере бин фильтра и подставляет его сюда.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Настройка цепочки фильтров безопасности.
     *
     * @param http объект для конфигурации HTTP-безопасности
     * @return настроенный SecurityFilterChain
     * @throws Exception в случае ошибки конфигурации
     */
     // Указывает, что метод возвращает объект (бин), которым будет управлять Spring.
     // Под капотом: Spring вызывает этот метод при старте, регистрирует возвращенный объект в своем контейнере (ApplicationContext)
     // и позволяет потом его использовать через @Autowired или конструктор.
     // Имя бина по умолчанию — имя метода.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Отключаем CSRF
                // CSRF (Cross-Site Request Forgery) — защита от межсайтовых поддельных запросов.
                // Для REST API, где клиент — отдельное приложение (React, мобильное приложение),
                // CSRF не требуется. Включают только для веб-приложений с сессиями и формами.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Настраиваем CORS
                // CORS (Cross-Origin Resource Sharing) — разрешает запросы с других доменов.
                // Например, наш фронтенд на localhost:3000 будет стучаться на бэкенд localhost:8081.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Настраиваем авторизацию запросов
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты — доступны без токена
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/actuator/health", "/actuator/info").permitAll()
                        // Эндпоинты только для роли ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // 4. Настраиваем управление сессиями
                // STATELESS — не создаём HTTP-сессии, каждый запрос аутентифицируется отдельно (через JWT)
                // Это стандарт для REST API
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Добавляем фильтр аутентификации JWT ДО стандартного фильтра
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Настройка CORS (Cross-Origin Resource Sharing).
     * <p>
     * Разрешает кросс-доменные запросы с указанных источников.
     * Для разработки разрешаем порты:3000 (React),5173 (Vite), 8080 (запасной)
     *
     * @return источник конфигурации CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешенные источники (откуда могут приходить запросы)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",     // React
                "http://localhost:5173",     // Vite (современный React)
                "http://localhost:8080"      // Альтернативный порт для фронтенда
        ));

        // Разрешенные HTTP-методы
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Разрешенные заголовки
        // "Authorization" - для передачи токена (JWT)
        // "Content-Type" - Формат данных (JSON, form-data)
        // "X-Requested-With" - это нестандартный HTTP-заголовок, который добавляется в запросы, отправленные через JavaScript (AJAX/Fetch), но не добавляется при обычном переходе по ссылке или отправке HTML-формы.
        // Добавили его, для совместимости со старыми проектами и для корректной работы CORS с некоторыми библиотеками.
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // Разрешено отправлять учетные данные (cookies, авторизационные заголовки)
        configuration.setAllowCredentials(true);

        // Применяем конфигурацию ко всем эндпоинтам (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Кодировщик паролей (BCrypt).
     * <p>
     * BCrypt — алгоритм хеширования паролей с "солью" (добавление случайной строки при хешировании, чтобы пароль был уникален каждый раз).
     * Особенности:
     * - необратимое хеширование
     * - один и тот же пароль каждый раз дает разный хеш (устойчивость к радужным таблицам)
     * - медленная попытка проверки паролей (0.1-0.3 сек на итерацию) (усложняет подбор паролей)
     *
     * @return бин PasswordEncoder, доступный для внедрения в сервисы
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}