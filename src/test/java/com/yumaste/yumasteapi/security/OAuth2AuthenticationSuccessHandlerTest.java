package com.yumaste.yumasteapi.security;

import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock private JwtService jwtService;
    @Mock private UtenteRepository utenteRepository;
    @Mock private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler handler;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Authentication authentication;
    @Mock private OAuth2User oAuth2User;

    private Utente utente;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://yumaste-shop.vercel.app");
        handler.setRedirectStrategy(redirectStrategy);

        utente = new Utente();
        utente.setEmail("mario@yumaste.it");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
    }

    @Test
    @DisplayName("onAuthenticationSuccess - email presente: redirect con token e refreshToken")
    void onAuthenticationSuccess_withEmail_redirectsWithTokens() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn("mario@yumaste.it");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(jwtService.generateToken(utente)).thenReturn("access-tok");
        when(jwtService.generateRefreshToken(utente)).thenReturn("refresh-tok");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), urlCaptor.capture());

        String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl).startsWith("https://yumaste-shop.vercel.app/oauth2/redirect");
        assertThat(redirectUrl).contains("token=access-tok");
        assertThat(redirectUrl).contains("refreshToken=refresh-tok");
    }

    @Test
    @DisplayName("onAuthenticationSuccess - email null: usa login@github.com per trovare utente")
    void onAuthenticationSuccess_nullEmail_usesLoginFallback() throws IOException, ServletException {
        when(oAuth2User.getAttribute("email")).thenReturn(null);
        when(oAuth2User.getAttribute("login")).thenReturn("mario99");
        when(utenteRepository.findByEmail("mario99@github.com")).thenReturn(Optional.of(utente));
        when(jwtService.generateToken(utente)).thenReturn("tok");
        when(jwtService.generateRefreshToken(utente)).thenReturn("rtok");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(utenteRepository).findByEmail("mario99@github.com");
        verify(redirectStrategy).sendRedirect(any(), any(), anyString());
    }

    @Test
    @DisplayName("onAuthenticationSuccess - utente non trovato: lancia RuntimeException")
    void onAuthenticationSuccess_userNotFound_throws() {
        when(oAuth2User.getAttribute("email")).thenReturn("ghost@yumaste.it");
        when(utenteRepository.findByEmail("ghost@yumaste.it")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utente non trovato");
    }

    @Test
    @DisplayName("onAuthenticationSuccess - frontendUrl custom: redirect punta al nuovo URL")
    void onAuthenticationSuccess_customFrontendUrl_redirectsCorrectly() throws IOException, ServletException {
        ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:3000");
        when(oAuth2User.getAttribute("email")).thenReturn("mario@yumaste.it");
        when(utenteRepository.findByEmail("mario@yumaste.it")).thenReturn(Optional.of(utente));
        when(jwtService.generateToken(utente)).thenReturn("t");
        when(jwtService.generateRefreshToken(utente)).thenReturn("r");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(any(), any(), captor.capture());
        assertThat(captor.getValue()).startsWith("http://localhost:3000/oauth2/redirect");
    }
}
