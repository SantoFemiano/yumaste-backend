package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.IndirizzoRequestDTO;

import com.yumaste.yumasteapi.DTO.response.IndirizzoResponseDTO;
import com.yumaste.yumasteapi.DTO.response.UtenteProfileDTO;
import com.yumaste.yumasteapi.mapper.IndirizzoMapper;
import com.yumaste.yumasteapi.models.IndirizzoUtente;
import com.yumaste.yumasteapi.models.Utente;
import com.yumaste.yumasteapi.repositories.IndirizzoUtenteRepository;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UtenteRepository utenteRepository;
    private final IndirizzoUtenteRepository indirizzoUtenteRepository;
    private final IndirizzoMapper indirizzoMapper;

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



}