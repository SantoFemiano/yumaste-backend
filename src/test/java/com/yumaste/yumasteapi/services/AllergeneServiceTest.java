package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.repositories.AllergeneRepository;
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

    @Test
    @DisplayName("getAllAllergeni - restituisce lista completa")
    void getAllAllergeni() {
        Allergene a = new Allergene(); a.setId(1L); a.setNome("Glutine");
        when(allergeneRepository.findAll()).thenReturn(List.of(a));
        assertThat(allergeneService.getAllAllergeni()).hasSize(1);
    }

    @Test
    @DisplayName("getAllergeneById - trovato")
    void getAllergeneById_found() {
        Allergene a = new Allergene(); a.setId(1L);
        when(allergeneRepository.findById(1L)).thenReturn(Optional.of(a));
        assertThat(allergeneService.getAllergeneById(1L)).isEqualTo(a);
    }

    @Test
    @DisplayName("getAllergeneById - non trovato lancia ResourceNotFoundException")
    void getAllergeneById_notFound() {
        when(allergeneRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> allergeneService.getAllergeneById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("saveAllergene - salva e restituisce allergene")
    void saveAllergene() {
        Allergene a = new Allergene(); a.setNome("Latte");
        when(allergeneRepository.save(a)).thenReturn(a);
        assertThat(allergeneService.saveAllergene(a)).isEqualTo(a);
        verify(allergeneRepository).save(a);
    }

    @Test
    @DisplayName("deleteAllergene - non trovato lancia ResourceNotFoundException")
    void deleteAllergene_notFound() {
        when(allergeneRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> allergeneService.deleteAllergene(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteAllergene - trovato viene eliminato")
    void deleteAllergene_success() {
        Allergene a = new Allergene(); a.setId(5L);
        when(allergeneRepository.findById(5L)).thenReturn(Optional.of(a));
        allergeneService.deleteAllergene(5L);
        verify(allergeneRepository).delete(a);
    }
}
