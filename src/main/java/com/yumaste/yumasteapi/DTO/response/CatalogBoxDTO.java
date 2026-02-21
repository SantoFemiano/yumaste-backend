package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;

public record CatalogBoxDTO(
    Long id,
    String ean,
    String nome,
    String categoria,
    BigDecimal prezzo,
    Byte porzioni,
    String immagineUrl
) {}
