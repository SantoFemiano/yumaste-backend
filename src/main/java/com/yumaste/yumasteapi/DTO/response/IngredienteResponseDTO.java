package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;

public record IngredienteResponseDTO(
        Long id,
        String ean,
        String partitaIvaFornitore,
        Long fornitoreId,
        String nomeFornitore,
        String nome,
        String descrizione,
        String unitaMisura,
        BigDecimal prezzoPerUnita,
        Boolean attivo
) {}