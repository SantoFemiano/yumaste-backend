package com.yumaste.yumasteapi.DTO.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record DashboardStatsDTO(
        Long totaleOrdini,
        BigDecimal incassoTotale,
        Long totaleClienti,
        Long boxAttive
)implements Serializable {
}
