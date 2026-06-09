package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.UtenteProfileDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.IndirizzoMapper;
import com.yumaste.yumasteapi.mapper.UtenteMapper;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.IndirizzoUtenteRepository;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UtenteRepository utenteRepository;
    @Mock private IndirizzoUtenteRepository indirizzoUtenteRepository;
    @Mock private IndirizzoMapper indirizzoMapper;
    @Mock private UtenteMapper utenteMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserService self;

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
    @DisplayName("getClienti - restituisce lista di profili utenti")
    void getClienti_returnsList() {
        when(utenteRepository.findByRuolo("ROLE_USER")).thenReturn(List.of(utente));
        when(indirizzoUtenteRepository.findByUtente(utente)).thenReturn(List.of());

        List<UtenteProfileDTO> result = userService.getClienti();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Mario");
        assertThat(result.get(0).cognome()).isEqualTo("Rossi");
        assertThat(result.get(0).email()).isEqualTo("mario@yumaste.it");
    }

    @Test
    @DisplayName("getClienti - lista vuota quando non ci sono clienti")
    void getClienti_emptyList() {
        when(utenteRepository.findByRuolo("ROLE_USER")).thenReturn(List.of());

        List<UtenteProfileDTO> result = userService.getClienti();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteUser - utente trovato viene eliminato")
    void deleteUser_found_deletesUser() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));

        userService.deleteUser(1L);

        verify(utenteRepository).delete(utente);
    }

    @Test
    @DisplayName("deleteUser - utente non trovato lancia ResourceNotFoundException")
    void deleteUser_notFound_throwsException() {
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
