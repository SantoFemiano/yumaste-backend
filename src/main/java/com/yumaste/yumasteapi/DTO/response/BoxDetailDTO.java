package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;
import java.util.List;

public record BoxDetailDTO(
        Long id,
        String nome,
        String categoria,
        BigDecimal prezzoOriginale,
        BigDecimal prezzoScontato,
        Integer percentualeSconto,
        String immagineUrl,
        NutritionalValueDetailDTO macroTotali, // La somma calcolata
        List<String> allergeni,            // La lista unica
        List<IngredientiConValoriDTO> ingredienti // Il DTO che hai già creato!
) {
}
