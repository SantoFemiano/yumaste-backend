package com.yumaste.yumasteapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Mantenute le porte locali di React e Angular insieme ai domini di produzione Vercel
        configuration.setAllowedOrigins(List.of(
                "http://localhost:9000",
                "http://localhost:4200",
                "https://yumaste-shop-admin.vercel.app",
                "https://yumaste-shop.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Abilitazione CORS e disattivazione CSRF (essendo un'architettura stateless basata su token)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // Regole di autorizzazione delle richieste (struttura originale protetta al 100%)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()       // Endpoint di Login e Registrazione classici (liberi)
                        .requestMatchers("/api/public/**").permitAll()     // Controller pubblico per consultazione box/ingredienti
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN") // Accesso concesso a utenti e admin autenticati
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Pannello di controllo CRUD riservato esclusivamente agli admin
                        .requestMatchers("/v3/api-docs/**").permitAll()   // Endpoint OpenAPI di documentazione strutturale
                        .requestMatchers("/swagger-ui/**").permitAll()     // Interfaccia grafica di Swagger
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()       // Endpoint metriche Prometheus e monitoraggio sanitario
                        .anyRequest().authenticated()                      // Qualsiasi altra risorsa richiede esplicitamente il token
                )

                // Flusso di cattura dell'autenticazione delegata a terze parti (OAuth2 con GitHub)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)      // Carica o registra l'utente parziale nel DB MySQL
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // Genera il token JWT finale e lo trasmette al frontend
                )

                // Politica di gestione delle sessioni (Stateless: l'applicazione non memorizza lo stato lato server)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurazione dei provider di persistenza e catena dei filtri interceptor
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}