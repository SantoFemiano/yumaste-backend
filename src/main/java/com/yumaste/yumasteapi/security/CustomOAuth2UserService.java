package com.yumaste.yumasteapi.security;

import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UtenteRepository utenteRepository;

    /**
     * Metodo estratto per consentire l'override nei test senza chiamate HTTP reali.
     */
    protected OAuth2User fetchOAuth2User(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = fetchOAuth2User(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String login = oAuth2User.getAttribute("login");

        // GitHub permette di nascondere l'email pubblica; in tal caso generiamo un fallback basato sul login
        if (email == null || email.isBlank()) {
            email = login + "@github.com";
        }

        Optional<Utente> userOptional = utenteRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            Utente utente = new Utente();
            utente.setEmail(email);
            utente.setProvider("GITHUB");
            utente.setRuolo("ROLE_USER");

            String[] nameParts = (name != null ? name : login).split(" ", 2);
            utente.setNome(nameParts[0]);
            utente.setCognome(nameParts.length > 1 ? nameParts[1] : "GitHubUser");

            utente.setDataRegistrazione(Instant.now());
            utente.setDataAggiornamento(Instant.now());

            utenteRepository.save(utente);
        }

        return oAuth2User;
    }
}
