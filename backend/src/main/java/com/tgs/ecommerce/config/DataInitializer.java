package com.tgs.ecommerce.config;

import com.tgs.ecommerce.order.domain.DiscountWindow;
import com.tgs.ecommerce.order.domain.DiscountWindowType;
import com.tgs.ecommerce.order.repository.DiscountWindowRepository;
import com.tgs.ecommerce.user.domain.Role;
import com.tgs.ecommerce.user.domain.RoleName;
import com.tgs.ecommerce.user.domain.User;
import com.tgs.ecommerce.user.repository.RoleRepository;
import com.tgs.ecommerce.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * <p>Al arrancar la app crea, si aún no existen:
 * <ul>
 *   <li>Usuarios: {@code admin/admin123} (ADMIN) y {@code user/user123} (USER).</li>
 *   <li>Ventanas de descuento activas: 10 % GLOBAL y 50 % RANDOM
 *       (ambas válidas por 1 año, para poder probar todo el flujo).</li>
 * </ul>
 *
 * <p>Idempotente: cada bloque comprueba si ya hay datos y salta si es así.
 */
@Slf4j
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DiscountWindowRepository discountWindowRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedDiscountWindows();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            log.info("Usuarios ya presentes en la BD — saltando semilla de usuarios");
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

    private void seedDiscountWindows() {
        if (discountWindowRepository.count() > 0) {
            log.info("DiscountWindows ya presentes — saltando semilla");
            return;
        }

        Instant start = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(365, ChronoUnit.DAYS);

        DiscountWindow global = DiscountWindow.builder()
            .name("Promoción anual global")
            .type(DiscountWindowType.GLOBAL)
            .rate(new BigDecimal("0.1000"))
            .startAt(start)
            .endAt(end)
            .active(true)
            .build();

        DiscountWindow random = DiscountWindow.builder()
            .name("Promoción anual pedido aleatorio")
            .type(DiscountWindowType.RANDOM)
            .rate(new BigDecimal("0.5000"))
            .startAt(start)
            .endAt(end)
            .active(true)
            .build();

        discountWindowRepository.save(global);
        discountWindowRepository.save(random);
        log.info("DiscountWindows semilla creadas: GLOBAL 10% y RANDOM 50% (1 año)");
    }
}
