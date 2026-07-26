package com.tgs.ecommerce.user.service;

import com.tgs.ecommerce.audit.domain.AuditAction;
import com.tgs.ecommerce.audit.service.AuditService;
import com.tgs.ecommerce.common.exception.BusinessRuleException;
import com.tgs.ecommerce.security.CustomUserDetails;
import com.tgs.ecommerce.security.JwtProperties;
import com.tgs.ecommerce.security.JwtTokenProvider;
import com.tgs.ecommerce.user.domain.Role;
import com.tgs.ecommerce.user.domain.RoleName;
import com.tgs.ecommerce.user.domain.User;
import com.tgs.ecommerce.user.dto.AuthResponse;
import com.tgs.ecommerce.user.dto.LoginRequest;
import com.tgs.ecommerce.user.dto.RegisterRequest;
import com.tgs.ecommerce.user.repository.RoleRepository;
import com.tgs.ecommerce.user.repository.UserRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de autenticación y registro público.
 *
 * <p>{@code login}: delega en el {@link AuthenticationManager} — que a su
 * vez usa el {@code UserDetailsServiceImpl} + {@code PasswordEncoder} — y
 * si las credenciales son válidas, genera un JWT.
 *
 * <p>{@code register}: crea un usuario nuevo con rol {@link RoleName#USER}
 * y devuelve el JWT como si acabase de hacer login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final AuditService auditService;

    /**
     * Autentica al usuario y devuelve el JWT + información pública.
     */
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
            auditService.log(AuditAction.LOGIN, "User", details.getUser().getId(),
                "username=" + details.getUsername());
            return toAuthResponse(details);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            auditService.log(AuditAction.LOGIN_FAILED, "User", null,
                "username=" + request.username() + " reason=" + ex.getClass().getSimpleName());
            throw ex;
        }
    }

    /**
     * Registra un usuario nuevo con rol USER y devuelve su JWT.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessRuleException("El username ya está en uso");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("El email ya está en uso");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
            .orElseThrow(() -> new IllegalStateException(
                "Rol USER no encontrado — ¿faltan datos semilla?"));

        User user = User.builder()
            .username(request.username())
            .email(request.email())
            .fullName(request.fullName())
            .passwordHash(passwordEncoder.encode(request.password()))
            .active(true)
            .roles(Set.of(userRole))
            .build();

        User saved = userRepository.save(user);
        log.info("Usuario registrado: {}", saved.getUsername());
        auditService.log(AuditAction.REGISTER, "User", saved.getId(),
            "username=" + saved.getUsername() + " email=" + saved.getEmail());

        return toAuthResponse(new CustomUserDetails(saved));
    }

    private AuthResponse toAuthResponse(CustomUserDetails details) {
        List<String> roles = details.getRoleNames();
        String token = tokenProvider.generateToken(details.getUsername(), roles);
        return new AuthResponse(
            token,
            "Bearer",
            jwtProperties.getExpirationMs(),
            UserMapper.toResponse(details.getUser()),
            roles
        );
    }
}
