package com.shop.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * Конфигурация пары RSA-ключей для подписи JWT и их публикации в виде JWKS.
 * <p>
 * Ключи лежат в каталоге {@code jwt.keys-dir} (по умолчанию {@code keys} рядом с работающим
 * приложением) и не попадают в git. Если файлов нет, пара генерируется при первом старте
 * и сохраняется на диск, поэтому ранее выданные токены переживают перезапуск сервиса.
 * <p>
 * Подпись RS256: приватный ключ нужен только auth-service, а проверяющие сервисы забирают
 * публичный ключ по адресу {@code /.well-known/jwks.json}. Благодаря этому product-service
 * не получает секрет, которым можно подделать токен.
 */
@Configuration
public class JwtKeyConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyConfig.class);

    /** Каталог, в котором лежат private.pem и public.pem */
    @Value("${jwt.keys-dir:keys}")
    private String keysDir;

    /**
     * Приватный ключ для подписи: читается с диска, а при первом старте генерируется и сохраняется.
     *
     * @return приватный ключ RSA
     * @throws Exception если ключ не удалось прочитать или сгенерировать
     */
    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        Path privateKeyPath = Path.of(keysDir, "private.pem");
        if (Files.exists(privateKeyPath)) {
            return readPrivateKey(Files.readString(privateKeyPath));
        }

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        // Пишем оба файла: приватный нужен сервису при следующем старте,
        // публичный оставляем рядом для человека и внешних инструментов
        Files.createDirectories(Path.of(keysDir));
        writeKey(privateKeyPath, "PRIVATE KEY", keyPair.getPrivate().getEncoded());
        writeKey(Path.of(keysDir, "public.pem"), "PUBLIC KEY", keyPair.getPublic().getEncoded());
        log.info("Сгенерирована новая пара RSA-ключей в каталоге {}", Path.of(keysDir).toAbsolutePath());

        return (RSAPrivateKey) keyPair.getPrivate();
    }

    /**
     * Публичный ключ восстанавливается из приватного, чтобы не расходились две копии ключа.
     *
     * @param rsaPrivateKey приватный ключ
     * @return публичный ключ RSA
     * @throws Exception если публичную часть не удалось восстановить
     */
    @Bean
    public RSAPublicKey rsaPublicKey(RSAPrivateKey rsaPrivateKey) throws Exception {
        RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey) rsaPrivateKey;
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent()));
    }

    /**
     * Набор ключей в формате JWKS с приватной частью — он нужен кодировщику для подписи.
     * <p>
     * Идентификатор ключа (kid) считается из публичного ключа по RFC 7638, поэтому он стабилен
     * между перезапусками и совпадает с тем, что пишется в заголовок подписанного токена.
     * Наружу (в JWKS-эндпоинт) отдаётся только публичная часть — см. {@code toPublicJWK()}.
     *
     * @param rsaPublicKey  публичный ключ
     * @param rsaPrivateKey приватный ключ
     * @return JWKS с одним ключом
     * @throws Exception если не удалось вычислить отпечаток ключа
     */
    @Bean
    public RSAKey rsaJwk(RSAPublicKey rsaPublicKey, RSAPrivateKey rsaPrivateKey) throws Exception {
        return new RSAKey.Builder(rsaPublicKey)
                .privateKey(rsaPrivateKey)
                .keyID(new RSAKey.Builder(rsaPublicKey).build().computeThumbprint().toString())
                .build();
    }

    /**
     * Кодировщик токенов: подписывает JWT приватным ключом алгоритмом RS256.
     *
     * @param rsaJwk набор ключей
     * @return кодировщик
     */
    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaJwk) {
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaJwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Декодер для собственных проверок auth-service: сверяет подпись своим публичным ключом.
     *
     * @param rsaPublicKey публичный ключ
     * @return декодер
     */
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }

    /**
     * Читает приватный ключ в формате PKCS#8 из PEM-файла.
     *
     * @param pem содержимое файла
     * @return приватный ключ
     * @throws Exception если ключ не разбирается
     */
    private RSAPrivateKey readPrivateKey(String pem) throws Exception {
        String base64 = stripPem(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64));
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * Убирает из PEM-заголовки и переводы строк, оставляя только base64.
     *
     * @param pem содержимое файла
     * @return base64 без переводов строк
     */
    private String stripPem(String pem) {
        return pem.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", "");
    }

    /**
     * Записывает ключ в PEM-файл в читаемом виде.
     *
     * @param path    куда писать
     * @param type    тип блока PEM, например PRIVATE KEY
     * @param encoded ключ в DER-кодировке
     * @throws IOException если не удалось записать файл
     */
    private void writeKey(Path path, String type, byte[] encoded) throws IOException {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        String pem = "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
        Files.writeString(path, pem, StandardCharsets.UTF_8);
    }
}
