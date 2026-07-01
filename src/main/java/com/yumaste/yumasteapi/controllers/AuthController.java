package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.dto.request.LoginRequest;
import com.yumaste.yumasteapi.dto.request.RegisterRequest;
import com.yumaste.yumasteapi.dto.request.RefreshTokenRequest;
import com.yumaste.yumasteapi.dto.response.AuthResponse;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import com.yumaste.yumasteapi.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtenteRepository utenteRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        // 1. Controllo base: l'email esiste già?
        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Creiamo la nuova entità Utente travasando i dati dal DTO
        Utente utente = new Utente();
        utente.setCf(request.getCf());
        utente.setNome(request.getNome());
        utente.setCognome(request.getCognome());
        utente.setDataNascita(request.getDataNascita());
        utente.setTelefono(request.getTelefono());
        utente.setEmail(request.getEmail());

        // 3. CRIPTIAMO LA PASSWORD
        utente.setPasswordC(passwordEncoder.encode(request.getPassword()));

        // 4. Impostiamo i valori di default per il sistema
        utente.setRuolo("ROLE_USER");
        utente.setDataRegistrazione(Instant.now());
        utente.setDataAggiornamento(Instant.now());

        // 5. Salviamo l'utente nel database MySQL
        utenteRepository.save(utente);

        // 6. Generiamo entrambi i token JWT
        String jwtToken = jwtService.generateToken(utente);
        String refreshToken = jwtService.generateRefreshToken(utente);

        // 7. Restituiamo i token al client usando il costruttore aggiornato di AuthResponse
        return ResponseEntity.ok(new AuthResponse(jwtToken, refreshToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Diciamo a Spring Security di autenticare l'utente con l'email e la password fornite.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Recuperiamo l'utente dal database per poter generare il token.
        Utente utente = utenteRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato nel database"));

        // 3. Generiamo entrambi i token JWT usando il nostro servizio.
        String jwtToken = jwtService.generateToken(utente);
        String refreshToken = jwtService.generateRefreshToken(utente);

        // 4. Restituiamo i token al client in formato JSON.
        return ResponseEntity.ok(new AuthResponse(jwtToken, refreshToken));
    }

    // -------------------------------------------------------------------------
    // NUOVO ENDPOINT: GENERAZIONE NUOVO ACCESS TOKEN TRAMITE REFRESH TOKEN
    // -------------------------------------------------------------------------
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token mancante nella richiesta.");
        }

        try {
            // Estraiamo l'email decodificando il refresh token
            String userEmail = jwtService.extractUsername(refreshToken);

            if (userEmail != null) {
                // Cerchiamo l'utente sul DB
                Utente utente = utenteRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

                // Verifichiamo che il refresh token sia ancora valido per questo utente
                if (jwtService.isTokenValid(refreshToken, utente)) {
                    // Se è valido, generiamo un nuovo Access Token (scadenza breve)
                    String newAccessToken = jwtService.generateToken(utente);

                    // Restituiamo il nuovo access token e prolunghiamo la vita mantenendo lo stesso refresh token
                    return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
                }
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token non valido o scaduto. Effettua nuovamente il login.");
        } catch (Exception e) {
            // Se il token non è parsabile o è scaduto, JJWT lancia un'eccezione
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token scaduto o malformato. Effettua nuovamente il login.");        }
    }
}