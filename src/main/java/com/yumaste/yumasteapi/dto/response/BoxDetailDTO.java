package com.yumaste.yumasteapi.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record BoxDetailDTO(
        Long id,
        String nome,
        String categoria,
        Byte porzioni,
        BigDecimal prezzoOriginale,
        BigDecimal prezzoScontato,
        Integer percentualeSconto,
        String immagineUrl,
        NutritionalValueDetailDTO macroTotali, // La somma calcolata
        List<String> allergeni,            // La lista unica
        List<IngredientiConValoriDTO> ingredienti // Il DTO che hai già creato!
)implements Serializable {
}
