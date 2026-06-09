package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.dto.request.ValoriNutrizionaliRequestDTO;
import com.yumaste.yumasteapi.dto.response.IngredienteAllergeneResponseDTO;
import com.yumaste.yumasteapi.dto.response.IngredienteResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.IngredienteAllergeneMapper;
import com.yumaste.yumasteapi.mapper.IngredienteMapper;
import com.yumaste.yumasteapi.models.*;
import com.yumaste.yumasteapi.repositories.*;
import com.yumaste.yumasteapi.services.ai.AiDescriptionService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredienteServiceTest {

    @Mock private IngredienteRepository ingredienteRepository;
    @Mock private FornitoreRepository fornitoreRepository;
    @Mock private IngredienteMapper ingredienteMapper;
    @Mock private IngredienteAllergeneRepository ingredienteAllergeneRepository;
    @Mock private IngredienteAllergeneMapper ingredienteAllergeneMapper;
    @Mock private NutritionalValueRepository nutritionalValueRepository;
    @Mock private AllergeneRepository allergeneRepository;
    @Mock private AiDescriptionService aiDescriptionService;

    @InjectMocks private IngredienteService ingredienteService;

    private Fornitore fornitore;
    private Ingrediente ingrediente;
    private IngredienteRequestDTO requestDTO;
    private IngredienteResponseDTO responseDTO;
    private ValoriNutrizionaliRequestDTO valoriNutrizionaliRequestDTO;

    @BeforeEach
    void setUp() {
        fornitore = new Fornitore();
        fornitore.setId(1L);
        fornitore.setPartitaIva("12345678901");
        fornitore.setNome("Fornitore SRL");

        ingrediente = new Ingrediente();
        ingrediente.setId(10L);
        ingrediente.setNome("Pollo");
        ingrediente.setFornitore(fornitore);
        ingrediente.setAttivo(true);

        valoriNutrizionaliRequestDTO = new ValoriNutrizionaliRequestDTO(
                BigDecimal.valueOf(20), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(2), BigDecimal.valueOf(0.5), 110
        );

        requestDTO = new IngredienteRequestDTO(
                "1234567890123", "12345678901", "Pollo", "Petto di pollo",
                "kg", BigDecimal.valueOf(1), BigDecimal.valueOf(10.00), true,
                List.of(5L), valoriNutrizionaliRequestDTO
        );

        responseDTO = new IngredienteResponseDTO(
                10L, "1234567890123", "12345678901", 1L, "Fornitore SRL",
                "Pollo", "Petto di pollo", "kg", BigDecimal.valueOf(1),
                BigDecimal.valueOf(10.00), true, null
        );
    }

    // =========================================================================
    // TESTS: creaIngrediente
    // =========================================================================

    @Test
    @DisplayName("creaIngrediente - Successo con Valori Nutrizionali forniti e Allergeni")
    void creaIngrediente_Success_WithProvidedMacrosAndAllergeni() {
        Allergene allergene = new Allergene();
        allergene.setId(5L);
        allergene.setNome("Lattosio");

        when(ingredienteMapper.toEntity(requestDTO)).thenReturn(ingrediente);
        when(fornitoreRepository.findByPartitaIva("12345678901")).thenReturn(Optional.of(fornitore));
        when(ingredienteRepository.save(any(Ingrediente.class))).thenReturn(ingrediente);
        when(allergeneRepository.findById(5L)).thenReturn(Optional.of(allergene));
        when(ingredienteMapper.toResponseDTO(ingrediente)).thenReturn(responseDTO);

        IngredienteResponseDTO result = ingredienteService.creaIngrediente(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("Pollo");
        verify(ingredienteRepository).save(any(Ingrediente.class));
        verify(ingredienteAllergeneRepository).save(any(IngredienteAllergene.class));
    }

    @Test
    @DisplayName("creaIngrediente - Successo con Valori Nutrizionali generati da AI")
    void creaIngrediente_Success_WithAiGeneratedMacros() {
        // Prepariamo una richiesta senza valori nutrizionali
        IngredienteRequestDTO requestSenzaValori = new IngredienteRequestDTO(
                "1234567890123", "12345678901", "Pollo", "Petto di pollo",
                "kg", BigDecimal.valueOf(1), BigDecimal.valueOf(10.00), true,
                Collections.emptyList(), null
        );

        ingrediente.setValoriNutrizionali(null); // Assicuriamoci che parta da null

        when(ingredienteMapper.toEntity(requestSenzaValori)).thenReturn(ingrediente);
        when(fornitoreRepository.findByPartitaIva("12345678901")).thenReturn(Optional.of(fornitore));
        when(aiDescriptionService.generaValoriNutrizionali("Pollo")).thenReturn(valoriNutrizionaliRequestDTO);
        when(ingredienteRepository.save(any(Ingrediente.class))).thenReturn(ingrediente);
        when(ingredienteMapper.toResponseDTO(ingrediente)).thenReturn(responseDTO);

        IngredienteResponseDTO result = ingredienteService.creaIngrediente(requestSenzaValori);

        assertThat(result).isNotNull();
        verify(aiDescriptionService).generaValoriNutrizionali("Pollo");
        verify(ingredienteRepository).save(ingrediente);
    }

    @Test
    @DisplayName("creaIngrediente - Fornitore non trovato lancia ResourceNotFoundException")
    void creaIngrediente_FornitoreNotFound() {
        when(ingredienteMapper.toEntity(requestDTO)).thenReturn(ingrediente);
        when(fornitoreRepository.findByPartitaIva("12345678901")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.creaIngrediente(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fornitore non trovato con Partita Iva: 12345678901");
    }

    @Test
    @DisplayName("creaIngrediente - Allergene non trovato lancia ResourceNotFoundException")
    void creaIngrediente_AllergeneNotFound() {
        when(ingredienteMapper.toEntity(requestDTO)).thenReturn(ingrediente);
        when(fornitoreRepository.findByPartitaIva("12345678901")).thenReturn(Optional.of(fornitore));
        when(ingredienteRepository.save(any(Ingrediente.class))).thenReturn(ingrediente);
        when(allergeneRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.creaIngrediente(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergene non trovato ID: 5");
    }

    // =========================================================================
    // TESTS: Liste e Consultazioni
    // =========================================================================

    @Test
    @DisplayName("getAllIngredientiConAllergeni - Ritorna lista dettagliata mappata")
    void getAllIngredientiConAllergeni_ReturnsList() {
        IngredienteAllergene ponte = new IngredienteAllergene();
        IngredienteAllergeneResponseDTO dtoPonte = new IngredienteAllergeneResponseDTO(10L, "Pollo", 5L, "Lattosio");

        when(ingredienteAllergeneRepository.findAllWithDetails()).thenReturn(List.of(ponte));
        when(ingredienteAllergeneMapper.toDto(ponte)).thenReturn(dtoPonte);

        List<IngredienteAllergeneResponseDTO> result = ingredienteService.getAllIngredientiConAllergeni();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().ingredienteNome()).isEqualTo("Pollo");
    }

    @Test
    @DisplayName("getAllIngredienti - Ritorna solo ingredienti attivi")
    void getAllIngredienti_ReturnsActiveOnly() {
        when(ingredienteRepository.findByAttivoTrue()).thenReturn(List.of(ingrediente));
        when(ingredienteMapper.toResponseDTO(ingrediente)).thenReturn(responseDTO);

        List<IngredienteResponseDTO> result = ingredienteService.getAllIngredienti();

        assertThat(result).hasSize(1);
        verify(ingredienteRepository).findByAttivoTrue();
    }

    @Test
    @DisplayName("getAllIngredientiInattivi - Ritorna solo ingredienti inattivi")
    void getAllIngredientiInattivi_ReturnsInactiveOnly() {
        when(ingredienteRepository.findByAttivoFalse()).thenReturn(List.of(ingrediente));
        when(ingredienteMapper.toResponseDTO(ingrediente)).thenReturn(responseDTO);

        List<IngredienteResponseDTO> result = ingredienteService.getAllIngredientiInattivi();

        assertThat(result).hasSize(1);
        verify(ingredienteRepository).findByAttivoFalse();
    }

    // =========================================================================
    // TESTS: updateIngrediente
    // =========================================================================

    @Test
    @DisplayName("updateIngrediente - Successo senza cambio fornitore e con macro fornite")
    void updateIngrediente_Success_NoFornitoreChange() {
        ValoriNutrizionali vnEsistenti = new ValoriNutrizionali();

        when(ingredienteRepository.findById(10L)).thenReturn(Optional.of(ingrediente));
        when(nutritionalValueRepository.findByIngrediente(ingrediente)).thenReturn(Optional.of(vnEsistenti));
        when(ingredienteRepository.save(ingrediente)).thenReturn(ingrediente);
        when(ingredienteMapper.toResponseDTO(ingrediente)).thenReturn(responseDTO);

        IngredienteResponseDTO result = ingredienteService.updateIngrediente(10L, requestDTO);

        assertThat(result).isNotNull();
        verify(fornitoreRepository, never()).findByPartitaIva(anyString());
        verify(nutritionalValueRepository).save(any(ValoriNutrizionali.class));
    }

    @Test
    @DisplayName("updateIngrediente - Successo con cambio fornitore e macro AI")
    void updateIngrediente_Success_WithFornitoreChangeAndAiMacros() {
        // Prepariamo la richiesta con una p.iva diversa da quella attuale dell'ingrediente ("12345678901")
        IngredienteRequestDTO requestNuovoFornitore = new IngredienteRequestDTO(
                "1234567890123", "99999999999", "Pollo", "Petto di pollo",
                "kg", BigDecimal.valueOf(1), BigDecimal.valueOf(10.00), true,
                Collections.emptyList(), null // Triggera la chiamata a Gemini AI
        );

        Fornitore nuovoFornitore = new Fornitore();
        nuovoFornitore.setPartitaIva("99999999999");

        when(ingredienteRepository.findById(10L)).thenReturn(Optional.of(ingrediente));
        when(fornitoreRepository.findByPartitaIva("99999999999")).thenReturn(Optional.of(nuovoFornitore));
        when(aiDescriptionService.generaValoriNutrizionali("Pollo")).thenReturn(valoriNutrizionaliRequestDTO);
        when(nutritionalValueRepository.findByIngrediente(ingrediente)).thenReturn(Optional.empty()); // Forza la creazione ex-novo
        when(ingredienteRepository.save(ingrediente)).thenReturn(ingrediente);
        when(ingredienteMapper.toResponseDTO(ingrediente)).thenReturn(responseDTO);

        IngredienteResponseDTO result = ingredienteService.updateIngrediente(10L, requestNuovoFornitore);

        assertThat(result).isNotNull();
        verify(fornitoreRepository).findByPartitaIva("99999999999");
        verify(aiDescriptionService).generaValoriNutrizionali("Pollo");
        verify(nutritionalValueRepository).save(any(ValoriNutrizionali.class));
    }

    @Test
    @DisplayName("updateIngrediente - Ingrediente non trovato lancia RuntimeException")
    void updateIngrediente_IngredienteNotFound() {
        when(ingredienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.updateIngrediente(99L, requestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingrediente non trovato");
    }

    @Test
    @DisplayName("updateIngrediente - Nuovo Fornitore non trovato lancia RuntimeException")
    void updateIngrediente_NewFornitoreNotFound() {
        IngredienteRequestDTO requestNuovoFornitore = new IngredienteRequestDTO(
                "1234567890123", "99999999999", "Pollo", "Petto di pollo",
                "kg", BigDecimal.valueOf(1), BigDecimal.valueOf(10.00), true,
                Collections.emptyList(), valoriNutrizionaliRequestDTO
        );

        when(ingredienteRepository.findById(10L)).thenReturn(Optional.of(ingrediente));
        when(fornitoreRepository.findByPartitaIva("99999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.updateIngrediente(10L, requestNuovoFornitore))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Fornitore non trovato");
    }

    // =========================================================================
    // TESTS: deleteIngrediente
    // =========================================================================

    @Test
    @DisplayName("deleteIngrediente - Successo (Soft Delete)")
    void deleteIngrediente_Success() {
        when(ingredienteRepository.findById(10L)).thenReturn(Optional.of(ingrediente));
        when(ingredienteRepository.save(ingrediente)).thenReturn(ingrediente);

        ingredienteService.deleteIngrediente(10L);

        assertThat(ingrediente.getAttivo()).isFalse(); // Verifica il soft delete logic
        verify(ingredienteRepository).save(ingrediente);
    }

    @Test
    @DisplayName("deleteIngrediente - Non trovato lancia ResourceNotFoundException")
    void deleteIngrediente_NotFound() {
        when(ingredienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.deleteIngrediente(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ingrediente non trovato");
    }
}