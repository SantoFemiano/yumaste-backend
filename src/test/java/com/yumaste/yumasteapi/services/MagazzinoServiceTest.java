package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.repositories.MagazzinoRepository;
import org.junit.jupiter.api.BeforeEach;
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
    @InjectMocks private MagazzinoService magazzinoService;

    private Magazzino magazzino;

    @BeforeEach
    void setUp() {
        magazzino = new Magazzino();
        magazzino.setId(1L);
        magazzino.setNome("Magazzino Centrale");
    }

    @Test
    @DisplayName("getAllMagazzini - restituisce lista")
    void getAllMagazzini() {
        when(magazzinoRepository.findAll()).thenReturn(List.of(magazzino));
        assertThat(magazzinoService.getAllMagazzini()).hasSize(1);
    }

    @Test
    @DisplayName("getMagazzinoById - trovato")
    void getMagazzinoById_found() {
        when(magazzinoRepository.findById(1L)).thenReturn(Optional.of(magazzino));
        assertThat(magazzinoService.getMagazzinoById(1L)).isEqualTo(magazzino);
    }

    @Test
    @DisplayName("getMagazzinoById - non trovato lancia ResourceNotFoundException")
    void getMagazzinoById_notFound() {
        when(magazzinoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> magazzinoService.getMagazzinoById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
