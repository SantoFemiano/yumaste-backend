package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.DTO.response.IngredienteAllergeneResponseDTO;
import com.yumaste.yumasteapi.DTO.response.IngredienteResponseDTO;
import com.yumaste.yumasteapi.mapper.IngredienteAllergeneMapper;
import com.yumaste.yumasteapi.mapper.IngredienteMapper;
import com.yumaste.yumasteapi.models.*;
import com.yumaste.yumasteapi.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public IngredienteResponseDTO creaIngrediente(IngredienteRequestDTO request) {

        // 1. SALVATAGGIO INGREDIENTE BASE
        Ingrediente nuovoIngrediente = ingredienteMapper.toEntity(request);
        Fornitore fornitore = fornitoreRepository.findByPartitaIva((request.partitaIva())).orElseThrow(() -> new RuntimeException("Fornitore non trovato con Partita Iva: " + request.partitaIva()));
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
                        .orElseThrow(() -> new RuntimeException("Allergene non trovato ID: " + idAllergene));

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

    public List<IngredienteAllergeneResponseDTO> getAllIngredientiConAllergeni() {

        return ingredienteAllergeneRepository.findAllWithDetails()
                .stream().map(ingredienteAllergeneMapper::toDto)
                .toList();
    }

    public List<IngredienteResponseDTO> getAllIngredienti() {
        return ingredienteRepository.findAll()
                .stream()
                .map(ingredienteMapper::toResponseDTO)
                .toList();
    }

}