package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.dto.response.IngredienteAllergeneResponseDTO;
import com.yumaste.yumasteapi.dto.response.IngredienteResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.IngredienteAllergeneMapper;
import com.yumaste.yumasteapi.mapper.IngredienteMapper;
import com.yumaste.yumasteapi.models.*;
import com.yumaste.yumasteapi.repositories.*;
import com.yumaste.yumasteapi.services.ai.AiDescriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final FornitoreRepository fornitoreRepository;
    private final IngredienteMapper ingredienteMapper;
    private final IngredienteAllergeneRepository ingredienteAllergeneRepository;
    private final IngredienteAllergeneMapper ingredienteAllergeneMapper;
    private final NutritionalValueRepository nutritionalValueRepository;
    private final AllergeneRepository allergeneRepository;
    private final AiDescriptionService aiDescriptionService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ingredienti", allEntries = true),
            @CacheEvict(value = "ingredienti_allergeni", allEntries = true),
            @CacheEvict(value = "ingredienti_inattivi", allEntries = true),
            @CacheEvict(value = {"ingredienti_con_valori", "box_dettagli"}, allEntries = true)
    })
    public IngredienteResponseDTO creaIngrediente(IngredienteRequestDTO request) {

        // 1. CREAZIONE INGREDIENTE BASE (Il mapper mappa in automatico i valori se ci sono nel JSON)
        Ingrediente nuovoIngrediente = ingredienteMapper.toEntity(request);
        Fornitore fornitore = fornitoreRepository.findByPartitaIva((request.partitaIva()))
                .orElseThrow(() -> new ResourceNotFoundException("Fornitore non trovato con Partita Iva: " + request.partitaIva()));
        nuovoIngrediente.setFornitore(fornitore);

        // 2. GESTIONE VALORI AI (Facciamo questo PRIMA di salvare)
        // Se non ci sono valori nutrizionali nel DTO (quindi getValoriNutrizionali è null)
        if (nuovoIngrediente.getValoriNutrizionali() == null) {

            var valoriGenerati = aiDescriptionService.generaValoriNutrizionali(request.nome());

            if (valoriGenerati != null) {
                ValoriNutrizionali vn = new ValoriNutrizionali();
                vn.setProteine(valoriGenerati.proteine());
                vn.setCarboidrati(valoriGenerati.carboidrati());
                vn.setZuccheri(valoriGenerati.zuccheri());
                vn.setFibre(valoriGenerati.fibre());
                vn.setGrassi(valoriGenerati.grassi());
                vn.setSale(valoriGenerati.sale());
                vn.setChilocalorie(valoriGenerati.chilocalorie());

                // Usiamo il nostro setter speciale per collegare i due oggetti!
                nuovoIngrediente.setValoriNutrizionali(vn);
            }
        }

        // 3. IL SALVATAGGIO MAGICO
        // Questo singolo comando salva l'Ingrediente e, in automatico, inserisce
        // i ValoriNutrizionali nella loro tabella senza fare duplicati.
        Ingrediente ingredienteSalvato = ingredienteRepository.save(nuovoIngrediente);

        // 4. COLLEGAMENTO ALLERGENI
        if (request.allergeniIds() != null && !request.allergeniIds().isEmpty()) {
            for (Long idAllergene : request.allergeniIds()) {
                Allergene allergene = allergeneRepository.findById(idAllergene)
                        .orElseThrow(() -> new ResourceNotFoundException("Allergene non trovato ID: " + idAllergene));

                IngredienteAllergene associazione = new IngredienteAllergene();
                associazione.setId(new IngredienteAllergeneId(ingredienteSalvato.getId(), allergene.getId()));
                associazione.setIngrediente(ingredienteSalvato);
                associazione.setAllergene(allergene);
                associazione.setTipoPresenza("PRESENTE");

                ingredienteAllergeneRepository.save(associazione);
            }
        }

        return ingredienteMapper.toResponseDTO(ingredienteSalvato);
    }

    @Cacheable(value = "ingredienti_allergeni")
    public List<IngredienteAllergeneResponseDTO> getAllIngredientiConAllergeni() {

        return ingredienteAllergeneRepository.findAllWithDetails()
                .stream().map(ingredienteAllergeneMapper::toDto)
                .toList();
    }

    @Cacheable(value = "ingredienti")
    public List<IngredienteResponseDTO> getAllIngredienti() {
        return ingredienteRepository.findByAttivoTrue()
                .stream()
                .map(ingredienteMapper::toResponseDTO)
                .toList();
    }

    @Cacheable(value = "ingredienti_inattivi")
    public List<IngredienteResponseDTO> getAllIngredientiInattivi() {
        return ingredienteRepository.findByAttivoFalse()
                .stream()
                .map(ingredienteMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ingredienti", allEntries = true),
            @CacheEvict(value = "ingredienti_allergeni", allEntries = true),
            @CacheEvict(value = "valori_nutrizionali", allEntries = true),
            @CacheEvict(value = {"ingredienti_con_valori", "box_dettagli"}, allEntries = true)
    })
    public IngredienteResponseDTO updateIngrediente(Long id, IngredienteRequestDTO request) {
        Ingrediente ingrediente = ingredienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente non trovato"));

        ingrediente.setNome(request.nome());
        ingrediente.setDescrizione(request.descrizione());
        ingrediente.setEan(request.ean());
        ingrediente.setPrezzoPerUnita(request.prezzoPerUnita());
        ingrediente.setUnitaMisura(request.unitaMisura());

        // --- LA RIGA MANCANTE: AGGIORNIAMO IL PESO PER PEZZO ---
        ingrediente.setPesoPerPezzo(request.pesoPerPezzo());

        ingrediente.setAttivo(request.attivo() != null ? request.attivo() : ingrediente.getAttivo());

        // Se la P.IVA del fornitore cambia, aggiorna la relazione
        if(!ingrediente.getFornitore().getPartitaIva().equals(request.partitaIva())) {
            Fornitore fornitore = fornitoreRepository.findByPartitaIva(request.partitaIva())
                    .orElseThrow(() -> new RuntimeException("Fornitore non trovato"));
            ingrediente.setFornitore(fornitore);
        }

        // Estrapoliamo i valori nutrizionali dalla richiesta
        var valoriRequest = request.valoriNutrizionali();


        // Se l'utente ha attivato lo switch sul frontend (inviando null), ricalcoliamo con Gemini
        if (valoriRequest == null) {
            valoriRequest = aiDescriptionService.generaValoriNutrizionali(request.nome());
        }

        if (valoriRequest != null) {
            // Cerca i vecchi valori nutrizionali nel DB. Se l'ingrediente non li aveva, ne crea uno nuovo.
            ValoriNutrizionali vn = nutritionalValueRepository.findByIngrediente(ingrediente)
                    .orElse(new ValoriNutrizionali());

            vn.setIngrediente(ingrediente);
            vn.setProteine(valoriRequest.proteine());
            vn.setCarboidrati(valoriRequest.carboidrati());
            vn.setZuccheri(valoriRequest.zuccheri());
            vn.setFibre(valoriRequest.fibre());
            vn.setGrassi(valoriRequest.grassi());
            vn.setSale(valoriRequest.sale());
            vn.setChilocalorie(valoriRequest.chilocalorie());

            // Salva le modifiche nella tabella VALORI_NUTRIZIONALI
            nutritionalValueRepository.save(vn);
        }


        return ingredienteMapper.toResponseDTO(ingredienteRepository.save(ingrediente));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = {"ingredienti", "ingredienti_allergeni", "ingredienti_inattivi"}, allEntries = true),
            @CacheEvict(value = {"ingredienti_con_valori", "box_dettagli"}, allEntries = true)
    })
    public void deleteIngrediente(Long id) {
        Ingrediente ingrediente = ingredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente non trovato"));
        ingrediente.setAttivo(false);
        ingredienteRepository.save(ingrediente);
    }
}