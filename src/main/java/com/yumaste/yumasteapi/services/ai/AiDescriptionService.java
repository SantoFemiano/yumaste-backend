package com.yumaste.yumasteapi.services.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;
import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.DTO.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Fornitore;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.repositories.AllergeneRepository;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.FornitoreRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import com.yumaste.yumasteapi.services.BoxCompositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.yumaste.yumasteapi.DTO.request.AiRecommendationRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumaste.yumasteapi.DTO.response.AiRecommendationResponseDTO;
import com.yumaste.yumasteapi.DTO.request.ValoriNutrizionaliRequestDTO;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiDescriptionService {

    private final Client geminiClient;
    private final BoxRepository boxRepository;
    private final BoxCompositionService boxCompositionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IngredienteRepository ingredienteRepository;
    private final FornitoreRepository fornitoreRepository;
    private final AllergeneRepository allergeneRepository;

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

    public AiRecommendationResponseDTO consigliaBoxIntelligente(AiRecommendationRequestDTO preferenze) {
        List<Box> catalogo = boxRepository.findByAttivoTrue();

        // Includiamo l'ID nel riassunto per Gemini
        String riassuntoCatalogo = catalogo.stream()
                .map(b -> String.format("- ID: %d, Nome: %s (Categoria: %s)", b.getId(), b.getNome(), b.getCategoria()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format(
                """
                Sei il nutrizionista virtuale di Yumaste. 
                Cliente: Obiettivo %s, Dieta %s, Allergeni %s, %d kcal.
                
                Catalogo Box:
                %s
                
                Scegli la box migliore e rispondi ESCLUSIVAMENTE con un oggetto JSON valido.
                Non aggiungere testo prima o dopo il JSON.
                Formato richiesto:
                {
                  "boxId": (numero),
                  "messaggio": "(testo persuasivo sotto 60 parole)",
                  "nomeBox": "(nome della box scelta)"
                }
                """,
                preferenze.obiettivo(), preferenze.tipoDieta(),
                String.join(", ", preferenze.allergeni()), preferenze.calorieGiornaliere(),
                riassuntoCatalogo
        );

        try {
            GenerateContentResponse response = geminiClient.models.generateContent("gemini-3.1-flash-lite", prompt, null);
            String jsonResponse = response.text().trim();

            // Rimuovi eventuali blocchi di codice markdown se Gemini li include (es. ```json ... ```)
            jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();

            return objectMapper.readValue(jsonResponse, AiRecommendationResponseDTO.class);
        } catch (Exception e) {
            log.error("Errore durante la raccomandazione AI", e);
            // Ritorna un valore di fallback coerente col DTO
            return new AiRecommendationResponseDTO(null, "Al momento non riesco a connettermi, esplora il nostro catalogo!", "Catalogo");
        }
    }

    public ValoriNutrizionaliRequestDTO generaValoriNutrizionali(String nomeIngrediente) {
        String prompt = String.format(
                "Sei un nutrizionista esperto. Fornisci i valori nutrizionali medi per 100g di '%s'. " +
                        "Rispondi ESCLUSIVAMENTE con un oggetto JSON valido, senza testo aggiuntivo. " +
                        "I valori devono essere numeri o decimali (senza unità di misura). " +
                        "Usa ESATTAMENTE le seguenti chiavi: " +
                        "{\"proteine\": 0.0, \"carboidrati\": 0.0, \"zuccheri\": 0.0, \"fibre\": 0.0, \"grassi\": 0.0, \"sale\": 0.0, \"chilocalorie\": 0.0}",
                nomeIngrediente
        );

        try {
            log.info("Richiesta valori nutrizionali all'IA per l'ingrediente: {}", nomeIngrediente);
            GenerateContentResponse response = geminiClient.models.generateContent("gemini-3.1-flash-lite", prompt, null);
            String jsonResponse = response.text().trim();

            // Pulisce il testo da eventuali formattazioni markdown di Gemini
            jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();

            return objectMapper.readValue(jsonResponse, ValoriNutrizionaliRequestDTO.class);
        } catch (Exception e) {
            log.error("Errore durante la generazione dei valori nutrizionali per {}", nomeIngrediente, e);
            return null; // Restituisce null in caso di errore per evitare blocchi
        }
    }

    public List<IngredienteRequestDTO> generaIngredientiNuovi(int quantita) {
        // 1. Recupero nomi ingredienti esistenti per evitare duplicati
        List<String> nomiEsistenti = ingredienteRepository.findAll().stream()
                .map(Ingrediente::getNome)
                .collect(Collectors.toList());

        // 2. Recupero FORNITORI con Nome e P.IVA per l'associazione logica
        List<Fornitore> fornitori = fornitoreRepository.findAll();
        if (fornitori.isEmpty()) {
            throw new RuntimeException("Nessun fornitore in database. Impossibile generare ingredienti.");
        }

        // Creiamo una stringa descrittiva: "Nome Fornitore (P.IVA: 12345)"
        String listaFornitoriContesto = fornitori.stream()
                .map(f -> f.getNome() + " (P.IVA: " + f.getPartitaIva() + ")")
                .collect(Collectors.joining(", "));

        String allergeniDisponibili = allergeneRepository.findAll().stream()
                .map(a -> a.getId() + " (" + a.getNome() + ")")
                .collect(Collectors.joining(", "));

        // Prompt
        String prompt = String.format(
                "Sei un assistente esperto per l'e-commerce alimentare Yumaste. Genera un array JSON di %d nuovi ingredienti.\n\n" +
                        "REGOLE DI COERENZA:\n" +
                        "1. ACCOPPIAMENTO FORNITORE: Per ogni ingrediente che inventi, scegli il fornitore più adatto dalla seguente lista: [%s]. " +
                        "Ad esempio, se generi 'Mozzarella', scegli un fornitore il cui nome suggerisca prodotti caseari o alimentari generici.\n" +
                        "2. UNICITÀ: Non usare questi nomi già esistenti: %s.\n" +
                        "3. DATI TECNICI: Usa l'ID corretto degli allergeni da questa lista: %s.\n" +
                        "4. FORMATO E CHIAVI: Restituisci solo l'array JSON senza markdown. Usa ESATTAMENTE e SOLO le chiavi JSON fornite nell'esempio. NON aggiungere chiavi inventate come 'energia'.\n\n" +
                        "Struttura richiesta per oggetto:\n" +
                        "{\"ean\": \"13cifre\", \"partitaIva\": \"P.IVA_DEL_FORNITORE_SCELTO\", \"nome\": \"...\", \"descrizione\": \"...\", " +
                        "\"unitaMisura\": \"g\", \"pesoPerPezzo\": 0.0, \"prezzoPerUnita\": 0.0, \"attivo\": true, \"allergeniIds\": [], " +
                        "\"valoriNutrizionali\": {\"carboidrati\": 0.0, \"proteine\": 0.0, \"grassi\": 0.0, \"chilocalorie\": 0.0, \"sale\": 0.0, \"zuccheri\": 0.0, \"fibre\": 0.0}}",
                quantita,
                listaFornitoriContesto,
                nomiEsistenti.isEmpty() ? "nessuno" : String.join(", ", nomiEsistenti),
                allergeniDisponibili
        );

        try {
            log.info("Richiesta generazione ingredienti con accoppiamento fornitori intelligente...");
            GenerateContentResponse response = geminiClient.models.generateContent("gemini-3.1-flash-lite", prompt, null);
            String jsonResponse = response.text().trim().replace("```json", "").replace("```", "");

            return objectMapper.readValue(jsonResponse, new TypeReference<List<IngredienteRequestDTO>>() {});
        } catch (Exception e) {
            log.error("Errore nella generazione AI", e);
            throw new RuntimeException("Errore generazione ingredienti");
        }
    }
}

