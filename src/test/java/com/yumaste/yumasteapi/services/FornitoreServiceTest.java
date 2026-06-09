package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
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
    @InjectMocks private FornitoreService fornitoreService;

    private Fornitore fornitore;

    @BeforeEach
    void setUp() {
        fornitore = new Fornitore();
        fornitore.setId(1L);
        fornitore.setNome("Fornitore Test");
    }

    @Test
    @DisplayName("getAll - restituisce lista fornitori")
    void getAll() {
        when(fornitoreRepository.findAll()).thenReturn(List.of(fornitore));
        assertThat(fornitoreService.getAll()).hasSize(1);
    }

    @Test
    @DisplayName("getById - trovato")
    void getById_found() {
        when(fornitoreRepository.findById(1L)).thenReturn(Optional.of(fornitore));
        assertThat(fornitoreService.getById(1L)).isEqualTo(fornitore);
    }

    @Test
    @DisplayName("getById - non trovato lancia ResourceNotFoundException")
    void getById_notFound() {
        when(fornitoreRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fornitoreService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("save - salva e restituisce fornitore")
    void save() {
        when(fornitoreRepository.save(fornitore)).thenReturn(fornitore);
        assertThat(fornitoreService.save(fornitore)).isEqualTo(fornitore);
    }

    @Test
    @DisplayName("delete - trovato, elimina")
    void delete_found() {
        when(fornitoreRepository.findById(1L)).thenReturn(Optional.of(fornitore));
        fornitoreService.delete(1L);
        verify(fornitoreRepository).delete(fornitore);
    }

    @Test
    @DisplayName("delete - non trovato lancia ResourceNotFoundException")
    void delete_notFound() {
        when(fornitoreRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fornitoreService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
