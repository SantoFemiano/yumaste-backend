package com.yumaste.yumasteapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.TestSecurityController.class)
@Import({SecurityConfig.class, SecurityConfigTest.FilterTestConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock delle dipendenze richieste da SecurityConfig
    @MockitoBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;
    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    // Mock di JwtService e UserDetailsService per soddisfare le dipendenze
    // di JwtAuthenticationFilter durante il caricamento del contesto Spring
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    /**
     * Fornisce un JwtAuthenticationFilter pass-through che non esegue nessuna
     * logica JWT, lasciando che Spring Security gestisca le sue regole di
     * autorizzazione normalmente nei test.
     */
    @TestConfiguration
    static class FilterTestConfig {
        @Bean
        public JwtAuthenticationFilter jwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
            return new JwtAuthenticationFilter(jwtService, userDetailsService) {
                @Override
                protected void doFilterInternal(
                        @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response,
                        @NonNull FilterChain filterChain
                ) throws ServletException, IOException {
                    // Pass-through: non valida il JWT, lascia decidere Spring Security
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    // =========================================================================
    // TESTS: Rotte Pubbliche
    // =========================================================================

    @Test
    @DisplayName("Path /api/auth/** e /api/public/** devono essere accessibili a tutti senza token")
    void publicEndpoints_ShouldBePermittedAll() throws Exception {
        mockMvc.perform(get("/api/auth/test-all"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/public/test-all"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // TESTS: Rotte Utente protette (/api/user/**)
    // =========================================================================

    @Test
    @DisplayName("Path /api/user/** deve rifiutare gli utenti anonimi con un errore 401")
    void userEndpoint_ShouldRejectAnonymous() throws Exception {
        mockMvc.perform(get("/api/user/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Path /api/user/** deve consentire l'accesso ad utenti autenticati con ruolo USER")
    void userEndpoint_ShouldAllowUserRole() throws Exception {
        mockMvc.perform(get("/api/user/test"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Path /api/user/** deve consentire l'accesso ad utenti autenticati con ruolo ADMIN")
    void userEndpoint_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(get("/api/user/test"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // TESTS: Rotte Amministrative protette (/api/admin/**)
    // =========================================================================

    @Test
    @DisplayName("Path /api/admin/** deve rifiutare gli utenti anonimi con un errore 401")
    void adminEndpoint_ShouldRejectAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Path /api/admin/** deve rifiutare l'accesso con 403 Forbidden se l'utente ha solo ruolo USER")
    void adminEndpoint_ShouldRejectUserRole() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Path /api/admin/** deve consentire l'accesso ad utenti con ruolo ADMIN")
    void adminEndpoint_ShouldAllowAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // Controller Fittizio di Supporto
    // =========================================================================
    @RestController
    static class TestSecurityController {
        @GetMapping("/api/auth/test-all")
        public String authTest() { return "free"; }

        @GetMapping("/api/public/test-all")
        public String publicTest() { return "free"; }

        @GetMapping("/api/user/test")
        public String userTest() { return "secured-user"; }

        @GetMapping("/api/admin/test")
        public String adminTest() { return "secured-admin"; }
    }
}
