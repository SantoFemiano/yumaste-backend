package com.yumaste.yumasteapi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test di integrazione per SecurityConfig:
 * - verifica che gli endpoint pubblici siano accessibili senza token
 * - verifica che gli endpoint protetti restituiscano 401 senza token
 * - verifica la configurazione CORS
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    // -------------------------------------------------------
    // Endpoint PUBBLICI → devono rispondere senza token (non 401)
    // -------------------------------------------------------

    @Test
    @DisplayName("/api/auth/** è pubblico - non richiede autenticazione")
    void authEndpoint_isPublic() throws Exception {
        // POST su un endpoint di login inesistente restituisce 404/400/405, MAI 401
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNot(401));
    }

    @Test
    @DisplayName("/api/public/** è pubblico - non richiede autenticazione")
    void publicEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/api/public/box"))
                .andExpect(status().isNot(401));
    }

    @Test
    @DisplayName("/v3/api-docs è pubblico")
    void swaggerDocs_isPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNot(401));
    }

    @Test
    @DisplayName("/swagger-ui/** è pubblico")
    void swaggerUi_isPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isNot(401));
    }

    @Test
    @DisplayName("/error è pubblico")
    void errorEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().isNot(401));
    }

    // -------------------------------------------------------
    // Endpoint PROTETTI → devono restituire 401 senza token
    // -------------------------------------------------------

    @Test
    @DisplayName("/api/user/** restituisce 401 senza token")
    void userEndpoint_requires_authentication() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/admin/** restituisce 401 senza token")
    void adminEndpoint_requires_authentication() throws Exception {
        mockMvc.perform(get("/api/admin/utenti"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/actuator/** restituisce 401 senza token")
    void actuatorEndpoint_requires_authentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("endpoint generico restituisce 401 senza token")
    void genericEndpoint_requires_authentication() throws Exception {
        mockMvc.perform(get("/qualsiasi/risorsa"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------
    // CORS configuration
    // -------------------------------------------------------

    @Test
    @DisplayName("CorsConfigurationSource bean è presente nel contesto")
    void corsConfigurationSource_beanIsPresent() {
        assertThat(corsConfigurationSource).isNotNull();
    }

    @Test
    @DisplayName("CORS: origine localhost:9000 è permessa")
    void cors_localhost9000_isAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:9000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:9000"));
    }

    @Test
    @DisplayName("CORS: origine localhost:4200 è permessa")
    void cors_localhost4200_isAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    @DisplayName("CORS: origine Vercel prod shop è permessa")
    void cors_vercelShop_isAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://yumaste-shop.vercel.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().string("Access-Control-Allow-Origin", "https://yumaste-shop.vercel.app"));
    }

    @Test
    @DisplayName("CORS: origine Vercel prod admin è permessa")
    void cors_vercelAdmin_isAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://yumaste-shop-admin.vercel.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().string("Access-Control-Allow-Origin", "https://yumaste-shop-admin.vercel.app"));
    }

    @Test
    @DisplayName("CORS: origine non autorizzata non ottiene Allow-Origin")
    void cors_unknownOrigin_notAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://hacker.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
