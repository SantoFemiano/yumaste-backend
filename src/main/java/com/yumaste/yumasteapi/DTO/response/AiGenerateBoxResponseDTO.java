package com.yumaste.yumasteapi.DTO.response;

import java.util.List;

public record AiGenerateBoxResponseDTO(
        String nome,
        String descrizione,
        String categoria,
        double prezzo,
        int porzioni,
        String urlImmagine,
        List<AiBoxIngredientDTO> ingredienti
) {}