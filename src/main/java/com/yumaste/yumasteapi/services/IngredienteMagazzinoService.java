package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.IngredienteMagazzinoRequest;
import com.yumaste.yumasteapi.dto.response.IngredienteMagazzinoResponse;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.IngredienteMagazzinoMapper;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.models.IngredienteMagazzino;
import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.repositories.IngredienteMagazzinoRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import com.yumaste.yumasteapi.repositories.MagazzinoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredienteMagazzinoService {

    private final IngredienteMagazzinoRepository ingredienteMagazzinoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final IngredienteMagazzinoMapper ingredienteMagazzinoMapper;
    private final MagazzinoRepository magazzinoRepository;

    @Transactional
    @CacheEvict(value = "ingredientiMagazzino", allEntries = true)
    public IngredienteMagazzinoResponse caricaMerci(IngredienteMagazzinoRequest request) {

        Ingrediente ingrediente = ingredienteRepository.findById(request.ingredienteId()).orElseThrow(() ->new ResourceNotFoundException("Ingrediente non trovato!"));

        Magazzino magazzino = magazzinoRepository.findById(request.magazzinoId()).orElseThrow(() -> new ResourceNotFoundException("Magazzino non trovato!"));

        Optional<IngredienteMagazzino> giacenzaEsistente = ingredienteMagazzinoRepository.findByMagazzinoAndIngredienteAndLotto(magazzino,ingrediente, request.lotto());

        IngredienteMagazzino rigaSalvata;

        if(giacenzaEsistente.isPresent()){
            IngredienteMagazzino riga = giacenzaEsistente.get();
            riga.setQuantita(riga.getQuantita().add(request.quantita()));
            rigaSalvata = ingredienteMagazzinoRepository.save(riga);
        }else{
            IngredienteMagazzino nuovaRiga = new IngredienteMagazzino();
            nuovaRiga.setMagazzino(magazzino);
            nuovaRiga.setIngrediente(ingrediente);
            nuovaRiga.setLotto(request.lotto());
            nuovaRiga.setQuantita(request.quantita());
            nuovaRiga.setDataIngresso(request.dataIngresso());
            rigaSalvata = ingredienteMagazzinoRepository.save(nuovaRiga);
        }

        return ingredienteMagazzinoMapper.toDto(rigaSalvata);

    }

    @Cacheable(value= "ingredientiMagazzino" )
    public List<IngredienteMagazzinoResponse> getAllIngredienteMagazzino(){
        return ingredienteMagazzinoRepository.findAll().stream().map(ingredienteMagazzinoMapper::toDto).toList();
    }
}
