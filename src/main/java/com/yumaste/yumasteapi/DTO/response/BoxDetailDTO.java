package com.yumaste.yumasteapi.DTO.response;

import java.util.List;

public record BoxDetailDTO(
        Long id,
        String nome,
        String categoria,
        Double prezzo,
        String immagineUrl,
        NutritionalValueDetailDTO macroTotali, // La somma calcolata
        List<String> allergeni,            // La lista unica
        List<IngredientiConValoriDTO> ingredienti // Il DTO che hai già creato!
) {
}
