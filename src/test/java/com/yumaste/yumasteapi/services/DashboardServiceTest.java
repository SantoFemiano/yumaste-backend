package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.OrdineRepository;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

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
    @DisplayName("getDashboardStats - restituisce mappa con dati statistici")
    void getDashboardStats_returnsStatsMap() {
        when(utenteRepository.count()).thenReturn(10L);
        when(boxRepository.count()).thenReturn(5L);
        when(ordineRepository.count()).thenReturn(25L);

        Map<String, Long> stats = dashboardService.getDashboardStats();

        assertThat(stats).isNotNull();
        assertThat(stats).containsKey("utenti");
        assertThat(stats).containsKey("box");
        assertThat(stats).containsKey("ordini");
        assertThat(stats.get("utenti")).isEqualTo(10L);
        assertThat(stats.get("box")).isEqualTo(5L);
        assertThat(stats.get("ordini")).isEqualTo(25L);
    }
}
