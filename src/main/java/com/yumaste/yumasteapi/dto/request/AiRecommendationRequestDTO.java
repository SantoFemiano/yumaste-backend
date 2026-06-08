package com.yumaste.yumasteapi.dto.request;

import java.util.List;

public record AiRecommendationRequestDTO(
        String obiettivo,          // es. "Dimagrimento", "Massa muscolare", "Mangiare sano"
        String tipoDieta,          // es. "Onnivora", "Vegetariana", "Vegana"
        List<String> allergeni,    // es. ["Glutine", "Lattosio"]
        Integer calorieGiornaliere // es. 2000
) {}