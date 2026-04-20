package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.DTO.response.IngredienteAllergeneResponseDTO;
import com.yumaste.yumasteapi.DTO.response.IngredienteResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.IngredienteAllergeneMapper;
import com.yumaste.yumasteapi.mapper.IngredienteMapper;
import com.yumaste.yumasteapi.models.*;
import com.yumaste.yumasteapi.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
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

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ingredienti", allEntries = true),
            @CacheEvict(value = "ingredienti_allergeni", allEntries = true),
            @CacheEvict(value = "ingredienti_inattivi", allEntries = true),
            @CacheEvict(value = {"ingredienti_con_valori", "box_dettagli"}, allEntries = true)
    })
    public IngredienteResponseDTO creaIngrediente(IngredienteRequestDTO request) {

        // 1. SALVATAGGIO INGREDIENTE BASE
        Ingrediente nuovoIngrediente = ingredienteMapper.toEntity(request);
        Fornitore fornitore = fornitoreRepository.findByPartitaIva((request.partitaIva())).orElseThrow(() -> new ResourceNotFoundException("Fornitore non trovato con Partita Iva: " + request.partitaIva()));
        nuovoIngrediente.setFornitore(fornitore);
        Ingrediente ingredienteSalvato = ingredienteRepository.save(nuovoIngrediente);

        // 2. SALVATAGGIO VALORI NUTRIZIONALI
        if (request.valoriNutrizionali() != null) {
            ValoriNutrizionali vn = new ValoriNutrizionali();
            vn.setIngrediente(ingredienteSalvato);
            vn.setProteine(request.valoriNutrizionali().proteine());
            vn.setCarboidrati(request.valoriNutrizionali().carboidrati());
            vn.setZuccheri(request.valoriNutrizionali().zuccheri());
            vn.setFibre(request.valoriNutrizionali().fibre());
            vn.setGrassi(request.valoriNutrizionali().grassi());
            vn.setSale(request.valoriNutrizionali().sale());
            vn.setChilocalorie(request.valoriNutrizionali().chilocalorie());

            nutritionalValueRepository.save(vn);
        }

        // 3. COLLEGAMENTO ALLERGENI
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


        if (request.valoriNutrizionali() != null) {
            // Cerca i vecchi valori nutrizionali nel DB. Se l'ingrediente non li aveva, ne crea uno nuovo.
            ValoriNutrizionali vn = nutritionalValueRepository.findByIngrediente(ingrediente)
                    .orElse(new ValoriNutrizionali());

            vn.setIngrediente(ingrediente);
            vn.setProteine(request.valoriNutrizionali().proteine());
            vn.setCarboidrati(request.valoriNutrizionali().carboidrati());
            vn.setZuccheri(request.valoriNutrizionali().zuccheri());
            vn.setFibre(request.valoriNutrizionali().fibre());
            vn.setGrassi(request.valoriNutrizionali().grassi());
            vn.setSale(request.valoriNutrizionali().sale());
            vn.setChilocalorie(request.valoriNutrizionali().chilocalorie());

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