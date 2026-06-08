package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.DashboardStatsDTO;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.OrdineRepository;
import com.yumaste.yumasteapi.repositories.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrdineRepository ordineRepository;
    private final UtenteRepository utenteRepository;
    private final BoxRepository boxRepository;

    @Cacheable(value = "statistiche")
    public DashboardStatsDTO getStats() {
        Long totaleOrdini = ordineRepository.countOrdiniValidi();
        if (totaleOrdini == null) totaleOrdini = 0L;

        BigDecimal incasso = ordineRepository.sumIncassoTotale();
        if (incasso == null) incasso = BigDecimal.ZERO;

        Long totaleClienti = utenteRepository.countByRuolo("ROLE_USER");
        if (totaleClienti == null) totaleClienti = 0L;

        Long boxAttive = boxRepository.countByAttivoTrue();
        if (boxAttive == null) boxAttive = 0L;

        return new DashboardStatsDTO(totaleOrdini, incasso, totaleClienti, boxAttive);
    }
}