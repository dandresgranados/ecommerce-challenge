package com.tgs.ecommerce.user.controller;

import com.tgs.ecommerce.user.dto.AuthResponse;
import com.tgs.ecommerce.user.dto.LoginRequest;
import com.tgs.ecommerce.user.dto.RegisterRequest;
import com.tgs.ecommerce.user.dto.UserResponse;
import com.tgs.ecommerce.user.service.AuthService;
import com.tgs.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de autenticación y consulta del usuario actual.
 *
 * <ul>
 *   <li>{@code POST /api/auth/login} — pública</li>
 *   <li>{@code POST /api/auth/register} — pública</li>
 *   <li>{@code GET  /api/auth/me} — requiere JWT</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Devuelve el usuario asociado al JWT actual. Útil para que el frontend
     * refresque su estado tras un login o un F5.
     *
     * <p>{@code @AuthenticationPrincipal String username} inyecta el
     * {@code principal} que puso el {@link
     * com.tgs.ecommerce.security.JwtAuthenticationFilter} en el
     * {@code SecurityContext} — que en nuestro filtro es el username.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userService.getByUsername(username));
    }
}
