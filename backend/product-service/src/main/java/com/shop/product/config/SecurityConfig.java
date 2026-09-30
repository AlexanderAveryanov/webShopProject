package com.shop.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация безопасности product-service.
 * <p>
 * Сервис выступает в роли ресурс-сервера OAuth2: он сам проверяет подпись и срок действия
 * JWT-токена, выданного auth-service, и не обращается к auth-service за ролью пользователя -
 * роль лежит в самом токене, в claim "role".
 * <p>
 * Ключ для проверки подписи сервис получает штатным способом: по адресу JWKS auth-service
 * ({@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri}), а значение claim iss
 * сверяется с {@code issuer-uri}. Никаких ключей и секретов в конфигурации сервиса нет:
 * подделать токен на его стороне невозможно, а проверка подписи локальная и не требует
 * обращений к auth-service на каждый запрос.
 * <p>
 * Определяет:
 * - как claim "role" превращается в права пользователя
 * - настройки CORS (кросс-доменные запросы)
 * - отключение CSRF (для REST API)
 * - стратегию работы с сессиями (stateless)
 */
@Configuration
// Активирует механизмы безопасности Spring Security: фильтры, аутентификацию, авторизацию
@EnableWebSecurity
@EnableMethodSecurity // Для того, чтобы работали аннотации @PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed
public class SecurityConfig {

    /**
     * Преобразует claim "role" токена в права пользователя.
     * <p>
     * В токене лежит "чистая" роль (USER или ADMIN) без префикса.
     * Здесь она превращается в authority с префиксом ROLE_ (ROLE_USER, ROLE_ADMIN),
     * поэтому в аннотациях используется hasRole('ADMIN') - так же, как в auth-service.
     *
     * @return конвертер прав пользователя
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }

    /**
     * Настройка цепочки фильтров безопасности.
     *
     * @param http объект для конфигурации HTTP-безопасности
     * @return настроенный SecurityFilterChain
     * @throws Exception в случае ошибки конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Отключаем CSRF
                // CSRF (Cross-Site Request Forgery) - защита от поддельных запросов.
                // Для REST API, где клиент - отдельное приложение (React, мобильное приложение),
                // CSRF не требуется. Включают только для веб-приложений с сессиями и формами.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Настраиваем CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Настраиваем проверку токена и авторизацию запросов
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты - доступны без токена
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Каталог товаров закрыт полностью: роли проверяются на уровне методов (@PreAuthorize)
                        .anyRequest().authenticated()
                )

                // 4. Настраиваем управление сессиями
                // STATELESS - не создаём HTTP-сессии, каждый запрос аутентифицируется отдельно (через JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. Включаем проверку JWT-токенов (Bearer) и разбор claim "role" в права
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Настройки CORS (Cross-Origin Resource Sharing).
     * <p>
     * Разрешённые порты:3000 (React), 5173 (Vite), 8080 (запасной).
     * Набор методов повторяет методы каталога, включая PATCH для изменения остатка.
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
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Разрешенные заголовки
        // "Authorization" - для передачи токена (JWT)
        // "Content-Type" - Формат данных (JSON, form-data)
        // "X-Requested-With" - нестандартный HTTP-заголовок, добавляется запросами из JavaScript (Fetch/AJAX)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // Разрешено отправлять учетные данные (cookies, авторизационные заголовки)
        configuration.setAllowCredentials(true);

        // Применяем конфигурацию ко всем эндпоинтам (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
