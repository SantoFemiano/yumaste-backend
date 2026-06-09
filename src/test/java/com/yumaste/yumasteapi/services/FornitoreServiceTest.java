package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.FornitoreResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.FornitoreMapper;
import com.yumaste.yumasteapi.models.Fornitore;
import com.yumaste.yumasteapi.repositories.FornitoreRepository;
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
class FornitoreServiceTest {

    @Mock private FornitoreRepository fornitoreRepository;
    @Mock private FornitoreMapper fornitoreMapper;
    @InjectMocks private FornitoreService fornitoreService;

    private Fornitore fornitore;
    private FornitoreResponseDTO fornitoreDTO;

    @BeforeEach
    void setUp() {
        fornitore = new Fornitore();
        fornitore.setId(1L);
        fornitore.setNome("Fornitore Test");
        fornitoreDTO = mock(FornitoreResponseDTO.class);
    }

    @Test
    @DisplayName("getAllFornitore - restituisce lista DTO")
    void getAllFornitore() {
        when(fornitoreRepository.findAll()).thenReturn(List.of(fornitore));
        when(fornitoreMapper.toDto(fornitore)).thenReturn(fornitoreDTO);

        List<FornitoreResponseDTO> result = fornitoreService.getAllFornitore();

        assertThat(result).hasSize(1);
        verify(fornitoreRepository).findAll();
    }

    @Test
    @DisplayName("deleteFornitore - trovato, elimina")
    void deleteFornitore_found() {
        when(fornitoreRepository.findById(1L)).thenReturn(Optional.of(fornitore));

        fornitoreService.deleteFornitore(1L);

        verify(fornitoreRepository).delete(fornitore);
    }

    @Test
    @DisplayName("deleteFornitore - non trovato lancia ResourceNotFoundException")
    void deleteFornitore_notFound() {
        when(fornitoreRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fornitoreService.deleteFornitore(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
