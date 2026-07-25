package com.tgs.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración real de Spring Security (Fase 3.2).
 *
 * <ul>
 *   <li>Sesiones {@code STATELESS} — cada request lleva su propio JWT.</li>
 *   <li>Rutas públicas: {@code /api/auth/**}, {@code /h2-console/**},
 *       {@code /actuator/health}.</li>
 *   <li>Todo lo demás requiere autenticación.</li>
 *   <li>{@link JwtAuthenticationFilter} corre antes del filtro estándar
 *       de login.</li>
 *   <li>CORS abierto para el frontend Angular en localhost:4200.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity   // habilita @PreAuthorize/@PostAuthorize en los controllers/services
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF no aplica a APIs stateless con JWT.
            .csrf(AbstractHttpConfigurer::disable)

            // La consola H2 se sirve en un iframe.
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))

            // Sin sesiones HTTP: cada request es independiente.
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // El resto requiere autenticación
                .anyRequest().authenticated()
            )

            // Colocamos nuestro filtro JWT ANTES del filtro de login estándar.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt: algoritmo estándar para hashear contraseñas.
     * Lento a propósito (dificulta fuerza bruta), salado internamente.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el {@link AuthenticationManager} como bean para que el
     * AuthService pueda invocarlo desde el endpoint de login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /** CORS: permite al frontend Angular (localhost:4200) llamar al API. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization")
            .allowCredentials(true);
    }
}
