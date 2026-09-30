package com.shop.auth.controller;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Публикация публичного ключа для проверки подписи токенов.
 * <p>
 * Адрес {@code /.well-known/jwks.json} — стандартный, по нему сервисы-проверяющие
 * (например, product-service через spring.security.oauth2.resourceserver.jwt.jwk-set-uri)
 * сами забирают ключ и проверяют подпись токена локально.
 * <p>
 * Наружу отдается только публичная часть ключа: с ней можно проверить подпись,
 * но нельзя выпустить новый токен. Эндпоинт доступен без аутентификации.
 */
@RestController
public class JwksController {

    private final RSAKey rsaKey;

    /**
     * Конструктор внедрения зависимостей.
     *
     * @param rsaKey набор ключей, из которого берётся публичная часть
     */
    public JwksController(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
    }

    /**
     * Отдаёт набор публичных ключей в формате JWKS.
     *
     * @return JWKS с одним публичным ключом
     */
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
