package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.repositories.MagazzinoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MagazzinoServiceTest {

    @Mock private MagazzinoRepository magazzinoRepository;
    @Mock private com.yumaste.yumasteapi.repositories.IngredienteRepository ingredienteRepository;
    @InjectMocks private MagazzinoService magazzinoService;

    @Test
    @DisplayName("getAllMagazzino - restituisce lista")
    void getAllMagazzino() {
        Magazzino m = new Magazzino();
        when(magazzinoRepository.findAll()).thenReturn(List.of(m));
        assertThat(magazzinoService.getAllMagazzino()).hasSize(1);
    }

    @Test
    @DisplayName("getMagazzinoById - trovato")
    void getMagazzinoById_found() {
        Magazzino m = new Magazzino(); m.setId(1L);
        when(magazzinoRepository.findById(1L)).thenReturn(Optional.of(m));
        assertThat(magazzinoService.getMagazzinoById(1L)).isEqualTo(m);
    }

    @Test
    @DisplayName("getMagazzinoById - non trovato lancia ResourceNotFoundException")
    void getMagazzinoById_notFound() {
        when(magazzinoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> magazzinoService.getMagazzinoById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
