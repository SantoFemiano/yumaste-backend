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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Questi due componenti li creeremo a breve, Spring li inietterà in automatico grazie a Lombok
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disabilitiamo CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Definiamo le regole di accesso agli endpoint
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()       // Login e Registrazione liberi
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/user/**").hasRole("USER") // Solo user autenticati
                        .requestMatchers("/api/user/**").hasRole("ADMIN") // Solo user autenticati
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Solo amministratori

                        .anyRequest().authenticated()                      // Tutto il resto richiede il token
                )

                // 3. Gestione della Sessione (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Aggiungiamo il nostro Provider e il Filtro JWT
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}