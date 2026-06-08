package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.AddIngredienteToBoxRequestDTO;
import com.yumaste.yumasteapi.dto.response.BoxIngredientDTO;
import com.yumaste.yumasteapi.dto.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.BoxCompositionMapper;
import com.yumaste.yumasteapi.mapper.DettaglioBoxMapper;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.ComposizioneBox;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.models.ValoriNutrizionali;
import com.yumaste.yumasteapi.repositories.BoxCompositionRepository;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import com.yumaste.yumasteapi.repositories.NutritionalValueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoxCompositionService {
    private final  BoxCompositionRepository boxCompositionRepository;
    private final BoxCompositionMapper boxCompositionMapper;
    private final BoxRepository boxRepository;
    private final IngredienteRepository ingredienteRepository;
    private final NutritionalValueRepository nutritionalValueRepository;
    private final DettaglioBoxMapper dettaglioBoxMapper;


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ingredienti_box", key = "#boxId"),
            @CacheEvict(value = "ingredienti_con_valori", key = "#boxId"),
            @CacheEvict(value = "box_dettagli", key = "#boxId")
    })
    public BoxIngredientDTO addBoxIngredient(Long boxId, AddIngredienteToBoxRequestDTO request) {
        Box box = boxRepository.findById(boxId).orElseThrow(() -> new RuntimeException("Box non trovato con id: " + boxId));

        Ingrediente ingrediente = ingredienteRepository.findById(request.ingredienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente non trovato con id: " + request.ingredienteId()));
        Optional<ComposizioneBox> composizioneEsistente = boxCompositionRepository.findByBoxAndIngrediente(box, ingrediente);
        ComposizioneBox composizione;
        if(composizioneEsistente.isPresent()){
            composizione = composizioneEsistente.get();
            composizione.setQuantita(request.quantita());
            log.info("Aggiornata quantità ingrediente {} nella Box {}", ingrediente.getNome(), box.getNome());
    }else{
            composizione = new ComposizioneBox();
            composizione.setBox(box);
            composizione.setIngrediente(ingrediente);
            composizione.setQuantita(request.quantita());
            log.info("Aggiunto nuovo ingrediente {} alla Box {}", ingrediente.getNome(), box.getNome());

        }
        ComposizioneBox salvato = boxCompositionRepository.save(composizione);
        return boxCompositionMapper.toDto(salvato);
    }


    @Cacheable(value ="ingredienti_box",key = "#boxId")
    public List<BoxIngredientDTO> getBoxIngredients(Long boxId) {
        Box box = boxRepository.findById(boxId).orElseThrow(() -> new ResourceNotFoundException("Box non trovato con id: " + boxId));
        List<ComposizioneBox> composizioni = boxCompositionRepository.findByBox(box);
        return composizioni.stream().map(boxCompositionMapper::toDto).toList();
    }

    @Cacheable(value ="ingredienti_con_valori",key = "#boxId")
    public List<IngredientiConValoriDTO> getIngredientiConValoriDellaBox(Long boxId) {

        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new ResourceNotFoundException("Box non trovata con ID: " + boxId));

        List<ComposizioneBox> composizioni = boxCompositionRepository.findByBox(box);

        //Estraggo la lista degli ingredienti presenti in questa box
        List<Ingrediente> ingredientiDellaBox = composizioni.stream()
                .map(ComposizioneBox::getIngrediente)
                .toList();

        //Chiedo TUTTI i valori nutrizionali
        List<ValoriNutrizionali> tuttiIValori = nutritionalValueRepository.findByIngredienteIn(ingredientiDellaBox);

        List<IngredientiConValoriDTO> risultati = new ArrayList<>();

        for (ComposizioneBox cb : composizioni) {
            //Cerco il valore nutrizionale nella lista che ho salvato in memoria (tuttiIValori)
            ValoriNutrizionali valori = tuttiIValori.stream()
                    .filter(v -> v.getIngrediente().getId().equals(cb.getIngrediente().getId()))
                    .findFirst()
                    .orElse(null);

            IngredientiConValoriDTO dtoCalcolato = dettaglioBoxMapper.toDtoCalcolato(cb, valori);
            risultati.add(dtoCalcolato);
        }

        return risultati;
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ingredienti_box", key = "#boxId"),
            @CacheEvict(value = "ingredienti_con_valori", key = "#boxId")
    })
    public void removeIngredienteFromBox(Long boxId, Long ingredienteId) {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new ResourceNotFoundException("Box non trovata"));
        Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente non trovato"));

        ComposizioneBox composizione = boxCompositionRepository.findByBoxAndIngrediente(box, ingrediente)
                .orElseThrow(() -> new ResourceNotFoundException("Associazione non trovata"));

        boxCompositionRepository.delete(composizione);
    }



}
