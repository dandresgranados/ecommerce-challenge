package com.tgs.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Genera y valida JSON Web Tokens firmados con HMAC-SHA256.
 *
 * <p>Los tokens contienen:
 * <ul>
 *   <li>{@code sub} — username del usuario.</li>
 *   <li>{@code roles} — lista de roles (ej. ["ADMIN"]).</li>
 *   <li>{@code iat} — fecha de emisión.</li>
 *   <li>{@code exp} — fecha de expiración.</li>
 * </ul>
 *
 * <p>La firma HMAC garantiza que el payload no ha sido manipulado (si alguien
 * cambia el username, la firma deja de coincidir y el token se rechaza).
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties props) {
        // Aceptamos tanto una clave Base64 como texto plano. Si el string tiene
        // longitud suficiente en bytes UTF-8, lo usamos directo.
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(props.getSecret());
            if (keyBytes.length < 32) {
                keyBytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException e) {
            keyBytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "app.jwt.secret debe tener al menos 32 bytes (256 bits) para HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = props.getExpirationMs();
    }

    /**
     * Genera un JWT para el usuario dado.
     *
     * @param username identificador único (ira al claim {@code sub})
     * @param roles roles del usuario (ej. ["ADMIN", "USER"])
     * @return token codificado en Base64URL (cadena "xxx.yyy.zzz")
     */
    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
            .subject(username)
            .claims(Map.of("roles", roles))
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey)
            .compact();
    }

    /** Extrae el username (subject) del token. */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extrae la lista de roles del token. */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        return roles instanceof List<?> list ? (List<String>) list : List.of();
    }

    /**
     * Valida firma y expiración. Devuelve true si el token es válido y usable,
     * false en cualquier otro caso (firma alterada, expirado, malformado).
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
