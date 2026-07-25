package com.tgs.ecommerce.security;

import com.tgs.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link UserDetailsService} que carga usuarios de la BD.
 *
 * <p>Spring Security la invoca durante el login para verificar credenciales
 * y también podría usarse en cada request si no llevamos JWT.
 *
 * <p>{@code @Transactional(readOnly = true)} mantiene la sesión JPA abierta
 * durante el método para que la relación LAZY {@code User.roles} se pueda
 * cargar sin {@code LazyInitializationException}.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(CustomUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
}
