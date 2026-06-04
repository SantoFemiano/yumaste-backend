package com.yumaste.yumasteapi.security;

import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;

    @Value("${frontend.url:https://yumaste-shop.vercel.app}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if(email == null) {
            email = oAuth2User.getAttribute("login") + "@github.com";
        }

        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // Genera entrambi i token utilizzando la logica aggiornata del sistema
        String token = jwtService.generateToken(utente);
        String refreshToken = jwtService.generateRefreshToken(utente);

        // Reindirizza al frontend passando entrambi i token come query parameter
        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + token + "&refreshToken=" + refreshToken;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}