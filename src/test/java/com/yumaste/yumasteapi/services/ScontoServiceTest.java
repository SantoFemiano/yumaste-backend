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
        sconto.setValore(10);
    }

    @Test
    @DisplayName("getAllSconti - restituisce lista")
    void getAllSconti() {
        when(scontoRepository.findAll()).thenReturn(List.of(sconto));
        assertThat(scontoService.getAllSconti()).hasSize(1);
    }

    @Test
    @DisplayName("getScontoById - trovato")
    void getScontoById_found() {
        when(scontoRepository.findById(1L)).thenReturn(Optional.of(sconto));
        assertThat(scontoService.getScontoById(1L)).isEqualTo(sconto);
    }

    @Test
    @DisplayName("getScontoById - non trovato lancia ResourceNotFoundException")
    void getScontoById_notFound() {
        when(scontoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scontoService.getScontoById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminaSconto - trovato, elimina")
    void eliminaSconto_found() {
        when(scontoRepository.findById(1L)).thenReturn(Optional.of(sconto));
        scontoService.eliminaSconto(1L);
        verify(scontoRepository).delete(sconto);
    }

    @Test
    @DisplayName("eliminaSconto - non trovato lancia ResourceNotFoundException")
    void eliminaSconto_notFound() {
        when(scontoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scontoService.eliminaSconto(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
