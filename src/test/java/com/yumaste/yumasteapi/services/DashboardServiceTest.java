package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.repositories.OrdineRepository;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock OrdineRepository ordineRepository;
    @Mock UtenteRepository utenteRepository;
    @Mock BoxRepository boxRepository;

    @InjectMocks DashboardService dashboardService;

    @Test
    @DisplayName("getDashboardStats - restituisce mappa con statistiche")
    void getDashboardStats_returnsStats() {
        when(ordineRepository.count()).thenReturn(10L);
        when(utenteRepository.count()).thenReturn(5L);
        when(boxRepository.count()).thenReturn(3L);
        when(ordineRepository.sumTotaleOrdini()).thenReturn(BigDecimal.valueOf(500.00));

        Map<String, Object> result = dashboardService.getDashboardStats();

        assertThat(result).containsKey("totalOrdini");
        assertThat(result).containsKey("totalUtenti");
        assertThat(result).containsKey("totalBox");
        assertThat(result).containsKey("totalRevenue");
    }
}
