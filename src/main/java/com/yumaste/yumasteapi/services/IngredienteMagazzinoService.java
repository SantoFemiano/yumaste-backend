package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.IngredienteMagazzinoRequest;
import com.yumaste.yumasteapi.DTO.response.IngredienteMagazzinoResponse;
import com.yumaste.yumasteapi.mapper.IngredienteMagazzinoMapper;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.models.IngredienteMagazzino;
import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.repositories.IngredienteMagazzinoRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import com.yumaste.yumasteapi.repositories.MagazzinoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public IngredienteMagazzinoResponse caricaMerci(IngredienteMagazzinoRequest request) {

        Ingrediente ingrediente = ingredienteRepository.findById(request.ingredienteId()).orElseThrow(() ->new RuntimeException("Ingrediente non trovato!"));

        Magazzino magazzino = magazzinoRepository.findById(request.magazzinoId()).orElseThrow(() -> new RuntimeException("Magazzino non trovato!"));

        Optional<IngredienteMagazzino> giacenzaesistente = ingredienteMagazzinoRepository.findByMagazzinoAndIngredienteAndLotto(magazzino,ingrediente, request.lotto());

        IngredienteMagazzino riga_salvata;

        if(giacenzaesistente.isPresent()){
            IngredienteMagazzino riga = giacenzaesistente.get();
            riga.setQuantita(riga.getQuantita().add(request.quantita()));
            riga_salvata = ingredienteMagazzinoRepository.save(riga);
        }else{
            IngredienteMagazzino nuovaRiga = new IngredienteMagazzino();
            nuovaRiga.setMagazzino(magazzino);
            nuovaRiga.setIngrediente(ingrediente);
            nuovaRiga.setLotto(request.lotto());
            nuovaRiga.setQuantita(request.quantita());
            nuovaRiga.setDataIngresso(request.dataIngresso());
            riga_salvata = ingredienteMagazzinoRepository.save(nuovaRiga);
        }

        return ingredienteMagazzinoMapper.ToDto(riga_salvata);

    }


    public List<IngredienteMagazzinoResponse> getAllIngredienteMagazzino(){
        return ingredienteMagazzinoRepository.findAll().stream().map(ingredienteMagazzinoMapper::ToDto).toList();
    }
}
