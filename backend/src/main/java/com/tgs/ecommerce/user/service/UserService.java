package com.tgs.ecommerce.user.service;

import com.tgs.ecommerce.audit.domain.AuditAction;
import com.tgs.ecommerce.audit.service.AuditService;
import com.tgs.ecommerce.common.exception.BusinessRuleException;
import com.tgs.ecommerce.common.exception.ResourceNotFoundException;
import com.tgs.ecommerce.user.domain.Role;
import com.tgs.ecommerce.user.domain.RoleName;
import com.tgs.ecommerce.user.domain.User;
import com.tgs.ecommerce.user.dto.PasswordChangeRequest;
import com.tgs.ecommerce.user.dto.UserCreateRequest;
import com.tgs.ecommerce.user.dto.UserResponse;
import com.tgs.ecommerce.user.dto.UserUpdateRequest;
import com.tgs.ecommerce.user.repository.RoleRepository;
import com.tgs.ecommerce.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD de usuarios. Endpoint {@code /api/users/**} — solo accesible por
 * ADMIN (gestión de usuarios, requisito c del reto).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
            .map(UserMapper::toResponse)
            .orElseThrow(() -> ResourceNotFoundException.of("Usuario", id));
    }

    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(UserMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuario '" + username + "' no encontrado"));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessRuleException("El username ya está en uso");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("El email ya está en uso");
        }

        User user = User.builder()
            .username(request.username())
            .email(request.email())
            .fullName(request.fullName())
            .passwordHash(passwordEncoder.encode(request.password()))
            .active(true)
            .roles(resolveRoles(request.roles()))
            .build();

        User saved = userRepository.save(user);
        log.info("Usuario creado por ADMIN: {} (roles={})", saved.getUsername(), request.roles());
        auditService.log(AuditAction.CREATE, "User", saved.getId(),
            "username=" + saved.getUsername() + " roles=" + request.roles());
        return UserMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Usuario", id));

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessRuleException("El email ya está en uso");
            }
            user.setEmail(request.email());
        }
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.roles() != null && !request.roles().isEmpty()) {
            user.setRoles(resolveRoles(request.roles()));
        }

        log.info("Usuario actualizado id={}", id);
        auditService.log(AuditAction.UPDATE, "User", id, "username=" + user.getUsername());
        return UserMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(Long id, PasswordChangeRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Usuario", id));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        log.info("Contraseña cambiada para userId={}", id);
        auditService.log(AuditAction.PASSWORD_CHANGE, "User", id, "username=" + user.getUsername());
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Usuario", id));
        // Soft-delete: mantenemos el registro (para historial de órdenes)
        // pero lo desactivamos y no podrá volver a iniciar sesión.
        user.setActive(false);
        log.info("Usuario desactivado (soft-delete) id={}", id);
        auditService.log(AuditAction.DELETE, "User", id, "username=" + user.getUsername());
    }

    private Set<Role> resolveRoles(Set<RoleName> names) {
        Set<Role> roles = new HashSet<>();
        for (RoleName name : names) {
            Role r = roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(
                    "Rol " + name + " no existe — ¿faltan datos semilla?"));
            roles.add(r);
        }
        return roles;
    }
}
