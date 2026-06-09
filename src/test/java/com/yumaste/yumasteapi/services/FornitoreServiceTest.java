package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Fornitore;
import com.yumaste.yumasteapi.repositories.FornitoreRepository;
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

    @Test
    @DisplayName("getAllFornitori - restituisce lista")
    void getAllFornitori() {
        Fornitore f = new Fornitore(); f.setId(1L);
        when(fornitoreRepository.findAll()).thenReturn(List.of(f));
        assertThat(fornitoreService.getAllFornitori()).hasSize(1);
    }

    @Test
    @DisplayName("getFornitoreById - trovato")
    void getFornitoreById_found() {
        Fornitore f = new Fornitore(); f.setId(1L);
        when(fornitoreRepository.findById(1L)).thenReturn(Optional.of(f));
        assertThat(fornitoreService.getFornitoreById(1L)).isEqualTo(f);
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
        Fornitore f = new Fornitore(); f.setNome("Fornitore SpA");
        when(fornitoreRepository.save(f)).thenReturn(f);
        assertThat(fornitoreService.creaFornitore(f)).isEqualTo(f);
    }

    @Test
    @DisplayName("eliminaFornitore - non trovato lancia ResourceNotFoundException")
    void eliminaFornitore_notFound() {
        when(fornitoreRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fornitoreService.eliminaFornitore(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminaFornitore - trovato viene eliminato")
    void eliminaFornitore_success() {
        Fornitore f = new Fornitore(); f.setId(5L);
        when(fornitoreRepository.findById(5L)).thenReturn(Optional.of(f));
        fornitoreService.eliminaFornitore(5L);
        verify(fornitoreRepository).delete(f);
    }
}
