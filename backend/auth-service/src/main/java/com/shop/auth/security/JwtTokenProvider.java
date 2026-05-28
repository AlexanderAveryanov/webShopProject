package com.shop.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Генерация и валидация токена (JWT)
 * <p>
 * Отвечает за:
 * - генерацию JWT при успешном входе пользователя
 * - валидацию JWT при каждом авторизованном запросе
 * - извлечение id пользователя (subject) из токена
 */
@Component // Позволяет Spring управлять этим классом как бином, внедрять его в другие классы
public class JwtTokenProvider {
    /** Секретный ключ для подписи токена (JWT). Тянется из application.yml (jwt.secret) */
    @Value("${jwt.secret}") // Внедрение значений из различных источников в поля, методы или параметры конструктора. Подстановка происходит в runtime.
    private String jwtSecret;

    /** Время жизни токена (JWT). Тянется из application.yml (jwt.expiration) */
    @Value("${jwt.expiration}")
    private int jwtExpiration;

    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        return Jwts.builder()
                .subject(userId.toString()) // кому выдан (id пользователя)
                .issuedAt(now)              // когда выдан
                .expiration(expiryDate)     // когда истекает
                .signWith(getSigningKey())  // подпись секретным ключом
                .compact();                 // собираем в строку
    }

    /**
     * Извлечение id пользователя из JWT токена
     *
     * @param token JWT-токен
     * @return id (subject) из токена
     */
    public String getUserIdFromToken(String token) {
        // Claims - объект, содержащий все данные, зашитые в токен:
        // - Subject - Субъект (обычно id пользователя, или другое уникальное значение для идентификации)
        // - IssuedAt - Время выдачи токена
        // - Expiration - Время истечения токена
        // - Issuer - Кто выдал токен
        // - Audience - Для кого предназначен токен
        Claims claims = Jwts.parser()  // Создает парсер
                .verifyWith(getSigningKey()) // Устанавливает секретный ключ для верификации подписи токена
                .build() // Завершает конфигурацию парсера и создает готовый объект JwtParser
                .parseSignedClaims(token) // Проверка подписи токена (через установленный секретный ключ в verifyWith) и срок действия. Если все ок, возвращает объект Jws<Claims>
                .getPayload(); // Достает объект Claims (данные) из объекта Jws<Claims> (подпись + данные)
        return claims.getSubject(); // Достает id пользователя
    }

    /**
     * Валидирует токен (JWT)
     * <p>
     * Проверяет подпись токена на соответствие секрету и срок токена
     *
     * @param token токен (JWT)
     * @return true - токен валидный
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false; // При любой ошибке. (подпись не совпадает, токен просрочен, малактив)
        }
    }

    /**
     * Создает ключ подписи из секрета
     * <p>
     * Преобразует строковый секрет в объект SecretKey, который использует библиотека JJWT для подписи и проверки
     *
     * @return SecretKey для подписи JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
