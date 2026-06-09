package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.ScontoResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.ScontoBoxMapper;
import com.yumaste.yumasteapi.mapper.ScontoMapper;
import com.yumaste.yumasteapi.models.Sconto;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.ScontoBoxRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
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
    @Mock private BoxRepository boxRepository;
    @Mock private ScontoMapper scontoMapper;
    @Mock private ScontoBoxMapper scontoBoxMapper;
    @InjectMocks private ScontoService scontoService;

    private Sconto sconto;
    private ScontoResponseDTO scontoDTO;

    @BeforeEach
    void setUp() {
        sconto = new Sconto();
        sconto.setId(1L);
        sconto.setValore(10);
        scontoDTO = mock(ScontoResponseDTO.class);
    }

    @Test
    @DisplayName("getSconti - restituisce lista DTO")
    void getSconti() {
        when(scontoRepository.findAll()).thenReturn(List.of(sconto));
        when(scontoMapper.toDto(sconto)).thenReturn(scontoDTO);

        List<ScontoResponseDTO> result = scontoService.getSconti();

        assertThat(result).hasSize(1);
        verify(scontoRepository).findAll();
    }

    @Test
    @DisplayName("deleteSconto - trovato e senza box associate, elimina")
    void deleteSconto_found() {
        when(scontoBoxRepository.existsBySconto_Id(1L)).thenReturn(false);
        when(scontoRepository.findById(1L)).thenReturn(Optional.of(sconto));

        scontoService.deleteSconto(1L);

        verify(scontoRepository).delete(sconto);
    }

    @Test
    @DisplayName("deleteSconto - non trovato lancia ResourceNotFoundException")
    void deleteSconto_notFound() {
        when(scontoBoxRepository.existsBySconto_Id(99L)).thenReturn(false);
        when(scontoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scontoService.deleteSconto(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteSconto - con box associate lancia BusinessException")
    void deleteSconto_withBoxAssociate() {
        when(scontoBoxRepository.existsBySconto_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> scontoService.deleteSconto(1L))
                .isInstanceOf(com.yumaste.yumasteapi.exceptions.BusinessException.class);
    }
}
