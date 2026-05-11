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
import com.yumaste.yumasteapi.DTO.request.AiRecommendationRequestDTO;

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
                    "gemini-3.1-flash-lite",
                    prompt,
                    null
            );

            return response.text();

        } catch (Exception e) {
            log.error("Errore durante la generazione della descrizione con Gemini", e);
            throw new RuntimeException("Impossibile generare la descrizione in questo momento.", e);
        }
    }

    public String consigliaBoxIntelligente(AiRecommendationRequestDTO preferenze) {

        // 1. Recupera tutte le box disponibili dal database
        List<Box> catalogo = boxRepository.findByAttivoTrue();

        // 2. Crea una stringa riassuntiva del catalogo per Gemini
        String riassuntoCatalogo = catalogo.stream()
                .map(b -> String.format("- %s (Categoria: %s, Porzioni: %d)", b.getNome(), b.getCategoria(), b.getPorzioni()))
                .collect(Collectors.joining("\n"));

        // 3. Costruisci un Prompt potentissimo
        String prompt = String.format(
                """
                        Sei il nutrizionista virtuale e assistente alle vendite di Yumaste. \
                        Un cliente ha appena compilato il nostro questionario con queste preferenze:
                        - Obiettivo: %s
                        - Stile alimentare: %s
                        - Allergeni da evitare: %s
                        - Calorie giornaliere target: %d kcal
                        
                        Questo è il nostro catalogo di Box attualmente disponibili:
                        %s
                        
                        Analizza le richieste del cliente e scegli UNA box dal catalogo che sia perfetta per lui. \
                        Scrivi un messaggio accogliente, diretto al cliente (dandogli del tu), spiegando perché gli consigli proprio quella box. \
                        Sii persuasivo, empatico e mantieni la risposta sotto le 60 parole.""",
                preferenze.obiettivo(),
                preferenze.tipoDieta(),
                String.join(", ", preferenze.allergeni()),
                preferenze.calorieGiornaliere(),
                riassuntoCatalogo
        );

        // 4. Invia al modello
        try {
            log.info("Richiesta consiglio AI per obiettivo: {}", preferenze.obiettivo());
            GenerateContentResponse response = geminiClient.models.generateContent("gemini-3.1-flash-lite", prompt,null);
            return response.text();
        } catch (Exception e) {
            log.error("Errore durante la raccomandazione AI", e);
            return "Al momento il nostro Chef AI sta riposando, ma ti consigliamo di dare un'occhiata alle nostre box più popolari nel catalogo!";
        }
    }
}