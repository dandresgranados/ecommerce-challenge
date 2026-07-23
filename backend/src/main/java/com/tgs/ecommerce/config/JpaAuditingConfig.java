package com.tgs.ecommerce.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Habilita el sistema de auditoría de Spring Data JPA.
 *
 * <p>{@code @EnableJpaAuditing} activa el procesamiento de las anotaciones
 * {@code @CreatedDate}, {@code @LastModifiedDate}, {@code @CreatedBy} y
 * {@code @LastModifiedBy} declaradas en {@link
 * com.tgs.ecommerce.common.domain.AuditableEntity}.
 *
 * <p>El bean {@link AuditorAware} le dice a Spring cómo obtener el
 * "usuario actual" para rellenar {@code createdBy}/{@code updatedBy}.
 * Aquí lo resolvemos leyendo el {@code SecurityContext} de Spring Security
 * (que se rellena tras el login). Si no hay usuario autenticado (por
 * ejemplo, tests o carga inicial) devolvemos "system".
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("system");
            }
            return Optional.ofNullable(auth.getName());
        };
    }
}
