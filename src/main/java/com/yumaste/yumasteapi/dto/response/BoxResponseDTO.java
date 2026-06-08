package com.yumaste.yumasteapi.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record BoxResponseDTO(
        Long id,
        String ean,
        String nome,
        String categoria,
        Byte porzioni,
        Integer quantitaInBox,
        BigDecimal prezzoOriginale,
        BigDecimal prezzoScontato,
        Integer percentualeSconto,
        String immagineUrl,
        Boolean attivo
)implements Serializable {}