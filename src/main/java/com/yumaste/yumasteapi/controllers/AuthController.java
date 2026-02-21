package com.yumaste.yumasteapi.controllers;
import com.yumaste.yumasteapi.DTO.request.LoginRequest;
import com.yumaste.yumasteapi.DTO.request.RegisterRequest;
import com.yumaste.yumasteapi.DTO.response.AuthResponse;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import com.yumaste.yumasteapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        // 1. Controllo base: l'email esiste già?
        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build(); // In un'app reale, qui lanceresti una Custom Exception
        }

        // 2. Creiamo la nuova entità Utente travasando i dati dal DTO
        Utente utente = new Utente();
        utente.setCf(request.getCf());
        utente.setNome(request.getNome());
        utente.setCognome(request.getCognome());
        utente.setDataNascita(request.getDataNascita());
        utente.setTelefono(request.getTelefono());
        utente.setEmail(request.getEmail());

        // 3. CRIPTIAMO LA PASSWORD! Questo è il passaggio vitale.
        utente.setPasswordC(passwordEncoder.encode(request.getPassword()));

        // 4. Impostiamo i valori di default per il sistema
        utente.setRuolo("ROLE_USER");
        utente.setDataRegistrazione(Instant.now());
        utente.setDataAggiornamento(Instant.now());

        // 5. Salviamo l'utente nel database MySQL
        utenteRepository.save(utente);

        // 6. Generiamo subito un token JWT, così l'utente risulta già loggato dopo la registrazione
        String jwtToken = jwtService.generateToken(utente);

        // 7. Restituiamo il token al client
        return ResponseEntity.ok(new AuthResponse(jwtToken));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Diciamo a Spring Security di autenticare l'utente con l'email e la password fornite.
        // Se la password è sbagliata o l'email non esiste, Spring lancerà un'eccezione (403 Forbidden o 401 Unauthorized) automaticamente.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Se arriviamo a questa riga, significa che l'autenticazione è andata a buon fine.
        // Recuperiamo l'utente dal database per poter generare il token.
        Utente utente = utenteRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato nel database"));

        // 3. Generiamo il token JWT usando il nostro servizio.
        String jwtToken = jwtService.generateToken(utente);

        // 4. Restituiamo il token al client in formato JSON.
        return ResponseEntity.ok(new AuthResponse(jwtToken));
    }
}