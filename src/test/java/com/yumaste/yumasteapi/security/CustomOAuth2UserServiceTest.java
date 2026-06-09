package com.yumaste.yumasteapi.security;

import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock private UtenteRepository utenteRepository;

    // Spy per intercettare super.loadUser() senza fare HTTP reale
    @Spy
    @InjectMocks
    private CustomOAuth2UserService service;

    @Mock private OAuth2UserRequest userRequest;
    @Mock private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        // Facciamo override del metodo padre così non parte nessuna chiamata HTTP
        doReturn(oAuth2User).when(service).loadUser(userRequest);
    }

    // --- utente esistente ---

    @Test
    @DisplayName("loadUser - utente già presente nel DB: non salva nulla e restituisce OAuth2User")
    void loadUser_existingUser_doesNotSave() {
        when(oAuth2User.getAttribute("email")).thenReturn("mario@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Mario Rossi");
        when(oAuth2User.getAttribute("login")).thenReturn("mariorossi");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(new Utente()));

        OAuth2User result = service.loadUser(userRequest);

        assertThat(result).isEqualTo(oAuth2User);
        verify(utenteRepository, never()).save(any());
    }

    // --- nuovo utente con email e nome completo ---

    @Test
    @DisplayName("loadUser - nuovo utente con email valida e nome e cognome: salva e restituisce OAuth2User")
    void loadUser_newUser_withEmailAndFullName_saves() {
        when(oAuth2User.getAttribute("email")).thenReturn("nuovo@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Luigi Verdi");
        when(oAuth2User.getAttribute("login")).thenReturn("luigiverdi");
        when(utenteRepository.findByEmail("nuovo@yumaste.it")).thenReturn(Optional.empty());
        when(utenteRepository.save(any(Utente.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2User result = service.loadUser(userRequest);

        assertThat(result).isEqualTo(oAuth2User);
        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        Utente saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("nuovo@yumaste.it");
        assertThat(saved.getNome()).isEqualTo("Luigi");
        assertThat(saved.getCognome()).isEqualTo("Verdi");
        assertThat(saved.getProvider()).isEqualTo("GITHUB");
        assertThat(saved.getRuolo()).isEqualTo("ROLE_USER");
    }

    // --- email null: fallback su login@github.com ---

    @Test
    @DisplayName("loadUser - email null: genera email da login e salva nuovo utente")
    void loadUser_nullEmail_fallsBackToLoginEmail() {
        when(oAuth2User.getAttribute("email")).thenReturn(null);
        when(oAuth2User.getAttribute("name")).thenReturn("Marco");
        when(oAuth2User.getAttribute("login")).thenReturn("marco99");
        when(utenteRepository.findByEmail("marco99@github.com")).thenReturn(Optional.empty());
        when(utenteRepository.save(any(Utente.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2User result = service.loadUser(userRequest);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("marco99@github.com");
    }

    // --- email blank: fallback su login@github.com ---

    @Test
    @DisplayName("loadUser - email blank: genera email da login e salva nuovo utente")
    void loadUser_blankEmail_fallsBackToLoginEmail() {
        when(oAuth2User.getAttribute("email")).thenReturn("   ");
        when(oAuth2User.getAttribute("name")).thenReturn(null);
        when(oAuth2User.getAttribute("login")).thenReturn("gino");
        when(utenteRepository.findByEmail("gino@github.com")).thenReturn(Optional.empty());
        when(utenteRepository.save(any(Utente.class))).thenAnswer(inv -> inv.getArgument(0));

        service.loadUser(userRequest);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        // Quando name è null usa login come nome, cognome = "GitHubUser"
        assertThat(captor.getValue().getNome()).isEqualTo("gino");
        assertThat(captor.getValue().getCognome()).isEqualTo("GitHubUser");
    }

    // --- nome con una sola parola: cognome default "GitHubUser" ---

    @Test
    @DisplayName("loadUser - nome con una sola parola: cognome diventa 'GitHubUser'")
    void loadUser_singleWordName_defaultSurname() {
        when(oAuth2User.getAttribute("email")).thenReturn("solo@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Unico");
        when(oAuth2User.getAttribute("login")).thenReturn("unico");
        when(utenteRepository.findByEmail("solo@yumaste.it")).thenReturn(Optional.empty());
        when(utenteRepository.save(any(Utente.class))).thenAnswer(inv -> inv.getArgument(0));

        service.loadUser(userRequest);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        assertThat(captor.getValue().getCognome()).isEqualTo("GitHubUser");
    }
}
