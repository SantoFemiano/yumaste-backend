package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.dto.request.LoginRequest;
import com.yumaste.yumasteapi.dto.request.RefreshTokenRequest;
import com.yumaste.yumasteapi.dto.request.RegisterRequest;
import com.yumaste.yumasteapi.dto.response.AuthResponse;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import com.yumaste.yumasteapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UtenteRepository utenteRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private Utente utente;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setEmail("mario@yumaste.it");
        utente.setPasswordC("hashedPwd");
    }

    // --- /register ---

    @Test
    @DisplayName("register - email gia' presente restituisce 400")
    void register_emailAlreadyExists_returns400() {
        RegisterRequest req = mock(RegisterRequest.class);
        when(req.getEmail()).thenReturn("mario@yumaste.it");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));

        ResponseEntity<AuthResponse> resp = authController.register(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(utenteRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - nuova email registra utente e restituisce token")
    void register_newEmail_savesAndReturnsTokens() {
        RegisterRequest req = mock(RegisterRequest.class);
        when(req.getEmail()).thenReturn("nuovo@yumaste.it");
        when(req.getNome()).thenReturn("Luigi");
        when(req.getCognome()).thenReturn("Verdi");
        when(utenteRepository.findByEmail("nuovo@yumaste.it")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(utenteRepository.save(any(Utente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        ResponseEntity<AuthResponse> resp = authController.register(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).isEqualTo("access-token");
        assertThat(resp.getBody().getRefreshToken()).isEqualTo("refresh-token");
        verify(utenteRepository).save(any(Utente.class));
    }

    // --- /login ---

    @Test
    @DisplayName("login - credenziali valide restituisce 200 con token")
    void login_validCredentials_returnsTokens() {
        LoginRequest req = mock(LoginRequest.class);
        when(req.getEmail()).thenReturn("mario@yumaste.it");
        when(req.getPassword()).thenReturn("pass");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(jwtService.generateToken(utente)).thenReturn("access");
        when(jwtService.generateRefreshToken(utente)).thenReturn("refresh");

        ResponseEntity<AuthResponse> resp = authController.login(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getToken()).isEqualTo("access");
        assertThat(resp.getBody().getRefreshToken()).isEqualTo("refresh");
    }

    @Test
    @DisplayName("login - credenziali errate lancia eccezione dall'AuthenticationManager")
    void login_badCredentials_throws() {
        LoginRequest req = mock(LoginRequest.class);
        when(req.getEmail()).thenReturn("mario@yumaste.it");
        when(req.getPassword()).thenReturn("wrong");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authController.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    // --- /refresh ---

    @Test
    @DisplayName("refreshToken - refresh token mancante (null) lancia 400")
    void refreshToken_nullToken_throws400() {
        RefreshTokenRequest req = mock(RefreshTokenRequest.class);
        when(req.getRefreshToken()).thenReturn(null);

        assertThatThrownBy(() -> authController.refreshToken(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("refreshToken - refresh token blank lancia 400")
    void refreshToken_blankToken_throws400() {
        RefreshTokenRequest req = mock(RefreshTokenRequest.class);
        when(req.getRefreshToken()).thenReturn("   ");

        assertThatThrownBy(() -> authController.refreshToken(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("refreshToken - token valido restituisce nuovo access token")
    void refreshToken_validToken_returnsNewAccessToken() {
        String refreshTok = "valid.refresh.token";
        RefreshTokenRequest req = mock(RefreshTokenRequest.class);
        when(req.getRefreshToken()).thenReturn(refreshTok);
        when(jwtService.extractUsername(refreshTok)).thenReturn("mario@yumaste.it");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(jwtService.isTokenValid(refreshTok, utente)).thenReturn(true);
        when(jwtService.generateToken(utente)).thenReturn("new-access");

        ResponseEntity<AuthResponse> resp = authController.refreshToken(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getToken()).isEqualTo("new-access");
        assertThat(resp.getBody().getRefreshToken()).isEqualTo(refreshTok);
    }

    @Test
    @DisplayName("refreshToken - token non valido lancia 403")
    void refreshToken_invalidToken_throws403() {
        String refreshTok = "expired.token";
        RefreshTokenRequest req = mock(RefreshTokenRequest.class);
        when(req.getRefreshToken()).thenReturn(refreshTok);
        when(jwtService.extractUsername(refreshTok)).thenReturn("mario@yumaste.it");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(jwtService.isTokenValid(refreshTok, utente)).thenReturn(false);

        assertThatThrownBy(() -> authController.refreshToken(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("refreshToken - eccezione nel parsing lancia 403")
    void refreshToken_parseException_throws403() {
        String refreshTok = "malformed.token";
        RefreshTokenRequest req = mock(RefreshTokenRequest.class);
        when(req.getRefreshToken()).thenReturn(refreshTok);
        when(jwtService.extractUsername(refreshTok)).thenThrow(new RuntimeException("JWT parse error"));

        assertThatThrownBy(() -> authController.refreshToken(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }
}
