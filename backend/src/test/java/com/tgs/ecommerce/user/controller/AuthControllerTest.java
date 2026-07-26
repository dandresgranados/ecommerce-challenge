package com.tgs.ecommerce.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tgs.ecommerce.security.JwtAuthenticationFilter;
import com.tgs.ecommerce.user.dto.AuthResponse;
import com.tgs.ecommerce.user.dto.LoginRequest;
import com.tgs.ecommerce.user.dto.UserResponse;
import com.tgs.ecommerce.user.service.AuthService;
import com.tgs.ecommerce.user.service.UserService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de slice web: {@code @WebMvcTest} carga SOLO {@link AuthController}
 * + toda la maquinaria MVC (Jackson, validación, filtros), pero NO arranca
 * la BD, ni los services reales.
 *
 * <p>Los services y filtros de seguridad se sustituyen por mocks con
 * {@code @MockitoBean} (equivalente moderno de {@code @MockBean}).
 *
 * <p>{@code addFilters = false} desactiva el filtro JWT — probamos el
 * controller de auth aislado, no la cadena de seguridad completa (para eso
 * ya tenemos las pruebas end-to-end con curl en la Fase 3.2).
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;
    /**
     * El JwtAuthenticationFilter es un {@code @Component} en el paquete
     * escaneado y {@code @WebMvcTest} intentaría instanciarlo; mockearlo
     * evita el problema de sus dependencias (JwtTokenProvider) que aquí
     * no queremos cargar.
     */
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ------------------------------------------------------------
    // POST /api/auth/login
    // ------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/login con credenciales OK → 200 y JSON con token")
    void loginExitoso() throws Exception {
        AuthResponse mockResp = new AuthResponse(
            "fake.jwt.token", "Bearer", 86400000L,
            new UserResponse(1L, "admin", "admin@x", "Admin", true,
                List.of("ADMIN"), Instant.now(), Instant.now()),
            List.of("ADMIN"));
        when(authService.login(any(LoginRequest.class))).thenReturn(mockResp);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("fake.jwt.token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.username").value("admin"))
            .andExpect(jsonPath("$.roles[0]").value("ADMIN"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login con credenciales malas → 401 con ApiError")
    void loginCredencialesInvalidas() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"WRONG"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("POST /api/auth/login sin password → 400 con fieldError")
    void loginValidacionCampos() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'password')]").exists());
    }

    // ------------------------------------------------------------
    // POST /api/auth/register
    // ------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/register con email inválido → 400 con fieldError")
    void registerEmailInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"diego","email":"no-email","password":"secret123"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')]").exists());
    }
}
