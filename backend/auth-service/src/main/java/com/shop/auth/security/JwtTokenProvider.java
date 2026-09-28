package com.shop.auth.security;

import com.shop.auth.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Генерация и валидация токена (JWT)
 * <p>
 * Отвечает за:
 * - генерацию JWT при успешном входе пользователя
 * - валидацию JWT при каждом авторизованном запросе
 * - извлечение id пользователя (subject) из токена
 * <p>
 * Токен подписывается алгоритмом RS256 приватным ключом из {@link com.shop.auth.config.JwtKeyConfig}.
 * Проверяющие сервисы знают только публичный ключ, полученный по JWKS, поэтому подделать
 * токен на стороне сервиса невозможно.
 */
@Component // Позволяет Spring управлять этим классом как бином, внедрять его в другие классы
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    /** Время жизни токена (JWT). Тянется из application.yml (jwt.expiration) */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /** Идентификатор выпустившего токен сервиса. Тянется из application.yml (jwt.issuer) */
    @Value("${jwt.issuer}")
    private String jwtIssuer;

    /**
     * Конструктор внедрения зависимостей.
     *
     * @param jwtEncoder кодировщик, подписывающий токен приватным ключом
     * @param jwtDecoder декодер, проверяющий подпись токена публичным ключом
     */
    public JwtTokenProvider(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Генерация токена (JWT) для пользователя.
     * <p>
     * В токен кладутся:
     * - subject (sub) - id пользователя, которому выдан токен
     * - claim role - роль пользователя (USER или ADMIN). Она нужна другим сервисам,
     *   чтобы проверять права прямо из токена, не читая базу auth-service
     * - issuer (iss) - адрес auth-service, чтобы сервис-проверяющий убедился,
     *   что токен выпущен именно этим сервисом
     * - issuedAt (iat) и expiration (exp)
     * <p>
     * Роль кладется "чистым" значением, без префикса ROLE_: префикс добавляет сервис,
     * который проверяет токен, при переводе claim в authority
     *
     * @param user пользователь, которому выдается токен
     * @return подписанный JWT-токен
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString()) // кому выдан (id пользователя)
                .claim("role", user.getRole().name()) // роль: USER или ADMIN
                .issuer(jwtIssuer) // кто выпустил
                .issuedAt(now) // когда выдан
                .expiresAt(now.plusMillis(jwtExpiration)) // когда истекает
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .type("JWT")
                .build(); // kid проставит кодировщик из набора ключей

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * Извлечение id пользователя из JWT токена
     * <p>
     * Id пользователя лежит в claim subject, который при выдаче токена заполняется его id из БД.
     * Декодер перед этим проверяет подпись и срок действия токена.
     *
     * @param token JWT-токен
     * @return id (subject) из токена
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(jwtDecoder.decode(token).getSubject());
    }

    /**
     * Валидирует токен (JWT)
     * <p>
     * Проверяет подпись токена публичным ключом и срок токена
     *
     * @param token токен (JWT)
     * @return true - токен валидный
     */
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            return false; // При любой ошибке: подпись не совпадает, токен просрочен или поврежден
        }
    }
}
