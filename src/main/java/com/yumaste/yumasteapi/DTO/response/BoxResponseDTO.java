package com.yumaste.yumasteapi.DTO.response;

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
) {}