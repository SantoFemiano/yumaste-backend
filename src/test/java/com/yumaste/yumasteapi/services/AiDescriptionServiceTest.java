package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.AiRecommendationRequestDTO;
import com.yumaste.yumasteapi.dto.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.dto.request.ValoriNutrizionaliRequestDTO;
import com.yumaste.yumasteapi.dto.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.dto.response.AiRecommendationResponseDTO;
import com.yumaste.yumasteapi.exceptions.BusinessException;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.DettaglioOrdine;
import com.yumaste.yumasteapi.models.Fornitore;
import com.yumaste.yumasteapi.repositories.*;
import com.yumaste.yumasteapi.services.ai.AiDescriptionService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiDescriptionServiceTest {

    @Mock private ChatLanguageModel chatLanguageModel;
    @Mock private BoxRepository boxRepository;
    @Mock private BoxCompositionService boxCompositionService;
    @Mock private IngredienteRepository ingredienteRepository;
    @Mock private FornitoreRepository fornitoreRepository;
    @Mock private AllergeneRepository allergeneRepository;
    @Mock private DettaglioOrdineRepository dettaglioOrdineRepository;

    @InjectMocks private AiDescriptionService aiDescriptionService;

    private Box sampleBox;

    @BeforeEach
    void setUp() {
        sampleBox = new Box();
        sampleBox.setId(1L);
        sampleBox.setNome("Box Test");
        sampleBox.setCategoria("Vegano");
        sampleBox.setPorzioni((byte) 2);
        sampleBox.setPrezzo(new BigDecimal("25.00"));
        sampleBox.setAttivo(true);
    }

    // =========================================================================
    // TESTS: generaDescrizionePerBox
    // =========================================================================

    @Test
    @DisplayName("generaDescrizionePerBox - Successo")
    void generaDescrizionePerBox_Success() {
        IngredientiConValoriDTO ingredienteDto = new IngredientiConValoriDTO(
                "Pomodoro", BigDecimal.valueOf(200), "g", BigDecimal.valueOf(40),
                BigDecimal.valueOf(2), BigDecimal.valueOf(8), BigDecimal.valueOf(4),
                BigDecimal.valueOf(2), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.1)
        );

        when(boxRepository.findById(1L)).thenReturn(Optional.of(sampleBox));
        when(boxCompositionService.getIngredientiConValoriDellaBox(1L)).thenReturn(List.of(ingredienteDto));
        when(chatLanguageModel.generate(anyString())).thenReturn("Una splendida descrizione generata dall'AI.");

        String descrizione = aiDescriptionService.generaDescrizionePerBox(1L);

        assertThat(descrizione).isEqualTo("Una splendida descrizione generata dall'AI.");
        verify(boxRepository).findById(1L);
        verify(chatLanguageModel).generate(anyString());
    }

    @Test
    @DisplayName("generaDescrizionePerBox - Box non trovata lancia ResourceNotFoundException")
    void generaDescrizionePerBox_BoxNotFound() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiDescriptionService.generaDescrizionePerBox(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Box non trovata con ID: 99");
    }

    @Test
    @DisplayName("generaDescrizionePerBox - Errore LLM lancia BusinessException")
    void generaDescrizionePerBox_LlmError() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(sampleBox));
        when(boxCompositionService.getIngredientiConValoriDellaBox(1L)).thenReturn(Collections.emptyList());
        when(chatLanguageModel.generate(anyString())).thenThrow(new RuntimeException("LLM offline"));

        assertThatThrownBy(() -> aiDescriptionService.generaDescrizionePerBox(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossibile generare la descrizione in questo momento.");
    }

    // =========================================================================
    // TESTS: consigliaBoxIntelligente
    // =========================================================================

    @Test
    @DisplayName("consigliaBoxIntelligente - Successo")
    void consigliaBoxIntelligente_Success() {
        AiRecommendationRequestDTO request = new AiRecommendationRequestDTO("Dimagrimento", "Vegana", List.of("Glutine"), 2000);
        String mockJsonResponse = "{\"boxId\":1,\"messaggio\":\"Ti consiglio questa box\",\"nomeBox\":\"Box Test\"}";

        when(boxRepository.findByAttivoTrue()).thenReturn(List.of(sampleBox));
        when(chatLanguageModel.generate(anyString())).thenReturn(mockJsonResponse);

        AiRecommendationResponseDTO response = aiDescriptionService.consigliaBoxIntelligente(request);

        assertThat(response).isNotNull();
        assertThat(response.boxId()).isEqualTo(1L);
        assertThat(response.nomeBox()).isEqualTo("Box Test");
        assertThat(response.messaggio()).isEqualTo("Ti consiglio questa box");
    }

    @Test
    @DisplayName("consigliaBoxIntelligente - Fallback su errore di parsing o eccezione")
    void consigliaBoxIntelligente_FallbackOnError() {
        AiRecommendationRequestDTO request = new AiRecommendationRequestDTO("Massa", "Onnivora", Collections.emptyList(), 2500);

        when(boxRepository.findByAttivoTrue()).thenReturn(List.of(sampleBox));
        when(chatLanguageModel.generate(anyString())).thenReturn("JSON NON VALIDO");

        AiRecommendationResponseDTO response = aiDescriptionService.consigliaBoxIntelligente(request);

        assertThat(response).isNotNull();
        assertThat(response.boxId()).isNull();
        assertThat(response.nomeBox()).isEqualTo("Catalogo");
        assertThat(response.messaggio()).contains("Al momento non riesco a connettermi");
    }

    // =========================================================================
    // TESTS: generaValoriNutrizionali
    // =========================================================================

    @Test
    @DisplayName("generaValoriNutrizionali - Successo")
    void generaValoriNutrizionali_Success() {
        String mockJsonResponse = "{\"proteine\":12.5,\"carboidrati\":25.0,\"zuccheri\":2.0,\"fibre\":4.5,\"grassi\":1.2,\"sale\":0.5,\"chilocalorie\":160}";
        when(chatLanguageModel.generate(anyString())).thenReturn(mockJsonResponse);

        ValoriNutrizionaliRequestDTO result = aiDescriptionService.generaValoriNutrizionali("Pasta Integrale");

        assertThat(result).isNotNull();
        assertThat(result.chilocalorie()).isEqualTo(160);
        assertThat(result.proteine()).isEqualByComparingTo("12.5");
    }

    @Test
    @DisplayName("generaValoriNutrizionali - Ritorna null su eccezione")
    void javaGeneraValoriNutrizionali_ReturnsNullOnError() {
        when(chatLanguageModel.generate(anyString())).thenThrow(new RuntimeException("API error"));

        ValoriNutrizionaliRequestDTO result = aiDescriptionService.generaValoriNutrizionali("Mela");

        assertThat(result).isNull();
    }

    // =========================================================================
    // TESTS: generaIngredientiNuovi
    // =========================================================================

    @Test
    @DisplayName("generaIngredientiNuovi - Nessun Fornitore lancia BusinessException")
    void generaIngredientiNuovi_NoFornitori() {
        when(fornitoreRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> aiDescriptionService.generaIngredientiNuovi(1, "suggerimento"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nessun fornitore in database.");
    }

    @Test
    @DisplayName("generaIngredientiNuovi - Successo")
    void generaIngredientiNuovi_Success() {
        Fornitore fornitore = new Fornitore();
        fornitore.setNome("Fornitore SRL");
        fornitore.setPartitaIva("12345678901");

        String mockJsonResponse = "[{"
                + "\"ean\":\"\","
                + "\"partitaIva\":\"12345678901\","
                + "\"nome\":\"Zucchine\","
                + "\"descrizione\":\"Fresche\","
                + "\"unitaMisura\":\"kg\","
                + "\"pesoPerPezzo\":0,"
                + "\"prezzoPerUnita\":2.50,"
                + "\"attivo\":true,"
                + "\"allergeniIds\":[],"
                + "\"valoriNutrizionali\":{\"proteine\":1.2,\"carboidrati\":3.4,\"zuccheri\":1.0,\"fibre\":1.1,\"grassi\":0.2,\"sale\":0.01,\"chilocalorie\":20}"
                + "}]";

        when(ingredienteRepository.findAll()).thenReturn(Collections.emptyList());
        when(fornitoreRepository.findAll()).thenReturn(List.of(fornitore));
        when(allergeneRepository.findAll()).thenReturn(Collections.emptyList());
        when(chatLanguageModel.generate(anyString())).thenReturn(mockJsonResponse);

        List<IngredienteRequestDTO> result = aiDescriptionService.generaIngredientiNuovi(1, "Verdure fresche");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().nome()).isEqualTo("Zucchine");
        assertThat(result.getFirst().ean()).isNotEmpty(); // Controlla la generazione casuale dell'EAN univoco
    }

    @Test
    @DisplayName("generaIngredientiNuovi - Eccezione nel Mapping lancia BusinessException")
    void generaIngredientiNuovi_MappingError() {
        Fornitore fornitore = new Fornitore();
        when(fornitoreRepository.findAll()).thenReturn(List.of(fornitore));
        when(chatLanguageModel.generate(anyString())).thenReturn("FORMATO ERRATO");

        assertThatThrownBy(() -> aiDescriptionService.generaIngredientiNuovi(1, "suggerimento"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Errore generazione ingredienti");
    }

    // =========================================================================
    // TESTS: consigliaBoxDaOrdini
    // =========================================================================

    @Test
    @DisplayName("consigliaBoxDaOrdini - Senza ordini precedenti e catalogo disponibile (Fallback)")
    void consigliaBoxDaOrdini_NoOrders_ReturnsFallback() {
        when(dettaglioOrdineRepository.findUltimiDettagliByUtenteId(1L, 10)).thenReturn(Collections.emptyList());
        when(boxRepository.findByAttivoTrue()).thenReturn(List.of(sampleBox));

        AiRecommendationResponseDTO response = aiDescriptionService.consigliaBoxDaOrdini(1L);

        assertThat(response).isNotNull();
        assertThat(response.boxId()).isEqualTo(1L);
        assertThat(response.nomeBox()).isEqualTo("Box Test");
        assertThat(response.messaggio()).contains("Benvenuto! Questa è una delle nostre box più amate");
    }

    @Test
    @DisplayName("consigliaBoxDaOrdini - Senza ordini e catalogo vuoto lancia ResourceNotFoundException")
    void consigliaBoxDaOrdini_NoOrdersAndNoBoxes() {
        when(dettaglioOrdineRepository.findUltimiDettagliByUtenteId(1L, 10)).thenReturn(Collections.emptyList());
        when(boxRepository.findByAttivoTrue()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> aiDescriptionService.consigliaBoxDaOrdini(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nessuna box disponibile.");
    }

    @Test
    @DisplayName("consigliaBoxDaOrdini - Successo con ordini storici")
    void consigliaBoxDaOrdini_SuccessWithOrders() {
        DettaglioOrdine dettaglio = new DettaglioOrdine();
        dettaglio.setBox(sampleBox);
        dettaglio.setPrezzoUnitario(new BigDecimal("25.00"));

        Box alternativeBox = new Box();
        alternativeBox.setId(2L);
        alternativeBox.setNome("Box Pesce");
        alternativeBox.setCategoria("Pesce");
        alternativeBox.setPrezzo(new BigDecimal("30.00"));

        String mockJsonResponse = "{\"boxId\":2,\"messaggio\":\"Visto che ami i sapori freschi, prova il pesce!\",\"nomeBox\":\"Box Pesce\"}";

        when(dettaglioOrdineRepository.findUltimiDettagliByUtenteId(1L, 10)).thenReturn(List.of(dettaglio));
        when(dettaglioOrdineRepository.findBoxIdOrdinateByUtenteId(1L)).thenReturn(List.of(1L));
        when(boxRepository.findByAttivoTrueAndIdNotIn(List.of(1L))).thenReturn(List.of(alternativeBox));
        when(chatLanguageModel.generate(anyString())).thenReturn(mockJsonResponse);

        AiRecommendationResponseDTO response = aiDescriptionService.consigliaBoxDaOrdini(1L);

        assertThat(response).isNotNull();
        assertThat(response.boxId()).isEqualTo(2L);
        assertThat(response.nomeBox()).isEqualTo("Box Pesce");
    }

    @Test
    @DisplayName("consigliaBoxDaOrdini - Fallback su errore generico")
    void consigliaBoxDaOrdini_FallbackOnError() {
        DettaglioOrdine dettaglio = new DettaglioOrdine();
        dettaglio.setBox(sampleBox);
        dettaglio.setPrezzoUnitario(new BigDecimal("25.00"));

        when(dettaglioOrdineRepository.findUltimiDettagliByUtenteId(1L, 10)).thenReturn(List.of(dettaglio));
        when(chatLanguageModel.generate(anyString())).thenThrow(new RuntimeException("Model Timeout"));

        AiRecommendationResponseDTO response = aiDescriptionService.consigliaBoxDaOrdini(1L);

        assertThat(response).isNotNull();
        assertThat(response.boxId()).isNull();
        assertThat(response.nomeBox()).isEqualTo("Catalogo");
        assertThat(response.messaggio()).contains("Al momento non riesco a connettermi");
    }
}