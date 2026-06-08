package com.yumaste.yumasteapi.dto.response;

import java.io.Serializable;

public record IngredienteAllergeneResponseDTO(
        Long ingredienteId,
        String ingredienteNome,
        Long allergeneId,
        String allergeneNome
)implements Serializable {
}
