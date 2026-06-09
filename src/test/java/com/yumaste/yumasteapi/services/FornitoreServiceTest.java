package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.FornitoreRequestDTO;
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

    @Mock FornitoreRepository fornitoreRepository;
    @Mock FornitoreMapper fornitoreMapper;
    @InjectMocks FornitoreService fornitoreService;

    private Fornitore fornitore;

    @BeforeEach
    void setUp() {
        fornitore = new Fornitore();
        fornitore.setId(1L);
        fornitore.setNome("Fornitore SRL");
    }

    @Test
    @DisplayName("getAllFornitori - restituisce lista mappata")
    void getAllFornitori_returnsMappedList() {
        FornitoreResponseDTO dto = mock(FornitoreResponseDTO.class);
        when(fornitoreRepository.findAll()).thenReturn(List.of(fornitore));
        when(fornitoreMapper.toResponseDTO(fornitore)).thenReturn(dto);

        List<FornitoreResponseDTO> result = fornitoreService.getAllFornitori();
        assertThat(result).containsExactly(dto);
    }

    @Test
    @DisplayName("createFornitore - salva e restituisce DTO")
    void createFornitore_savesAndReturnsDTO() {
        FornitoreRequestDTO req = mock(FornitoreRequestDTO.class);
        FornitoreResponseDTO dto = mock(FornitoreResponseDTO.class);
        when(fornitoreMapper.toEntity(req)).thenReturn(fornitore);
        when(fornitoreRepository.save(fornitore)).thenReturn(fornitore);
        when(fornitoreMapper.toResponseDTO(fornitore)).thenReturn(dto);

        FornitoreResponseDTO result = fornitoreService.createFornitore(req);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("updateFornitore - aggiorna e restituisce DTO")
    void updateFornitore_updatesAndReturns() {
        FornitoreRequestDTO req = mock(FornitoreRequestDTO.class);
        FornitoreResponseDTO dto = mock(FornitoreResponseDTO.class);
        when(fornitoreRepository.findById(1L)).thenReturn(Optional.of(fornitore));
        when(fornitoreMapper.toEntity(req)).thenReturn(fornitore);
        when(fornitoreRepository.save(fornitore)).thenReturn(fornitore);
        when(fornitoreMapper.toResponseDTO(fornitore)).thenReturn(dto);

        FornitoreResponseDTO result = fornitoreService.updateFornitore(1L, req);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("updateFornitore - lancia eccezione se non trovato")
    void updateFornitore_notFound_throws() {
        when(fornitoreRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fornitoreService.updateFornitore(99L, mock(FornitoreRequestDTO.class)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteFornitore - elimina se trovato")
    void deleteFornitore_deletesIfFound() {
        when(fornitoreRepository.findById(1L)).thenReturn(Optional.of(fornitore));
        fornitoreService.deleteFornitore(1L);
        verify(fornitoreRepository).delete(fornitore);
    }

    @Test
    @DisplayName("deleteFornitore - lancia eccezione se non trovato")
    void deleteFornitore_notFound_throws() {
        when(fornitoreRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fornitoreService.deleteFornitore(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
