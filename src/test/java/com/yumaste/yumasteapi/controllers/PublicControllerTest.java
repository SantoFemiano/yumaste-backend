package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.dto.request.AiRecommendationRequestDTO;
import com.yumaste.yumasteapi.dto.response.*;
import com.yumaste.yumasteapi.services.BoxCompositionService;
import com.yumaste.yumasteapi.services.BoxService;
import com.yumaste.yumasteapi.services.ai.AiDescriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicControllerTest {

    @Mock private BoxService boxService;
    @Mock private BoxCompositionService boxCompositionService;
    @Mock private AiDescriptionService aiDescriptionService;

    @InjectMocks
    private PublicController publicController;

    // --- AI recommend ---

    @Test
    @DisplayName("getAiRecommendation - restituisce 200 con il DTO di raccomandazione")
    void getAiRecommendation_returns200() {
        AiRecommendationRequestDTO req = mock(AiRecommendationRequestDTO.class);
        AiRecommendationResponseDTO resp = mock(AiRecommendationResponseDTO.class);
        when(aiDescriptionService.consigliaBoxIntelligente(req)).thenReturn(resp);

        ResponseEntity<AiRecommendationResponseDTO> result = publicController.getAiRecommendation(req);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(resp);
    }

    // --- AI descrizione box ---

    @Test
    @DisplayName("generateBoxDescriptionWithAi - restituisce 200 con descrizione")
    void generateBoxDescription_returns200() {
        when(aiDescriptionService.generaDescrizionePerBox(1L)).thenReturn("Descrizione AI");

        ResponseEntity<String> result = publicController.generateBoxDescriptionWithAi(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo("Descrizione AI");
    }

    // --- getCatalog ---

    @Test
    @DisplayName("getCatalog - catalogo non vuoto restituisce 200")
    void getCatalog_nonEmpty_returns200() {
        CatalogBoxDTO box = mock(CatalogBoxDTO.class);
        PagedResponseDTO<CatalogBoxDTO> paged = new PagedResponseDTO<>(List.of(box), 0, 10, 1L, 1, false);
        when(boxService.getAllActiveBoxes(null, null, Pageable.unpaged())).thenReturn(paged);

        ResponseEntity<PagedResponseDTO<CatalogBoxDTO>> result =
                publicController.getCatalog(null, null, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().content()).hasSize(1);
    }

    @Test
    @DisplayName("getCatalog - catalogo vuoto restituisce 204")
    void getCatalog_empty_returns204() {
        PagedResponseDTO<CatalogBoxDTO> paged = new PagedResponseDTO<>(List.of(), 0, 0, 0L, 0, false);
        when(boxService.getAllActiveBoxes(null, null, Pageable.unpaged())).thenReturn(paged);

        ResponseEntity<PagedResponseDTO<CatalogBoxDTO>> result =
                publicController.getCatalog(null, null, Pageable.unpaged());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // --- getBoxById ---

    @Test
    @DisplayName("getBoxById - restituisce 200 con la box")
    void getBoxById_returns200() {
        CatalogBoxDTO box = mock(CatalogBoxDTO.class);
        when(boxService.getBoxById(1L)).thenReturn(box);

        ResponseEntity<CatalogBoxDTO> result = publicController.getBoxById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(box);
    }

    // --- getIngredientiByBoxId ---

    @Test
    @DisplayName("getIngredientiByBoxId - restituisce 200 con lista ingredienti")
    void getIngredientiByBoxId_returns200() {
        BoxIngredientDTO ing = mock(BoxIngredientDTO.class);
        when(boxCompositionService.getBoxIngredients(1L)).thenReturn(List.of(ing));

        ResponseEntity<List<BoxIngredientDTO>> result = publicController.getIngredientiByBoxId(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(ing);
    }

    // --- getIngredientiBox (valori nutrizionali) ---

    @Test
    @DisplayName("getIngredientiBox - restituisce 200 con valori nutrizionali")
    void getIngredientiBox_returns200() {
        IngredientiConValoriDTO val = mock(IngredientiConValoriDTO.class);
        when(boxCompositionService.getIngredientiConValoriDellaBox(1L)).thenReturn(List.of(val));

        ResponseEntity<List<IngredientiConValoriDTO>> result = publicController.getIngredientiBox(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(val);
    }

    // --- getBoxDetail ---

    @Test
    @DisplayName("getBoxDetail - restituisce 200 con dettaglio box")
    void getBoxDetail_returns200() {
        BoxDetailDTO detail = mock(BoxDetailDTO.class);
        when(boxService.getDettaglioBox(1L)).thenReturn(detail);

        ResponseEntity<BoxDetailDTO> result = publicController.getBoxDetail(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(detail);
    }
}
