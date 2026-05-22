package com.yumaste.yumasteapi.services.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.yumaste.yumasteapi.DTO.request.AiGenerateBoxRequestDTO;
import com.yumaste.yumasteapi.DTO.response.AiGenerateBoxResponseDTO;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBoxGenerationService {

    private final Client geminiClient;
    private final BoxRepository boxRepository;
    private final IngredienteRepository ingredienteRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiGenerateBoxResponseDTO generaBoxAutomatica(AiGenerateBoxRequestDTO request) {
        // 1. Recupero contesto: Evitare duplicati
        String boxEsistenti = boxRepository.findAll().stream()
                .map(Box::getNome)
                .collect(Collectors.joining(", "));

        // 2. Recupero contesto: Ingredienti realmente disponibili in magazzino
        String ingredientiDisponibili = ingredienteRepository.findAll().stream()
                .map(i -> String.format("{id: %d, nome: '%s', unita: '%s'}", i.getId(), i.getNome(), i.getUnitaMisura()))
                .collect(Collectors.joining(",\n"));

        if (ingredientiDisponibili.isEmpty()) {
            throw new RuntimeException("Nessun ingrediente in database per comporre la box.");
        }

        // 3. Prompt Engineering Avanzato per rispetto dei vincoli
        String prompt = String.format("""
            Sei lo Chef Executive e Product Manager di Yumaste. Crea una NUOVA Box alimentare.
            Suggerimento utente (se presente): %s
            
            VINCOLI DA RISPETTARE SCRUPOLOSAMENTE:
            1. UNICITÀ: Non creare nulla di simile a queste box già a catalogo: [%s].
            2. CATEGORIE CONSENTITE: Scegli una tra: "Carne", "Pesce", "Vegano", "Vegetariano", "Gluten Free".
            3. PREZZO E PORZIONI: Scegli un numero di porzioni logico (es. 2 o 4) e un prezzo di vendita sensato (es. tra 15.00 e 40.00).
            4. IMMAGINE: Genera un URL placeholder realistico descrittivo in questo formato: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=800&auto=format&fit=crop" (puoi variare l'ID se conosci immagini di cibo, o usare un placeholder semantico).
            5. INGREDIENTI (CRITICO): Puoi usare ESCLUSIVAMENTE gli ingredienti in questa lista:
            [%s]
            6. QUANTITÀ: Assegna una quantità esatta e sensata per ogni ingrediente basandoti sulla sua unità di misura ('kg', 'l', 'pz', 'g') e sul numero di porzioni della box.
            
            Devi rispondere SOLO ed ESCLUSIVAMENTE con un oggetto JSON valido. Niente markdown, niente spiegazioni testuali.
            
            Esempio di struttura JSON attesa:
            {
              "nome": "Trancio di Salmone agli Agrumi",
              "descrizione": "Una box fresca e deliziosa...",
              "categoria": "Pesce",
              "prezzo": 24.50,
              "porzioni": 2,
              "urlImmagine": "https://images.unsplash.com/...",
              "ingredienti": [
                { "ingredienteId": 12, "quantita": 0.4 },
                { "ingredienteId": 5, "quantita": 0.05 }
              ]
            }
            """,
                request.suggerimentoOpzionale() != null ? request.suggerimentoOpzionale() : "Nessuno. Inventa la migliore box possibile.",
                boxEsistenti.isEmpty() ? "Nessuna box presente" : boxEsistenti,
                ingredientiDisponibili
        );

        try {
            log.info("Richiesta generazione Box all'IA in corso...");
            GenerateContentResponse response = geminiClient.models.generateContent("gemini-3.1-flash-lite", prompt, null);

            // Pulizia standard per estrarre il JSON puro
            String jsonResponse = response.text().trim().replace("```json", "").replace("```", "");

            return objectMapper.readValue(jsonResponse, AiGenerateBoxResponseDTO.class);
        } catch (Exception e) {
            log.error("Errore fatale nella generazione della Box tramite IA", e);
            throw new RuntimeException("Impossibile generare la Box con l'IA al momento.", e);
        }
    }
}