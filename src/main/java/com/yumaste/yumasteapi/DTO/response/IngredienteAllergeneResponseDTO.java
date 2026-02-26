package com.yumaste.yumasteapi.DTO.response;

public record IngredienteAllergeneResponseDTO(
        Long ingredienteId,
        String ingredienteNome,
        Long allergeneId,
        String allergeneNome
) {
}
