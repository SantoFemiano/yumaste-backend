package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.repositories.AllergeneRepository;
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
class AllergeneServiceTest {

    @Mock private AllergeneRepository allergeneRepository;
    @InjectMocks private AllergeneService allergeneService;

    private Allergene allergene;

    @BeforeEach
    void setUp() {
        allergene = new Allergene();
        allergene.setId(1L);
        allergene.setNome("Glutine");
    }

    @Test
    @DisplayName("getAllAllergeni - restituisce lista")
    void getAllAllergeni() {
        when(allergeneRepository.findAll()).thenReturn(List.of(allergene));
        assertThat(allergeneService.getAllAllergeni()).hasSize(1);
    }

    @Test
    @DisplayName("getById - trovato")
    void getById_found() {
        when(allergeneRepository.findById(1L)).thenReturn(Optional.of(allergene));
        assertThat(allergeneService.getById(1L)).isEqualTo(allergene);
    }

    @Test
    @DisplayName("getById - non trovato lancia ResourceNotFoundException")
    void getById_notFound() {
        when(allergeneRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> allergeneService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("save - salva e restituisce allergene")
    void save() {
        when(allergeneRepository.save(allergene)).thenReturn(allergene);
        assertThat(allergeneService.save(allergene)).isEqualTo(allergene);
    }

    @Test
    @DisplayName("delete - trovato, elimina")
    void delete_found() {
        when(allergeneRepository.findById(1L)).thenReturn(Optional.of(allergene));
        allergeneService.delete(1L);
        verify(allergeneRepository).delete(allergene);
    }

    @Test
    @DisplayName("delete - non trovato lancia ResourceNotFoundException")
    void delete_notFound() {
        when(allergeneRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> allergeneService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
