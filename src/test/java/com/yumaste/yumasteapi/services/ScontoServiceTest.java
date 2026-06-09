package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.ScontoRequestDTO;
import com.yumaste.yumasteapi.exceptions.BusinessException;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.models.Sconto;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScontoServiceTest {

    @Mock private ScontoRepository scontoRepository;
    @Mock private BoxRepository boxRepository;

    @InjectMocks private ScontoService scontoService;

    private ScontoRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ScontoRequestDTO(
                "Promo Estate",
                20,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10),
                null,
                null
        );
    }

    @Test
    @DisplayName("getAllSconti - restituisce tutti gli sconti")
    void getAllSconti() {
        Sconto s1 = new Sconto(); s1.setId(1L);
        Sconto s2 = new Sconto(); s2.setId(2L);
        when(scontoRepository.findAll()).thenReturn(List.of(s1, s2));

        List<Sconto> result = scontoService.getAllSconti();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getScontoById - ID esistente restituisce sconto")
    void getScontoById_found() {
        Sconto sconto = new Sconto(); sconto.setId(1L);
        when(scontoRepository.findById(1L)).thenReturn(Optional.of(sconto));

        Sconto result = scontoService.getScontoById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getScontoById - ID non trovato lancia ResourceNotFoundException")
    void getScontoById_notFound() {
        when(scontoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scontoService.getScontoById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("creaSconto - data fine prima di data inizio lancia BusinessException")
    void creaSconto_invalidDates() {
        ScontoRequestDTO bad = new ScontoRequestDTO(
                "Bad", 10,
                LocalDate.now().plusDays(5),
                LocalDate.now(),
                null, null
        );
        assertThatThrownBy(() -> scontoService.creaSconto(bad))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("creaSconto - valore sconto fuori range lancia BusinessException")
    void creaSconto_invalidValue() {
        ScontoRequestDTO bad = new ScontoRequestDTO(
                "Bad", 101,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                null, null
        );
        assertThatThrownBy(() -> scontoService.creaSconto(bad))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("creaSconto - sconto valido viene salvato")
    void creaSconto_valid() {
        when(scontoRepository.save(any(Sconto.class))).thenAnswer(inv -> inv.getArgument(0));

        Sconto result = scontoService.creaSconto(validRequest);

        assertThat(result.getNome()).isEqualTo("Promo Estate");
        verify(scontoRepository).save(any(Sconto.class));
    }

    @Test
    @DisplayName("eliminaSconto - sconto non trovato lancia ResourceNotFoundException")
    void eliminaSconto_notFound() {
        when(scontoRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scontoService.eliminaSconto(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminaSconto - sconto trovato viene eliminato")
    void eliminaSconto_success() {
        Sconto sconto = new Sconto(); sconto.setId(5L);
        when(scontoRepository.findById(5L)).thenReturn(Optional.of(sconto));

        scontoService.eliminaSconto(5L);

        verify(scontoRepository).delete(sconto);
    }
}
