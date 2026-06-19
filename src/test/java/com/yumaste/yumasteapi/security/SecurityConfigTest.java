package com.yumaste.yumasteapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitari per SecurityConfig:
 * - verifica la configurazione CORS (origini, metodi, header)
 * - verifica che il bean CorsConfigurationSource sia configurato correttamente
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {



    @InjectMocks
    private SecurityConfig securityConfig;

    private CorsConfigurationSource corsSource;

    @BeforeEach
    void setUp() {
        corsSource = securityConfig.corsConfigurationSource();
    }

    // -------------------------------------------------------
    // Bean non nullo
    // -------------------------------------------------------

    @Test
    @DisplayName("corsConfigurationSource - il bean non e' null")
    void corsConfigurationSource_isNotNull() {
        assertThat(corsSource).isNotNull();
    }

    // -------------------------------------------------------
    // Origini permesse
    // -------------------------------------------------------


// -------------------------------------------------------
    // Origini permesse
    // -------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:9000",
            "http://localhost:4200",
            "https://yumaste-shop.vercel.app",
            "https://yumaste-shop-admin.vercel.app"
    })
    @DisplayName("CORS - origini permesse configurate correttamente")
    void cors_allowedOrigins(String allowedOrigin) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login"); // URI arbitraria, coperta dal pattern /**

        CorsConfiguration config = corsSource.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).contains(allowedOrigin);
    }


    @Test
    @DisplayName("CORS - hacker.com non e' un'origine permessa")
    void cors_unknownOrigin_notAllowed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        CorsConfiguration config = corsSource.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).doesNotContain("https://hacker.com");
    }

    // -------------------------------------------------------
    // Metodi HTTP permessi
    // -------------------------------------------------------

    @Test
    @DisplayName("CORS - i metodi GET, POST, PUT, DELETE, OPTIONS, PATCH sono permessi")
    void cors_allowedMethods() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        CorsConfiguration config = corsSource.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    }

    // -------------------------------------------------------
    // Header permessi
    // -------------------------------------------------------

    @Test
    @DisplayName("CORS - gli header Authorization e Content-Type sono permessi")
    void cors_allowedHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        CorsConfiguration config = corsSource.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedHeaders())
                .containsExactlyInAnyOrder("Authorization", "Content-Type");
    }

    // -------------------------------------------------------
    // Credentials
    // -------------------------------------------------------

    @Test
    @DisplayName("CORS - allowCredentials e' true")
    void cors_allowCredentials_isTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        CorsConfiguration config = corsSource.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowCredentials()).isTrue();
    }

    // -------------------------------------------------------
    // Copertura wildcard path
    // -------------------------------------------------------

    @Test
    @DisplayName("CORS - la configurazione si applica a qualsiasi path (/**)")
    void cors_appliesTo_allPaths() {
        MockHttpServletRequest requestUser = new MockHttpServletRequest();
        requestUser.setRequestURI("/api/user/profile");

        MockHttpServletRequest requestAdmin = new MockHttpServletRequest();
        requestAdmin.setRequestURI("/api/admin/dashboard");

        CorsConfiguration configUser = corsSource.getCorsConfiguration(requestUser);
        CorsConfiguration configAdmin = corsSource.getCorsConfiguration(requestAdmin);

        assertThat(configUser).isNotNull();
        assertThat(configAdmin).isNotNull();
        assertThat(configUser.getAllowedOrigins()).isEqualTo(configAdmin.getAllowedOrigins());
    }
}
