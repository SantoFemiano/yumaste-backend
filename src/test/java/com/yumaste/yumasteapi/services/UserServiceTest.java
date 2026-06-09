package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.CambioPasswordDTO;
import com.yumaste.yumasteapi.dto.request.IndirizzoRequestDTO;
import com.yumaste.yumasteapi.dto.request.UserUpdateDTO;
import com.yumaste.yumasteapi.dto.response.IndirizzoResponseDTO;
import com.yumaste.yumasteapi.dto.response.UtenteAggDTO;
import com.yumaste.yumasteapi.dto.response.UtenteProfileDTO;
import com.yumaste.yumasteapi.exceptions.BusinessException;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.IndirizzoMapper;
import com.yumaste.yumasteapi.mapper.UtenteMapper;
import com.yumaste.yumasteapi.models.IndirizzoUtente;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UtenteRepository utenteRepository;
    @Mock IndirizzoUtenteRepository indirizzoUtenteRepository;
    @Mock IndirizzoMapper indirizzoMapper;
    @Mock UtenteMapper utenteMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserService self;

    @InjectMocks
    UserService userService;

    private Utente utente;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setEmail("mario@yumaste.it");
        utente.setNome("Mario");
        utente.setCognome("Rossi");
    }

    // --- putProfile ---

    @Test
    @DisplayName("putProfile - aggiorna correttamente i dati utente")
    void putProfile_updatesAndReturnsDto() {
        UserUpdateDTO req = new UserUpdateDTO("nuovo@mail.it", "Luigi", "Verdi");
        // findByEmail ritorna lo stesso oggetto utente: così i setXxx agiscono su di esso
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(utenteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UtenteAggDTO expected = mock(UtenteAggDTO.class);
        when(utenteMapper.toDto(any(Utente.class))).thenReturn(expected);

        UtenteAggDTO result = userService.putProfile(utente, req);

        assertThat(result).isEqualTo(expected);
        assertThat(utente.getEmail()).isEqualTo("nuovo@mail.it");
        assertThat(utente.getNome()).isEqualTo("Luigi");
        assertThat(utente.getCognome()).isEqualTo("Verdi");
    }

    @Test
    @DisplayName("putProfile - lancia ResourceNotFoundException se utente non trovato")
    void putProfile_userNotFound_throws() {
        when(utenteRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.putProfile(utente, new UserUpdateDTO("a@b.it", "A", "B")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getClienti ---

    @Test
    @DisplayName("getClienti - restituisce lista clienti mappati")
    void getClienti_returnsMappedList() {
        Utente u2 = new Utente();
        u2.setId(2L);
        u2.setEmail("b@b.it");
        when(utenteRepository.findByRuolo("ROLE_USER")).thenReturn(List.of(utente, u2));
        when(indirizzoUtenteRepository.findByUtente(any())).thenReturn(List.of());

        List<UtenteProfileDTO> result = userService.getClienti();

        assertThat(result).hasSize(2);
    }

    // --- aggiungiIndirizzo ---

    @Test
    @DisplayName("aggiungiIndirizzo - salva e restituisce DTO")
    void aggiungiIndirizzo_savesAndReturnsDTO() {
        IndirizzoRequestDTO req = mock(IndirizzoRequestDTO.class);
        IndirizzoUtente entity = new IndirizzoUtente();
        IndirizzoResponseDTO dto = mock(IndirizzoResponseDTO.class);

        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(indirizzoMapper.toEntity(req)).thenReturn(entity);
        when(indirizzoUtenteRepository.save(entity)).thenReturn(entity);
        when(indirizzoMapper.toDTO(entity)).thenReturn(dto);

        IndirizzoResponseDTO result = userService.aggiungiIndirizzo("mario@yumaste.it", req);
        assertThat(result).isEqualTo(dto);
        assertThat(entity.getStato()).isEqualTo("attivo");
    }

    // --- deleteIndirizzo ---

    @Test
    @DisplayName("deleteIndirizzo - soft delete (stato=inattivo)")
    void deleteIndirizzo_softDelete() {
        IndirizzoUtente indirizzo = new IndirizzoUtente();
        when(indirizzoUtenteRepository.findByIdAndUtenteId(1L, 1L)).thenReturn(Optional.of(indirizzo));

        userService.deleteIndirizzo(1L, utente);

        assertThat(indirizzo.getStato()).isEqualTo("inattivo");
        verify(indirizzoUtenteRepository).save(indirizzo);
    }

    @Test
    @DisplayName("deleteIndirizzo - lancia ResourceNotFoundException se non trovato")
    void deleteIndirizzo_notFound_throws() {
        when(indirizzoUtenteRepository.findByIdAndUtenteId(anyLong(), anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteIndirizzo(99L, utente))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getIndirizziAttivi ---

    @Test
    @DisplayName("getIndirizziAttivi - restituisce solo indirizzi attivi mappati")
    void getIndirizziAttivi_returnsMappedActive() {
        IndirizzoUtente addr = new IndirizzoUtente();
        IndirizzoResponseDTO dto = mock(IndirizzoResponseDTO.class);

        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(indirizzoUtenteRepository.findByUtenteAndStato(utente, "attivo")).thenReturn(List.of(addr));
        when(indirizzoMapper.toDTO(addr)).thenReturn(dto);

        List<IndirizzoResponseDTO> result = userService.getIndirizziAttivi("mario@yumaste.it");
        assertThat(result).containsExactly(dto);
    }

    // --- putProfilePass ---

    @Test
    @DisplayName("putProfilePass - cambia password con successo")
    void putProfilePass_changesPassword() {
        utente.setPasswordC("oldHash");
        CambioPasswordDTO req = new CambioPasswordDTO("oldPass", "newPass");

        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches("oldPass", utente.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHash");
        when(utenteRepository.save(utente)).thenReturn(utente);
        when(utenteMapper.toDto(utente)).thenReturn(mock(UtenteAggDTO.class));

        userService.putProfilePass(utente, req);

        verify(passwordEncoder).encode("newPass");
        verify(utenteRepository).save(utente);
    }

    @Test
    @DisplayName("putProfilePass - lancia BusinessException se vecchia password errata")
    void putProfilePass_wrongOldPassword_throws() {
        utente.setPasswordC("hash");
        CambioPasswordDTO req = new CambioPasswordDTO("wrong", "new");

        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(passwordEncoder.matches(eq("wrong"), any())).thenReturn(false);

        assertThatThrownBy(() -> userService.putProfilePass(utente, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vecchia password");
    }

    // --- deleteUser ---

    @Test
    @DisplayName("deleteUser - elimina utente trovato")
    void deleteUser_deletesUser() {
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));

        userService.deleteUser(1L);

        verify(utenteRepository).delete(utente);
    }

    @Test
    @DisplayName("deleteUser - lancia ResourceNotFoundException se non trovato")
    void deleteUser_notFound_throws() {
        when(utenteRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
