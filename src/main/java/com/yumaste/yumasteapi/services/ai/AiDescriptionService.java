package com.yumaste.yumasteapi.services.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.yumaste.yumasteapi.DTO.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.services.BoxCompositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDescriptionService {

    private final Client geminiClient;
    private final BoxRepository boxRepository;
    private final BoxCompositionService boxCompositionService;

    public String generaDescrizionePerBox(Long boxId) {
        // 1. Recupera la Box
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box non trovata con ID: " + boxId));

        // 2. Recupera la lista degli ingredienti
        List<IngredientiConValoriDTO> ingredienti = boxCompositionService.getIngredientiConValoriDellaBox(boxId);
        String nomiIngredienti = ingredienti.stream()
                .map(IngredientiConValoriDTO::nomeIngrediente)
                .collect(Collectors.joining(", "));

        // 3. Costruisci il Prompt per Gemini
        String prompt = String.format(
                "Sei un copywriter esperto in food marketing per un e-commerce di meal-kit chiamato Yumaste. " +
                        "Scrivi una descrizione breve, accattivante e invitante (massimo 50 parole) per una box chiamata '%s' " +
                        "appartenente alla categoria '%s'. " +
                        "La box è pensata per %d porzioni e contiene i seguenti ingredienti di alta qualità: %s. " +
                        "Convici il cliente ad acquistarla! Rispondi restituendo solo il testo della descrizione, senza formattazioni extra.",
                box.getNome(),
                box.getCategoria(),
                box.getPorzioni(),
                nomiIngredienti
        );

        // 4. Chiama Gemini tramite l'SDK ufficiale
        try {
            log.info("Chiamata a Gemini in corso per la box: {}", box.getNome());

            GenerateContentResponse response = geminiClient.models.generateContent(
                    "gemini-3-flash-preview",
                    prompt,
                    null
            );

            return response.text();

        } catch (Exception e) {
            log.error("Errore durante la generazione della descrizione con Gemini", e);
            throw new RuntimeException("Impossibile generare la descrizione in questo momento.", e);
        }
    }
}