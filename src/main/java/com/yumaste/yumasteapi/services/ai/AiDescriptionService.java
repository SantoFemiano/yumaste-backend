package com.yumaste.yumasteapi.services.ai;

import com.yumaste.yumasteapi.DTO.request.ValoriNutrizionaliRequestDTO;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.ComposizioneBox;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.BoxCompositionRepository;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiDescriptionService {

    @Autowired
    private YumasteMarketingAgent marketingAgent;

    @Autowired
    private BoxRepository boxRepository;

    @Autowired
    private BoxCompositionRepository boxCompositionRepository; // Collegamento al repository della tabella di associazione

    public String generaDescrizionePerBox(Long boxId) {
        // 1. Verifichiamo che la box esista a sistema
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new ResourceNotFoundException("Box non trovata"));

        // 2. Recuperiamo tutte le righe di scomposizione associate a questa specifica box
        List<ComposizioneBox> composizioni = boxCompositionRepository.findByBoxId(boxId);

        // 3. Estraiamo il nome di ciascun ingrediente dalla relazione intermedia
        String ingredienti = composizioni.stream()
                .map(c -> c.getIngrediente().getNome())
                .collect(Collectors.joining(", "));

        // 4. Passiamo i dati puliti all'Agente LangChain4j per la stesura del testo
        return marketingAgent.generaDescrizioneBox(box.getNome(), ingredienti, 60);
    }

    public String generaDescrizionePerIngrediente(String nomeIngrediente) {
        return marketingAgent.generaDescrizioneIngrediente(nomeIngrediente);
    }

    public ValoriNutrizionaliRequestDTO generaValoriNutrizionali(String nomeIngrediente) {
        return marketingAgent.stimaValoriNutrizionali(nomeIngrediente);
    }
}
