package com.yumaste.yumasteapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Estraggo l'header Authorization dalla richiesta
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Controllo se l'header c'è e se inizia con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Se non c'è, passa la palla al prossimo filtro (es. se è una rotta pubblica)
            return;
        }

        // 3. Estraggo il token (salto i primi 7 caratteri: "Bearer ")
        jwt = authHeader.substring(7);

        // 4. Estraggo l'email dal token (ci serve il JwtService per farlo)
        userEmail = jwtService.extractUsername(jwt);

        // 5. Se l'email c'è, ma l'utente non è ancora "loggato" nel contesto attuale di questa specifica richiesta
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Vado a pescare i dettagli dell'utente dal DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Chiedo al JwtService se il token è valido per questo utente (e se non è scaduto)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Creo l'oggetto di autenticazione ufficiale per Spring
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // Qui ci sono i famosi RUOLI!
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. Dico a Spring: "Ok, l'utente è autenticato e sicuro. Salvalo nel contesto di questa richiesta."
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Finito il mio lavoro, passo la richiesta al controller
        filterChain.doFilter(request, response);
    }
}