package com.tgs.ecommerce.config;

import com.tgs.ecommerce.user.domain.Role;
import com.tgs.ecommerce.user.domain.RoleName;
import com.tgs.ecommerce.user.domain.User;
import com.tgs.ecommerce.user.repository.RoleRepository;
import com.tgs.ecommerce.user.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos para el perfil {@code dev}.
 *
 * <p>Al arrancar la app crea los usuarios semilla si aún no existen:
 * <ul>
 *   <li>{@code admin / admin123} — rol ADMIN</li>
 *   <li>{@code user  / user123}  — rol USER</li>
 * </ul>
 *
 * <p>Idempotente: si los usuarios ya existen (segundo arranque), no hace nada.
 *
 * <p>{@code @Profile("dev")}: NO se ejecuta en producción — allí los
 * usuarios se crean con el endpoint de registro o los inserta el DBA.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Usuarios ya presentes en la BD — saltando semilla");
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
            .orElseThrow(() -> new IllegalStateException("Rol ADMIN no encontrado — falta data.sql"));
        Role userRole = roleRepository.findByName(RoleName.USER)
            .orElseThrow(() -> new IllegalStateException("Rol USER no encontrado — falta data.sql"));

        User admin = User.builder()
            .username("admin")
            .email("admin@tgs.local")
            .fullName("Administrador")
            .passwordHash(passwordEncoder.encode("admin123"))
            .active(true)
            .roles(Set.of(adminRole, userRole))
            .build();

        User user = User.builder()
            .username("user")
            .email("user@tgs.local")
            .fullName("Usuario de prueba")
            .passwordHash(passwordEncoder.encode("user123"))
            .active(true)
            .roles(Set.of(userRole))
            .build();

        userRepository.save(admin);
        userRepository.save(user);

        log.info("=====================================================");
        log.info("Usuarios semilla creados (solo perfil dev):");
        log.info("  admin / admin123  (roles: ADMIN, USER)");
        log.info("  user  / user123   (roles: USER)");
        log.info("=====================================================");
    }
}
