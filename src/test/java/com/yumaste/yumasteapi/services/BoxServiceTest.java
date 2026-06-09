package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock private BoxRepository boxRepository;
    // BoxService dipende anche da altri service/repo — aggiungiamo i mock necessari
    @Mock private com.yumaste.yumasteapi.repositories.IngredienteRepository ingredienteRepository;
    @Mock private com.yumaste.yumasteapi.repositories.AllergeneRepository allergeneRepository;
    @Mock private com.yumaste.yumasteapi.repositories.BoxIngredienteRepository boxIngredienteRepository;
    @Mock private com.yumaste.yumasteapi.mapper.BoxMapper boxMapper;

    @InjectMocks private BoxService boxService;

    private Box box;

    @BeforeEach
    void setUp() {
        box = new Box();
        box.setId(1L);
        box.setNome("Box Vegana");
        box.setPrezzo(new BigDecimal("20.00"));
        box.setAttivo(true);
    }

    @Test
    @DisplayName("getAllBoxAttive - restituisce solo box attive")
    void getAllBoxAttive() {
        when(boxRepository.findByAttivoTrue()).thenReturn(List.of(box));
        assertThat(boxService.getAllBoxAttive()).hasSize(1);
    }

    @Test
    @DisplayName("getBoxById - trovata")
    void getBoxById_found() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        assertThat(boxService.getBoxById(1L)).isEqualTo(box);
    }

    @Test
    @DisplayName("getBoxById - non trovata lancia ResourceNotFoundException")
    void getBoxById_notFound() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boxService.getBoxById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("attivaBox - box non trovata lancia ResourceNotFoundException")
    void attivaBox_notFound() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boxService.attivaBox(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("attivaBox - box disattivata viene attivata")
    void attivaBox_success() {
        box.setAttivo(false);
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(boxRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boxService.attivaBox(1L);

        assertThat(box.getAttivo()).isTrue();
        verify(boxRepository).save(box);
    }

    @Test
    @DisplayName("disattivaBox - box non trovata lancia ResourceNotFoundException")
    void disattivaBox_notFound() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boxService.disattivaBox(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("disattivaBox - box attiva viene disattivata")
    void disattivaBox_success() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(boxRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boxService.disattivaBox(1L);

        assertThat(box.getAttivo()).isFalse();
        verify(boxRepository).save(box);
    }
}
