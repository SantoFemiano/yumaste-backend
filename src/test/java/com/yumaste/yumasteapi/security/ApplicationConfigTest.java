package com.yumaste.yumasteapi.security;

import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per ApplicationConfig:
 * - UserDetailsService carica utente o lancia eccezione
 * - PasswordEncoder è BCrypt e codifica correttamente
 * - AuthenticationProvider è DaoAuthenticationProvider
 */
@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private UtenteRepository utenteRepository;

    @InjectMocks
    private ApplicationConfig applicationConfig;

    // -------------------------------------------------------
    // UserDetailsService
    // -------------------------------------------------------

    @Test
    @DisplayName("userDetailsService - carica utente esistente tramite email")
    void userDetailsService_loadsExistingUser() {
        Utente utente = new Utente();
        utente.setEmail("mario@yumaste.it");
        utente.setPasswordC("hash");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));

        UserDetailsService uds = applicationConfig.userDetailsService();
        var result = uds.loadUserByUsername("mario@yumaste.it");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("mario@yumaste.it");
    }

    @Test
    @DisplayName("userDetailsService - lancia UsernameNotFoundException se utente non trovato")
    void userDetailsService_throwsIfUserNotFound() {
        when(utenteRepository.findByEmail("nessuno@yumaste.it")).thenReturn(Optional.empty());

        UserDetailsService uds = applicationConfig.userDetailsService();

        assertThatThrownBy(() -> uds.loadUserByUsername("nessuno@yumaste.it"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Utente non trovato");
    }

    // -------------------------------------------------------
    // PasswordEncoder
    // -------------------------------------------------------

    @Test
    @DisplayName("passwordEncoder - restituisce un'istanza di BCryptPasswordEncoder")
    void passwordEncoder_isBCrypt() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("passwordEncoder - codifica e verifica correttamente una password")
    void passwordEncoder_encodesAndMatches() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        String raw = "mySecretPassword";
        String encoded = encoder.encode(raw);

        assertThat(encoded).isNotEqualTo(raw);
        assertThat(encoder.matches(raw, encoded)).isTrue();
        assertThat(encoder.matches("wrongPassword", encoded)).isFalse();
    }

    @Test
    @DisplayName("passwordEncoder - due encoding della stessa password producono hash diversi (salt)")
    void passwordEncoder_producesDifferentHashEachTime() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        String raw = "password123";
        String hash1 = encoder.encode(raw);
        String hash2 = encoder.encode(raw);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(encoder.matches(raw, hash1)).isTrue();
        assertThat(encoder.matches(raw, hash2)).isTrue();
    }

    // -------------------------------------------------------
    // AuthenticationProvider
    // -------------------------------------------------------

    @Test
    @DisplayName("authenticationProvider - restituisce DaoAuthenticationProvider")
    void authenticationProvider_isDaoProvider() {
        AuthenticationProvider provider = applicationConfig.authenticationProvider();
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
    }
}
