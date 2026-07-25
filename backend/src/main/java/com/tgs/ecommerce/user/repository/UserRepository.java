package com.tgs.ecommerce.user.repository;

import com.tgs.ecommerce.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@link User}.
 *
 * <p>Spring Data JPA genera automáticamente la implementación a partir del
 * nombre de los métodos: {@code findByUsername} → {@code SELECT * FROM users
 * WHERE username = ?}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
