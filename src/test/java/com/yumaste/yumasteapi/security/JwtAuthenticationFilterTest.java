package com.yumaste.yumasteapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal - senza header Authorization, passa al filtro successivo")
    void doFilterInternal_noAuthHeader_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    @DisplayName("doFilterInternal - header senza 'Bearer ', passa al filtro successivo")
    void doFilterInternal_headerWithoutBearer_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    @DisplayName("doFilterInternal - token di tipo refresh viene ignorato")
    void doFilterInternal_refreshToken_continuesChainWithoutAuth() throws Exception {
        String jwt = "some.refresh.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn("user@yumaste.it");
        when(jwtService.extractTokenType(jwt)).thenReturn("refresh");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - token access valido imposta l'autenticazione nel contesto")
    void doFilterInternal_validAccessToken_setsAuthentication() throws Exception {
        String jwt = "some.valid.access.token";
        UserDetails userDetails = new User("mario@yumaste.it", "password", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn("mario@yumaste.it");
        when(jwtService.extractTokenType(jwt)).thenReturn("access");
        when(userDetailsService.loadUserByUsername("mario@yumaste.it")).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("mario@yumaste.it");
    }

    @Test
    @DisplayName("doFilterInternal - token access non valido non imposta autenticazione")
    void doFilterInternal_invalidAccessToken_doesNotSetAuthentication() throws Exception {
        String jwt = "invalid.access.token";
        UserDetails userDetails = new User("mario@yumaste.it", "password", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn("mario@yumaste.it");
        when(jwtService.extractTokenType(jwt)).thenReturn("access");
        when(userDetailsService.loadUserByUsername("mario@yumaste.it")).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
