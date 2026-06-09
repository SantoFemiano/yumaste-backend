package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.DashboardStatsDTO;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.OrdineRepository;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock OrdineRepository ordineRepository;
    @Mock UtenteRepository utenteRepository;
    @Mock BoxRepository boxRepository;

    @InjectMocks DashboardService dashboardService;

    @Test
    @DisplayName("getStats - restituisce DTO con valori dai repository")
    void getStats_returnsStatsDTO() {
        when(ordineRepository.countOrdiniValidi()).thenReturn(10L);
        when(ordineRepository.sumIncassoTotale()).thenReturn(BigDecimal.valueOf(500.00));
        when(utenteRepository.countByRuolo("ROLE_USER")).thenReturn(5L);
        when(boxRepository.countByAttivoTrue()).thenReturn(3L);

        DashboardStatsDTO result = dashboardService.getStats();

        assertThat(result.totaleOrdini()).isEqualTo(10L);
        assertThat(result.incassoTotale()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(result.totaleClienti()).isEqualTo(5L);
        assertThat(result.boxAttive()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getStats - gestisce null dai repository (default a 0)")
    void getStats_handlesNullValues() {
        when(ordineRepository.countOrdiniValidi()).thenReturn(null);
        when(ordineRepository.sumIncassoTotale()).thenReturn(null);
        when(utenteRepository.countByRuolo("ROLE_USER")).thenReturn(null);
        when(boxRepository.countByAttivoTrue()).thenReturn(null);

        DashboardStatsDTO result = dashboardService.getStats();

        assertThat(result.totaleOrdini()).isZero();
        assertThat(result.incassoTotale()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totaleClienti()).isZero();
        assertThat(result.boxAttive()).isZero();
    }
}
