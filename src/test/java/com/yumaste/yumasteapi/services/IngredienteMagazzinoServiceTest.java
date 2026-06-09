package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.IngredienteMagazzinoRequest;
import com.yumaste.yumasteapi.dto.response.IngredienteMagazzinoResponse;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.IngredienteMagazzinoMapper;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.models.IngredienteMagazzino;
import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.repositories.IngredienteMagazzinoRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import com.yumaste.yumasteapi.repositories.MagazzinoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredienteMagazzinoServiceTest {

    @Mock private IngredienteMagazzinoRepository ingredienteMagazzinoRepository;
    @Mock private IngredienteRepository ingredienteRepository;
    @Mock private IngredienteMagazzinoMapper ingredienteMagazzinoMapper;
    @Mock private MagazzinoRepository magazzinoRepository;

    @InjectMocks
    private IngredienteMagazzinoService service;

    private Ingrediente ingrediente;
    private Magazzino magazzino;
    private IngredienteMagazzinoRequest request;
    private IngredienteMagazzinoResponse mockResponse;

    @BeforeEach
    void setUp() {
        ingrediente = new Ingrediente();
        ingrediente.setId(1L);

        magazzino = new Magazzino();
        magazzino.setId(1L);

        request = new IngredienteMagazzinoRequest(
                1L, 1L, "LOTTO-001",
                new BigDecimal("100"), LocalDate.now()
        );

        mockResponse = mock(IngredienteMagazzinoResponse.class);
    }

    @Test
    @DisplayName("caricaMerci - nuova riga quando giacenza non esiste")
    void caricaMerci_newRow_whenGiacenzaNotFound() {
        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
        when(magazzinoRepository.findById(1L)).thenReturn(Optional.of(magazzino));
        when(ingredienteMagazzinoRepository.findByMagazzinoAndIngredienteAndLotto(any(), any(), any()))
                .thenReturn(Optional.empty());

        IngredienteMagazzino saved = new IngredienteMagazzino();
        when(ingredienteMagazzinoRepository.save(any())).thenReturn(saved);
        when(ingredienteMagazzinoMapper.toDto(saved)).thenReturn(mockResponse);

        IngredienteMagazzinoResponse result = service.caricaMerci(request);

        assertThat(result).isEqualTo(mockResponse);
        verify(ingredienteMagazzinoRepository).save(any(IngredienteMagazzino.class));
    }

    @Test
    @DisplayName("caricaMerci - aggiorna quantità quando giacenza esiste già")
    void caricaMerci_updatesQuantity_whenGiacenzaExists() {
        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
        when(magazzinoRepository.findById(1L)).thenReturn(Optional.of(magazzino));

        IngredienteMagazzino existing = new IngredienteMagazzino();
        existing.setQuantita(new BigDecimal("50"));
        when(ingredienteMagazzinoRepository.findByMagazzinoAndIngredienteAndLotto(any(), any(), any()))
                .thenReturn(Optional.of(existing));

        when(ingredienteMagazzinoRepository.save(existing)).thenReturn(existing);
        when(ingredienteMagazzinoMapper.toDto(existing)).thenReturn(mockResponse);

        service.caricaMerci(request);

        assertThat(existing.getQuantita()).isEqualByComparingTo(new BigDecimal("150"));
        verify(ingredienteMagazzinoRepository).save(existing);
    }

    @Test
    @DisplayName("caricaMerci - ingrediente non trovato lancia eccezione")
    void caricaMerci_ingredienteNotFound_throwsException() {
        when(ingredienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.caricaMerci(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ingrediente");
    }

    @Test
    @DisplayName("caricaMerci - magazzino non trovato lancia eccezione")
    void caricaMerci_magazzinoNotFound_throwsException() {
        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
        when(magazzinoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.caricaMerci(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Magazzino");
    }

    @Test
    @DisplayName("getAllIngredienteMagazzino - restituisce lista mappata")
    void getAllIngredienteMagazzino_returnsMappedList() {
        IngredienteMagazzino im = new IngredienteMagazzino();
        when(ingredienteMagazzinoRepository.findAll()).thenReturn(List.of(im));
        when(ingredienteMagazzinoMapper.toDto(im)).thenReturn(mockResponse);

        List<IngredienteMagazzinoResponse> result = service.getAllIngredienteMagazzino();

        assertThat(result).hasSize(1).contains(mockResponse);
    }
}
