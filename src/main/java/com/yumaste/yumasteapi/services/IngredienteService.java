package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.DTO.response.IngredienteResponseDTO;
import com.yumaste.yumasteapi.mapper.IngredienteMapper;
import com.yumaste.yumasteapi.models.Fornitore;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.repositories.FornitoreRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final FornitoreRepository fornitoreRepository;
    private final IngredienteMapper ingredienteMapper;

    public IngredienteResponseDTO creaIngrediente(IngredienteRequestDTO request) {

        // 1. Convertiamo i dati base
        Ingrediente nuovoIngrediente = ingredienteMapper.toEntity(request);

        // 2. Cerchiamo il fornitore dal DB. Se non esiste, blocchiamo tutto!
        Fornitore fornitore = fornitoreRepository.findById(request.fornitoreId())
                .orElseThrow(() -> new RuntimeException("Fornitore non trovato con ID: " + request.fornitoreId()));

        // 3. Colleghiamo il fornitore all'ingrediente
        nuovoIngrediente.setFornitore(fornitore);

        // 4. Salviamo nel database
        Ingrediente ingredienteSalvato = ingredienteRepository.save(nuovoIngrediente);

        // 5. Restituiamo il DTO di risposta
        return ingredienteMapper.toResponseDTO(ingredienteSalvato);
    }
}