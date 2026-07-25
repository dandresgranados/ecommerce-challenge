package com.tgs.ecommerce.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del JWT leída del bloque {@code app.jwt} del YAML.
 *
 * <p>Ejemplo:
 * <pre>
 * app:
 *   jwt:
 *     secret: "..."
 *     expiration-ms: 86400000
 * </pre>
 *
 * <p>Se registra como bean con {@code @EnableConfigurationProperties} en la
 * clase {@link SecurityConfig}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Clave HMAC para firmar los tokens. Mínimo 32 caracteres (256 bits). */
    private String secret;

    /** Duración del token en milisegundos. */
    private long expirationMs;
}
