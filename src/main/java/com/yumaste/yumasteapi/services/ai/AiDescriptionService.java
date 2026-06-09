package com.yumaste.yumasteapi.services.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumaste.yumasteapi.dto.request.AiRecommendationRequestDTO;
import com.yumaste.yumasteapi.dto.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.dto.request.ValoriNutrizionaliRequestDTO;
import com.yumaste.yumasteapi.dto.response.AiRecommendationResponseDTO;
import com.yumaste.yumasteapi.dto.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.exceptions.BusinessException;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.DettaglioOrdine;
import com.yumaste.yumasteapi.models.Fornitore;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.repositories.*;
import com.yumaste.yumasteapi.services.BoxCompositionService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDescriptionService {

    private final ChatLanguageModel chatLanguageModel;
    private final BoxRepository boxRepository;
    private final BoxCompositionService boxCompositionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IngredienteRepository ingredienteRepository;
    private final FornitoreRepository fornitoreRepository;
    private final AllergeneRepository allergeneRepository;
    private final DettaglioOrdineRepository dettaglioOrdineRepository;

    private static final String JSON_BLOCK_START = "```json";
    private static final String MARKDOWN_FENCE = "```";

    public String generaDescrizionePerBox(Long boxId) {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new ResourceNotFoundException("Box non trovata con ID: " + boxId));

        List<IngredientiConValoriDTO> ingredienti = boxCompositionService.getIngredientiConValoriDellaBox(boxId);
        String nomiIngredienti = ingredienti.stream()
                .map(IngredientiConValoriDTO::nomeIngrediente)
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "Sei un copywriter esperto in food marketing per un e-commerce di meal-kit chiamato Yumaste. " +
                        "Scrivi una descrizione breve, accattivante e invitante (massimo 50 parole) per una box chiamata '%s' " +
                        "appartenente alla categoria '%s'. " +
                        "La box è pensata per %d porzioni e contiene i seguenti ingredienti di alta qualità: %s. " +
                        "Convici il cliente ad acquistarla! Rispondi restituendo solo il testo della descrizione, senza formattazioni extra.",
                box.getNome(), box.getCategoria(), box.getPorzioni(), nomiIngredienti
        );

        try {
            log.info("Chiamata al modello AI per la box: {}", box.getNome());
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            log.error("Errore durante la generazione della descrizione", e);
            throw new BusinessException("Impossibile generare la descrizione in questo momento.");
        }
    }

    public AiRecommendationResponseDTO consigliaBoxIntelligente(AiRecommendationRequestDTO preferenze) {
        List<Box> catalogo = boxRepository.findByAttivoTrue();
        String riassuntoCatalogo = catalogo.stream()
                .map(b -> String.format("- ID: %d, Nome: %s (Categoria: %s)", b.getId(), b.getNome(), b.getCategoria()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
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
            String jsonResponse = chatLanguageModel.generate(prompt).trim()
                    .replace(JSON_BLOCK_START, "").replace(MARKDOWN_FENCE, "");
            return objectMapper.readValue(jsonResponse, AiRecommendationResponseDTO.class);
        } catch (Exception e) {
            log.error("Errore durante la raccomandazione AI", e);
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
            log.info("Richiesta valori nutrizionali per: {}", nomeIngrediente);
            String jsonResponse = chatLanguageModel.generate(prompt).trim()
                    .replace(JSON_BLOCK_START, "").replace(MARKDOWN_FENCE, "");
            return objectMapper.readValue(jsonResponse, ValoriNutrizionaliRequestDTO.class);
        } catch (Exception e) {
            log.error("Errore generazione valori nutrizionali per {}", nomeIngrediente, e);
            return null;
        }
    }

    public List<IngredienteRequestDTO> generaIngredientiNuovi(int quantita, String suggerimento) {
        List<String> nomiEsistenti = ingredienteRepository.findAll().stream()
                .map(Ingrediente::getNome).toList();

        List<Fornitore> fornitori = fornitoreRepository.findAll();
        if (fornitori.isEmpty()) throw new BusinessException("Nessun fornitore in database.");

        String listaFornitoriContesto = fornitori.stream()
                .map(f -> f.getNome() + " (P.IVA: " + f.getPartitaIva() + ")")
                .collect(Collectors.joining(", "));

        String allergeniDisponibili = allergeneRepository.findAll().stream()
                .map(a -> a.getId() + " (" + a.getNome() + ")")
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                """
                        Sei un assistente esperto per l'e-commerce alimentare Yumaste. Genera un array JSON di %d nuovi ingredienti.
                        REGOLE DI COERENZA OBBLIGATORIE:
                        0. CRITERIO: IL TEMA È %s.
                        1. FORNITORE da questa lista: [%s].
                        2. UNICITÀ: Non usare: %s.
                        3. ALLERGENI: Usa solo ID: %s.
                        4. VALORI NUTRIZIONALI: realistici per 100g.
                        5. UNITÀ: 'kg' per carne/pesce/formaggi, 'l' per liquidi, 'pz' per uova, 'g' solo per spezie.
                        6. PREZZO: realistico per unità scelta.
                        7. Restituisci SOLO l'array JSON.
                        
                        {"ean": "", "partitaIva": "...", "nome": "...", "descrizione": "...", \
                        "unitaMisura": "kg", "pesoPerPezzo": 0.0, "prezzoPerUnita": 15.50, "attivo": true, "allergeniIds": [], \
                        "valoriNutrizionali": {"carboidrati": 0.0, "proteine": 20.0, "grassi": 5.0, "chilocalorie": 120.0, "sale": 1.0, "zuccheri": 0.0, "fibre": 0.0}}""",
                quantita,
                suggerimento.isEmpty() ? "nessun suggerimento" : suggerimento,
                listaFornitoriContesto,
                nomiEsistenti.isEmpty() ? "nessuno" : String.join(", ", nomiEsistenti),
                allergeniDisponibili
        );

        try {
            String jsonResponse = chatLanguageModel.generate(prompt).trim()
                    .replace(JSON_BLOCK_START, "").replace(MARKDOWN_FENCE, "");
            List<IngredienteRequestDTO> ingredientiGenerati = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            List<IngredienteRequestDTO> listaDefinitiva = new ArrayList<>();
            for (IngredienteRequestDTO ing : ingredientiGenerati) {
                listaDefinitiva.add(new IngredienteRequestDTO(
                        generaEanUnivocoCasuale(), ing.partitaIva(), ing.nome(),
                        ing.descrizione(), ing.unitaMisura(), ing.pesoPerPezzo(),
                        ing.prezzoPerUnita(), ing.attivo(), ing.allergeniIds(), ing.valoriNutrizionali()
                ));
            }
            return listaDefinitiva;
        } catch (Exception e) {
            log.error("Errore nella generazione AI", e);
            throw new BusinessException("Errore generazione ingredienti");
        }
    }

    public AiRecommendationResponseDTO consigliaBoxDaOrdini(Long utenteId) {
        List<DettaglioOrdine> ultimi = dettaglioOrdineRepository
                .findUltimiDettagliByUtenteId(utenteId, 10);

        if (ultimi.isEmpty()) {
            List<Box> catalogo = boxRepository.findByAttivoTrue();
            if (catalogo.isEmpty()) throw new ResourceNotFoundException("Nessuna box disponibile.");
            Box fallback = catalogo.getFirst();
            return new AiRecommendationResponseDTO(
                    fallback.getId(),
                    "Benvenuto! Questa è una delle nostre box più amate, perfetta per iniziare!",
                    fallback.getNome()
            );
        }

        List<Long> boxGiaOrdinate = dettaglioOrdineRepository
                .findBoxIdOrdinateByUtenteId(utenteId);

        List<Box> boxDisponibili = boxRepository.findByAttivoTrueAndIdNotIn(boxGiaOrdinate);
        if (boxDisponibili.isEmpty()) {
            boxDisponibili = boxRepository.findByAttivoTrue();
        }

        String ordiniPrecedenti = ultimi.stream()
                .map(d -> String.format("- %s (categoria: %s, €%s)",
                        d.getBox().getNome(),
                        d.getBox().getCategoria(),
                        d.getPrezzoUnitario()))
                .collect(Collectors.joining("\n"));

        String catalogoDisponibile = boxDisponibili.stream()
                .map(b -> String.format("- ID: %d, Nome: %s (Categoria: %s, €%s)",
                        b.getId(), b.getNome(), b.getCategoria(), b.getPrezzo()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            Sei il nutrizionista virtuale di Yumaste.
            
            Il cliente ha ordinato in passato:
            %s
            
            Box disponibili che non ha ancora ordinato:
            %s
            
            Scegli la box più adatta ai suoi gusti e rispondi ESCLUSIVAMENTE con un oggetto JSON valido.
            Non aggiungere testo prima o dopo il JSON.
            Formato richiesto:
            {
              "boxId": (numero intero),
              "messaggio": "(testo persuasivo sotto 60 parole, in italiano)",
              "nomeBox": "(nome esatto della box scelta)"
            }
            """,
                ordiniPrecedenti,
                catalogoDisponibile
        );

        try {
            log.info("Raccomandazione box da ordini per utente ID: {}", utenteId);
            String jsonResponse = chatLanguageModel.generate(prompt).trim()
                    .replace(JSON_BLOCK_START, "").replace(MARKDOWN_FENCE, "");
            return objectMapper.readValue(jsonResponse, AiRecommendationResponseDTO.class);
        } catch (Exception e) {
            log.error("Errore raccomandazione box da ordini per utente {}", utenteId, e);
            return new AiRecommendationResponseDTO(
                    null,
                    "Al momento non riesco a connettermi, esplora il nostro catalogo!",
                    "Catalogo"
            );
        }
    }

 // Definisci la costante all'inizio della classe
private static final SecureRandom SECURE_RANDOM = new SecureRandom();
 
private String generaEanUnivocoCasuale() {
    // Ora riutilizzi la costante invece di istanziarne una nuova
    StringBuilder ean = new StringBuilder();
    ean.append(SECURE_RANDOM.nextInt(1, 10));
    for (int i = 0; i < 12; i++) {
        ean.append(SECURE_RANDOM.nextInt(10));
    }
    return ean.toString();
}
}
