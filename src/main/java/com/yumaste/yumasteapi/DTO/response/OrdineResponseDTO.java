package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OrdineResponseDTO(
        Long id,
        String codiceOrdine,
        Instant dataOrdine,
        BigDecimal totalePrezzo,
        String statoOrdine,
        String statoSpedizione
) {
}
