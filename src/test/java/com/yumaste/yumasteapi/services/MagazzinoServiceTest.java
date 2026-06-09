package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.MagazzinoResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.MagazzinoMapper;
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
    @Mock private MagazzinoMapper magazzinoMapper;
    @InjectMocks private MagazzinoService magazzinoService;

    private Magazzino magazzino;
    private MagazzinoResponseDTO magazzinoDTO;

    @BeforeEach
    void setUp() {
        magazzino = new Magazzino();
        magazzino.setId(1L);
        magazzino.setNome("Magazzino Centrale");
        magazzinoDTO = mock(MagazzinoResponseDTO.class);
    }

    @Test
    @DisplayName("getAllMagazzino - restituisce lista DTO")
    void getAllMagazzino() {
        when(magazzinoRepository.findAll()).thenReturn(List.of(magazzino));
        when(magazzinoMapper.toDto(magazzino)).thenReturn(magazzinoDTO);

        List<MagazzinoResponseDTO> result = magazzinoService.getAllMagazzino();

        assertThat(result).hasSize(1);
        verify(magazzinoRepository).findAll();
    }

    @Test
    @DisplayName("deleteMagazzino - trovato, elimina")
    void deleteMagazzino_found() {
        when(magazzinoRepository.findById(1L)).thenReturn(Optional.of(magazzino));

        magazzinoService.deleteMagazzino(1L);

        verify(magazzinoRepository).delete(magazzino);
    }

    @Test
    @DisplayName("deleteMagazzino - non trovato lancia ResourceNotFoundException")
    void deleteMagazzino_notFound() {
        when(magazzinoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> magazzinoService.deleteMagazzino(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
