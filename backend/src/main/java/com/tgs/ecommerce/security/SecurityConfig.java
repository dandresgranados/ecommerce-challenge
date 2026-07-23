package com.tgs.ecommerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración temporal de seguridad — Fase 3.1.
 *
 * <p>Deshabilita CSRF y permite todos los endpoints para que podamos
 * verificar el arranque sin pelearnos con el login autogenerado de Spring
 * Security. En la Fase 3.2 la reemplazaremos por la configuración real
 * con filtro JWT.
 *
 * <p>También expone el bean {@link PasswordEncoder} (BCrypt) que
 * usaremos al crear usuarios.
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // La consola H2 va en un iframe → deshabilitamos frame options para /h2-console.
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * BCrypt es el algoritmo estándar para hash de contraseñas. Es lento a
     * propósito (dificulta ataques de fuerza bruta) y salado internamente.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** CORS: permite que Angular (localhost:4200) llame al backend en dev. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
