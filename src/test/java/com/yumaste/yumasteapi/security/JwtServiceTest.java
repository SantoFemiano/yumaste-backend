package com.yumaste.yumasteapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    // Chiave Base64 di 256 bit valida per HMAC-SHA256
    private static final String SECRET = "dGVzdFNlY3JldEtleUZvckp3dFRlc3RpbmdZdW1hc3RlMTIz";
    private static final long EXPIRATION = 3_600_000L;      // 1 ora
    private static final long REFRESH_EXPIRATION = 86_400_000L; // 24 ore

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);
    }

    private UserDetails buildUser(String email) {
        return new User(email, "password", Collections.emptyList());
    }

    @Test
    @DisplayName("generateToken - deve contenere l'email come subject")
    void generateToken_containsEmail() {
        UserDetails user = buildUser("mario@yumaste.it");
        String token = jwtService.generateToken(user);
        assertThat(jwtService.extractUsername(token)).isEqualTo("mario@yumaste.it");
    }

    @Test
    @DisplayName("generateToken - il tipo deve essere 'access'")
    void generateToken_typeIsAccess() {
        UserDetails user = buildUser("mario@yumaste.it");
        String token = jwtService.generateToken(user);
        assertThat(jwtService.extractTokenType(token)).isEqualTo("access");
    }

    @Test
    @DisplayName("generateRefreshToken - il tipo deve essere 'refresh'")
    void generateRefreshToken_typeIsRefresh() {
        UserDetails user = buildUser("mario@yumaste.it");
        String token = jwtService.generateRefreshToken(user);
        assertThat(jwtService.extractTokenType(token)).isEqualTo("refresh");
    }

    @Test
    @DisplayName("isTokenValid - token valido per lo stesso utente")
    void isTokenValid_validToken() {
        UserDetails user = buildUser("luigi@yumaste.it");
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid - token di un altro utente deve essere falso")
    void isTokenValid_wrongUser() {
        UserDetails owner = buildUser("owner@yumaste.it");
        UserDetails other = buildUser("other@yumaste.it");
        String token = jwtService.generateToken(owner);
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid - token scaduto deve essere falso")
    void isTokenValid_expiredToken() throws Exception {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 ms
        UserDetails user = buildUser("exp@yumaste.it");
        String token = jwtService.generateToken(user);
        Thread.sleep(10);
        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }

    @Test
    @DisplayName("extractUsername - token malformato lancia eccezione")
    void extractUsername_malformedToken() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.token"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("generateToken con extra claims - i claims extra sono presenti")
    void generateToken_withExtraClaims() {
        UserDetails user = buildUser("claims@yumaste.it");
        java.util.Map<String, Object> extra = new java.util.HashMap<>();
        extra.put("ruolo", "ADMIN");
        String token = jwtService.generateToken(extra, user);
        String ruolo = jwtService.extractClaim(token, claims -> claims.get("ruolo", String.class));
        assertThat(ruolo).isEqualTo("ADMIN");
    }
}
