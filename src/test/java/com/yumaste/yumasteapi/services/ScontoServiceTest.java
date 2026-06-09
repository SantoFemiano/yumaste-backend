package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Sconto;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import com.yumaste.yumasteapi.repositories.ScontoBoxRepository;
import com.yumaste.yumasteapi.repositories.ScontoCategoriaRepository;
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
class ScontoServiceTest {

    @Mock private ScontoRepository scontoRepository;
    @Mock private ScontoBoxRepository scontoBoxRepository;
    @Mock private ScontoCategoriaRepository scontoCategoriaRepository;
    @InjectMocks private ScontoService scontoService;

    private Sconto sconto;

    @BeforeEach
    void setUp() {
        sconto = new Sconto();
        sconto.setId(1L);
        sconto.setCodice("PROMO10");
    }

    @Test
    @DisplayName("getAll - restituisce lista sconti")
    void getAll() {
        when(scontoRepository.findAll()).thenReturn(List.of(sconto));
        assertThat(scontoService.getAll()).hasSize(1);
    }

    @Test
    @DisplayName("getById - trovato")
    void getById_found() {
        when(scontoRepository.findById(1L)).thenReturn(Optional.of(sconto));
        assertThat(scontoService.getById(1L)).isEqualTo(sconto);
    }

    @Test
    @DisplayName("getById - non trovato lancia ResourceNotFoundException")
    void getById_notFound() {
        when(scontoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scontoService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete - trovato, elimina")
    void delete_found() {
        when(scontoRepository.findById(1L)).thenReturn(Optional.of(sconto));
        scontoService.delete(1L);
        verify(scontoRepository).delete(sconto);
    }

    @Test
    @DisplayName("delete - non trovato lancia ResourceNotFoundException")
    void delete_notFound() {
        when(scontoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scontoService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
