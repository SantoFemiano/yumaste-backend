package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.CambioPasswordDTO;
import com.yumaste.yumasteapi.DTO.request.IndirizzoRequestDTO;

import com.yumaste.yumasteapi.DTO.request.UserUpdateDTO;
import com.yumaste.yumasteapi.DTO.response.IndirizzoResponseDTO;
import com.yumaste.yumasteapi.DTO.response.UtenteAggDTO;
import com.yumaste.yumasteapi.DTO.response.UtenteProfileDTO;
import com.yumaste.yumasteapi.mapper.IndirizzoMapper;
import com.yumaste.yumasteapi.mapper.UtenteMapper;
import com.yumaste.yumasteapi.models.IndirizzoUtente;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.IndirizzoUtenteRepository;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UtenteRepository utenteRepository;
    private final IndirizzoUtenteRepository indirizzoUtenteRepository;
    private final IndirizzoMapper indirizzoMapper;
    private final UtenteMapper utenteMapper;
    private final PasswordEncoder passwordEncoder;


    // Metodo privato di supporto per estrarre l'utente in modo sicuro
    private Utente getUtenteLoggato(String email) {
        return utenteRepository.findByEmail(email) // Richiede che nel UtenteRepository ci sia findByEmail
                .orElseThrow(() -> new RuntimeException("Utente non trovato nel database"));
    }

    public UtenteProfileDTO getProfilo(String email) {
        Utente utente = getUtenteLoggato(email);
        List<IndirizzoResponseDTO> indirizziAttivi = getIndirizziAttivi(email);

        return new UtenteProfileDTO(
                utente.getId(),
                utente.getNome(),
                utente.getCognome(),
                utente.getEmail(),
                utente.getCf(),
                indirizziAttivi
        );
    }


    public List<UtenteProfileDTO> getClienti() {
        List<Utente> clienti = utenteRepository.findAll();

        return clienti.stream().map(utente -> {
            List<IndirizzoResponseDTO> indirizzi = indirizzoUtenteRepository.findByUtente(utente)
                    .stream()
                    .map(indirizzoMapper::toDTO)
                    .toList();

            return new UtenteProfileDTO(
                    utente.getId(),
                    utente.getNome(),
                    utente.getCognome(),
                    utente.getEmail(),
                    utente.getCf(),
                    indirizzi
            );
        }).toList();
    }

    public IndirizzoResponseDTO aggiungiIndirizzo(String email, IndirizzoRequestDTO request) {
        Utente utente = getUtenteLoggato(email);


        IndirizzoUtente nuovoIndirizzo = indirizzoMapper.toEntity(request);
        nuovoIndirizzo.setUtente(utente);
        nuovoIndirizzo.setStato("attivo");


        IndirizzoUtente salvato = indirizzoUtenteRepository.save(nuovoIndirizzo);
        return indirizzoMapper.toDTO(salvato);
    }

    public List<IndirizzoResponseDTO> getIndirizziAttivi(String email) {
        Utente utente = getUtenteLoggato(email);

        return indirizzoUtenteRepository.findByUtenteAndStato(utente, "attivo")
                .stream()
                .map(indirizzoMapper::toDTO)
                .toList();
    }

    public UtenteAggDTO putProfile(Utente utente , UserUpdateDTO request) {
        Utente utentecorrente = getUtenteLoggato(utente.getEmail());

        utentecorrente.setEmail(request.email());
        utentecorrente.setNome(request.nome());
        utentecorrente.setCognome(request.cognome());


        Utente nuovoutente =  utenteRepository.save(utentecorrente);
        return utenteMapper.toDto(nuovoutente);

    }

    public UtenteAggDTO putProfilePass(Utente utente, CambioPasswordDTO request) {

        Utente utentecorrente = getUtenteLoggato(utente.getEmail());

        // 1. CONTROLLO SICUREZZA: La vecchia password coincide?
        // Usa il getter corretto della password della tua entità (es. getPassword() o getPasswordC())
        if (!passwordEncoder.matches(request.vecchiaPassword(), utentecorrente.getPassword())) {
            throw new IllegalArgumentException("La vecchia password non è corretta");
        }

        // 2. Se è corretta, criptiamo la nuova e la salviamo
        // Nota: ho lasciato setPasswordC come avevi scritto tu, assicurati sia il nome giusto!
        utentecorrente.setPasswordC(passwordEncoder.encode(request.nuovaPassword()));

        return utenteMapper.toDto(utenteRepository.save(utentecorrente));
    }




}