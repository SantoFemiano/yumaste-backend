package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.AiGenerateBoxRequestDTO;
import com.yumaste.yumasteapi.dto.response.AiGenerateBoxResponseDTO;
import com.yumaste.yumasteapi.exceptions.BusinessException;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import com.yumaste.yumasteapi.services.ai.AiBoxGenerationService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiBoxGenerationServiceTest {

    @Mock private ChatLanguageModel chatLanguageModel;
    @Mock private BoxRepository boxRepository;
    @Mock private IngredienteRepository ingredienteRepository;

    @InjectMocks private AiBoxGenerationService aiBoxGenerationService;

    private Box esistenteBox;
    private Ingrediente ingredienteDisponibile;
    private String mockValidJsonResponse;

    @BeforeEach
    void setUp() {
        esistenteBox = new Box();
        esistenteBox.setNome("Box Americana");

        ingredienteDisponibile = new Ingrediente();
        ingredienteDisponibile.setId(1L);
        ingredienteDisponibile.setNome("Cheddar");
        ingredienteDisponibile.setUnitaMisura("g");

        // Risposta JSON mockata valida che l'ObjectMapper dovrà decodificare
        mockValidJsonResponse = """
                ```json
                {
                  "nome": "Trancio di Salmone agli Agrumi",
                  "descrizione": "Una box fresca e deliziosa...",
                  "categoria": "Pesce",
                  "prezzo": 24.50,
                  "porzioni": 2,
                  "urlImmagine": "[https://images.unsplash.com/](https://images.unsplash.com/)...",
                  "ingredienti": [
                    { "ingredienteId": 1, "quantita": 150.0 }
                  ]
                }
                ```
                """;
    }

    // =========================================================================
    // TESTS: generaBoxAutomatica
    // =========================================================================

    @Test
    @DisplayName("generaBoxAutomatica - Successo completo con suggerimento e pulizia Markdown")
    void generaBoxAutomatica_Success_WithSuggerimentoAndMarkdownCleanup() {
        AiGenerateBoxRequestDTO request = new AiGenerateBoxRequestDTO("Qualcosa a base di Pesce");

        when(boxRepository.findAll()).thenReturn(List.of(esistenteBox));
        when(ingredienteRepository.findAll()).thenReturn(List.of(ingredienteDisponibile));
        when(chatLanguageModel.generate(anyString())).thenReturn(mockValidJsonResponse);

        AiGenerateBoxResponseDTO result = aiBoxGenerationService.generaBoxAutomatica(request);

        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("Trancio di Salmone agli Agrumi");
        assertThat(result.categoria()).isEqualTo("Pesce");
        assertThat(result.prezzo()).isEqualTo(24.50);
        assertThat(result.ingredienti()).hasSize(1);
        assertThat(result.ingredienti().getFirst().ingredienteId()).isEqualTo(1L);

        verify(boxRepository).findAll();
        verify(ingredienteRepository).findAll();
        verify(chatLanguageModel).generate(anyString());
    }

    @Test
    @DisplayName("generaBoxAutomatica - Successo senza suggerimento utente e senza box storiche")
    void generaBoxAutomatica_Success_NoSuggerimentoAndNoExistingBoxes() {
        AiGenerateBoxRequestDTO requestSenzaSuggerimento = new AiGenerateBoxRequestDTO(null);

        // Caso in cui il catalogo iniziale è vuoto (es. Primo avvio)
        when(boxRepository.findAll()).thenReturn(Collections.emptyList());
        when(ingredienteRepository.findAll()).thenReturn(List.of(ingredienteDisponibile));
        when(chatLanguageModel.generate(anyString())).thenReturn(mockValidJsonResponse);

        AiGenerateBoxResponseDTO result = aiBoxGenerationService.generaBoxAutomatica(requestSenzaSuggerimento);

        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("Trancio di Salmone agli Agrumi");

        verify(boxRepository).findAll();
        verify(ingredienteRepository).findAll();
    }

    @Test
    @DisplayName("generaBoxAutomatica - Errore se non ci sono ingredienti censiti nel database")
    void generaBoxAutomatica_ThrowsException_WhenNoIngredientsInDb() {
        AiGenerateBoxRequestDTO request = new AiGenerateBoxRequestDTO("Qualsiasi cosa");

        when(boxRepository.findAll()).thenReturn(List.of(esistenteBox));
        // Il magazzino/ingredienti è vuoto
        when(ingredienteRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> aiBoxGenerationService.generaBoxAutomatica(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nessun ingrediente in database per comporre la box.");

        verify(chatLanguageModel, never()).generate(anyString());
    }

    @Test
    @DisplayName("generaBoxAutomatica - Errore LLM offline o JSON malformato lancia BusinessException")
    void generaBoxAutomatica_ThrowsException_OnLlmOrParsingError() {
        AiGenerateBoxRequestDTO request = new AiGenerateBoxRequestDTO("Box Piccante");

        when(boxRepository.findAll()).thenReturn(List.of(esistenteBox));
        when(ingredienteRepository.findAll()).thenReturn(List.of(ingredienteDisponibile));
        // L'AI restituisce del testo normale al posto del JSON richiesto rompendo il parsing
        when(chatLanguageModel.generate(anyString())).thenReturn("Mi dispiace, non posso aiutarti.");

        assertThatThrownBy(() -> aiBoxGenerationService.generaBoxAutomatica(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossibile generare la Box con l'IA al momento.");

        verify(chatLanguageModel).generate(anyString());
    }
}