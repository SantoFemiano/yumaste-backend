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
    @DisplayName("getAllFornitori - restituisce lista")
    void getAllFornitori() {
        when(fornitoreRepository.findAll()).thenReturn(List.of(fornitore));
        assertThat(fornitoreService.getAllFornitori()).hasSize(1);
    }

    @Test
    @DisplayName("getFornitoreById - trovato")
    void getFornitoreById_found() {
        when(fornitoreRepository.findById(1L)).thenReturn(Optional.of(fornitore));
        assertThat(fornitoreService.getFornitoreById(1L)).isEqualTo(fornitore);
    }

    @Test
    @DisplayName("getFornitoreById - non trovato lancia ResourceNotFoundException")
    void getFornitoreById_notFound() {
        when(fornitoreRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fornitoreService.getFornitoreById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("creaFornitore - salva e restituisce fornitore")
    void creaFornitore() {
        when(fornitoreRepository.save(fornitore)).thenReturn(fornitore);
        assertThat(fornitoreService.creaFornitore(fornitore)).isEqualTo(fornitore);
    }

    @Test
    @DisplayName("eliminaFornitore - trovato, elimina")
    void eliminaFornitore_found() {
        when(fornitoreRepository.findById(1L)).thenReturn(Optional.of(fornitore));
        fornitoreService.eliminaFornitore(1L);
        verify(fornitoreRepository).delete(fornitore);
    }

    @Test
    @DisplayName("eliminaFornitore - non trovato lancia ResourceNotFoundException")
    void eliminaFornitore_notFound() {
        when(fornitoreRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fornitoreService.eliminaFornitore(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
