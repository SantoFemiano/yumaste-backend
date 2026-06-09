package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UtenteRepository utenteRepository;

    @InjectMocks
    private UserService userService;

    private Utente utente;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setEmail("mario@yumaste.it");
        utente.setPasswordC("hashedPassword");
        utente.setRuolo("ROLE_USER");
        utente.setNome("Mario");
        utente.setCognome("Rossi");
    }

    @Test
    @DisplayName("loadUserByUsername - utente trovato restituisce UserDetails")
    void loadUserByUsername_found_returnsUserDetails() {
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));

        UserDetails result = userService.loadUserByUsername("mario@yumaste.it");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("mario@yumaste.it");
    }

    @Test
    @DisplayName("loadUserByUsername - utente non trovato lancia UsernameNotFoundException")
    void loadUserByUsername_notFound_throwsException() {
        when(utenteRepository.findByEmail("unknown@yumaste.it")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("unknown@yumaste.it"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("getUtenteByEmail - utente trovato")
    void getUtenteByEmail_found_returnsUtente() {
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));

        Utente result = userService.getUtenteByEmail("mario@yumaste.it");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("mario@yumaste.it");
    }

    @Test
    @DisplayName("getUtenteByEmail - utente non trovato lancia ResourceNotFoundException")
    void getUtenteByEmail_notFound_throwsException() {
        when(utenteRepository.findByEmail("ghost@yumaste.it")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUtenteByEmail("ghost@yumaste.it"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
