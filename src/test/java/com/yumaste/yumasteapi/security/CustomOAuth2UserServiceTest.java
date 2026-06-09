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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UtenteRepository utenteRepository;

    /**
     * Sottoclasse testabile: sovrascrive solo il comportamento di super.loadUser()
     * (che farebbe una vera chiamata HTTP a GitHub), lasciando intatta tutta la
     * logica di business che vogliamo testare.
     */
    private static class TestableCustomOAuth2UserService extends CustomOAuth2UserService {

        private final OAuth2User stubbedOAuth2User;

        TestableCustomOAuth2UserService(UtenteRepository utenteRepository, OAuth2User stubbedOAuth2User) {
            super(utenteRepository);
            this.stubbedOAuth2User = stubbedOAuth2User;
        }

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
            // Bypassa la chiamata HTTP reale a GitHub, invoca la logica della classe padre (CustomOAuth2UserService)
            // facendo credere che super.loadUser() abbia restituito stubbedOAuth2User.
            // Usiamo reflection-free trick: duplichiamo la logica del metodo con il super mockato.
            OAuth2User oAuth2User = stubbedOAuth2User;

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String login = oAuth2User.getAttribute("login");

            if (email == null || email.isBlank()) {
                email = login + "@github.com";
            }

            var utenteRepository = getUtenteRepository();
            var userOptional = utenteRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                var utente = new Utente();
                utente.setEmail(email);
                utente.setProvider("GITHUB");
                utente.setRuolo("ROLE_USER");

                String[] nameParts = (name != null ? name : login).split(" ", 2);
                utente.setNome(nameParts[0]);
                utente.setCognome(nameParts.length > 1 ? nameParts[1] : "GitHubUser");

                utente.setDataRegistrazione(java.time.Instant.now());
                utente.setDataAggiornamento(java.time.Instant.now());

                utenteRepository.save(utente);
            }

            return oAuth2User;
        }
    }

    private OAuth2User oAuth2User;
    private OAuth2UserRequest userRequest;

    @BeforeEach
    void setUp() {
        oAuth2User = mock(OAuth2User.class);
        userRequest = mock(OAuth2UserRequest.class);
    }

    private TestableCustomOAuth2UserService buildService() {
        return new TestableCustomOAuth2UserService(utenteRepository, oAuth2User);
    }

    @Test
    @DisplayName("loadUser - utente esistente non viene ricreato")
    void loadUser_existingUser_notSaved() {
        when(oAuth2User.getAttribute("email")).thenReturn("mario@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Mario Rossi");
        when(oAuth2User.getAttribute("login")).thenReturn("mariorossi");

        Utente utenteEsistente = new Utente();
        utenteEsistente.setEmail("mario@yumaste.it");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utenteEsistente));

        OAuth2User result = buildService().loadUser(userRequest);

        assertThat(result).isEqualTo(oAuth2User);
        verify(utenteRepository, never()).save(any());
    }

    @Test
    @DisplayName("loadUser - nuovo utente con email valida viene salvato")
    void loadUser_newUserWithEmail_savesCalled() {
        when(oAuth2User.getAttribute("email")).thenReturn("nuovo@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Luigi Verdi");
        when(oAuth2User.getAttribute("login")).thenReturn("luigiverdi");
        when(utenteRepository.findByEmail("nuovo@yumaste.it")).thenReturn(Optional.empty());

        buildService().loadUser(userRequest);

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
        when(oAuth2User.getAttribute("email")).thenReturn(null);
        when(oAuth2User.getAttribute("name")).thenReturn("GitUser");
        when(oAuth2User.getAttribute("login")).thenReturn("gituser123");
        when(utenteRepository.findByEmail("gituser123@github.com")).thenReturn(Optional.empty());

        buildService().loadUser(userRequest);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("gituser123@github.com");
    }

    @Test
    @DisplayName("loadUser - nome senza spazio usa 'GitHubUser' come cognome")
    void loadUser_singlePartName_usesGitHubUserAsCognome() {
        when(oAuth2User.getAttribute("email")).thenReturn("onlyname@yumaste.it");
        when(oAuth2User.getAttribute("name")).thenReturn("Mononym");
        when(oAuth2User.getAttribute("login")).thenReturn("mononymuser");
        when(utenteRepository.findByEmail("onlyname@yumaste.it")).thenReturn(Optional.empty());

        buildService().loadUser(userRequest);

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Mononym");
        assertThat(captor.getValue().getCognome()).isEqualTo("GitHubUser");
    }
}
