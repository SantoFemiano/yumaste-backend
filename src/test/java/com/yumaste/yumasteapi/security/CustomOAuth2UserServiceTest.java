package com.yumaste.yumasteapi.security;

import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UtenteRepository utenteRepository;

    // Usiamo una spy per evitare la vera chiamata HTTP a GitHub OAuth2
    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        service = spy(new CustomOAuth2UserService(utenteRepository));
    }

    @Test
    @DisplayName("loadUser - utente esistente non viene ricreato")
    void loadUser_existingUser_notSaved() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);

        when(oAuth2User.getAttribute("email")).thenReturn("mario@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Mario Rossi");
        when(oAuth2User.getAttribute("login")).thenReturn("mariorossi");
        doReturn(oAuth2User).when(service).loadUser(userRequest); // bypass HTTP

        Utente utenteEsistente = new Utente();
        utenteEsistente.setEmail("mario@yumaste.it");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utenteEsistente));

        OAuth2User result = service.loadUser(userRequest);

        assertThat(result).isEqualTo(oAuth2User);
        verify(utenteRepository, never()).save(any());
    }

    @Test
    @DisplayName("loadUser - nuovo utente con email valida viene salvato")
    void loadUser_newUserWithEmail_savesCalled() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);

        when(oAuth2User.getAttribute("email")).thenReturn("nuovo@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Luigi Verdi");
        when(oAuth2User.getAttribute("login")).thenReturn("luigiverdi");
        doReturn(oAuth2User).when(service).loadUser(userRequest);

        when(utenteRepository.findByEmail("nuovo@yumaste.it")).thenReturn(Optional.empty());

        service.loadUser(userRequest);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        Utente salvato = captor.getValue();

        assertThat(salvato.getEmail()).isEqualTo("nuovo@yumaste.it");
        assertThat(salvato.getProvider()).isEqualTo("GITHUB");
        assertThat(salvato.getRuolo()).isEqualTo("ROLE_USER");
        assertThat(salvato.getNome()).isEqualTo("Luigi");
        assertThat(salvato.getCognome()).isEqualTo("Verdi");
    }

    @Test
    @DisplayName("loadUser - email null, usa login come fallback email")
    void loadUser_nullEmail_usesLoginFallback() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);

        when(oAuth2User.getAttribute("email")).thenReturn(null);
        when(oAuth2User.getAttribute("name")).thenReturn("GitUser");
        when(oAuth2User.getAttribute("login")).thenReturn("gituser123");
        doReturn(oAuth2User).when(service).loadUser(userRequest);

        when(utenteRepository.findByEmail("gituser123@github.com")).thenReturn(Optional.empty());

        service.loadUser(userRequest);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("gituser123@github.com");
    }

    @Test
    @DisplayName("loadUser - nome senza spazio usa login come cognome")
    void loadUser_singlePartName_usesGitHubUserAsCognome() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);

        when(oAuth2User.getAttribute("email")).thenReturn("onlyname@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Mononym");
        when(oAuth2User.getAttribute("login")).thenReturn("mononymuser");
        doReturn(oAuth2User).when(service).loadUser(userRequest);

        when(utenteRepository.findByEmail("onlyname@yumaste.it")).thenReturn(Optional.empty());

        service.loadUser(userRequest);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Mononym");
        assertThat(captor.getValue().getCognome()).isEqualTo("GitHubUser");
    }
}
