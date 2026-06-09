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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private UtenteRepository utenteRepository;
    @Mock private BoxRepository boxRepository;
    @Mock private OrdineRepository ordineRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("getStats - restituisce DTO con dati statistici")
    void getStats_returnsDashboardStatsDTO() {
        when(ordineRepository.countOrdiniValidi()).thenReturn(25L);
        when(ordineRepository.sumIncassoTotale()).thenReturn(new BigDecimal("1500.00"));
        when(utenteRepository.countByRuolo("ROLE_USER")).thenReturn(10L);
        when(boxRepository.countByAttivoTrue()).thenReturn(5L);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertThat(stats).isNotNull();
        assertThat(stats.totaleOrdini()).isEqualTo(25L);
        assertThat(stats.incassoTotale()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(stats.totaleClienti()).isEqualTo(10L);
        assertThat(stats.boxAttive()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getStats - valori null dal repository vengono gestiti con 0")
    void getStats_nullValues_returnsZeros() {
        when(ordineRepository.countOrdiniValidi()).thenReturn(null);
        when(ordineRepository.sumIncassoTotale()).thenReturn(null);
        when(utenteRepository.countByRuolo("ROLE_USER")).thenReturn(null);
        when(boxRepository.countByAttivoTrue()).thenReturn(null);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertThat(stats).isNotNull();
        assertThat(stats.totaleOrdini()).isEqualTo(0L);
        assertThat(stats.incassoTotale()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stats.totaleClienti()).isEqualTo(0L);
        assertThat(stats.boxAttive()).isEqualTo(0L);
    }
}
