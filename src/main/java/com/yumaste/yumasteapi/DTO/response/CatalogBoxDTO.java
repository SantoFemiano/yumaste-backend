package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;

public record CatalogBoxDTO(
    Long id,
    String ean,
    String nome,
    String categoria,
    BigDecimal prezzo,
    BigDecimal prezzoScontato,
    Integer percentualeSconto,
    Byte porzioni,
    String immagineUrl
) {}
