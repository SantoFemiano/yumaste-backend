package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.AddIngredienteToBoxRequestDTO;
import com.yumaste.yumasteapi.DTO.response.BoxIngredientDTO;
import com.yumaste.yumasteapi.DTO.response.IngredientiConValoriDTO;
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
    public BoxIngredientDTO addBoxIngredient(Long boxId, AddIngredienteToBoxRequestDTO request) {
        Box box = boxRepository.findById(boxId).orElseThrow(() -> new RuntimeException("Box non trovato con id: " + boxId));

        Ingrediente ingrediente = ingredienteRepository.findById(request.ingredienteId())
                .orElseThrow(() -> new RuntimeException("Ingrediente non trovato con id: " + request.ingredienteId()));
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

    public List<BoxIngredientDTO> getBoxIngredients(Long boxId) {
        Box box = boxRepository.findById(boxId).orElseThrow(() -> new RuntimeException("Box non trovato con id: " + boxId));
        List<ComposizioneBox> composizioni = boxCompositionRepository.findByBox(box);
        return composizioni.stream().map(boxCompositionMapper::toDto).toList();
    }

    public List<IngredientiConValoriDTO> getIngredientiConValoriDellaBox(Long boxId) {

        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box non trovata con ID: " + boxId));

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
}
