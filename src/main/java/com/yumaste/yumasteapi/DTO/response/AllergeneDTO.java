package com.yumaste.yumasteapi.DTO.response;

import java.io.Serializable;

public record AllergeneDTO(
        Long id,
        String nome,
        String descrizione)implements Serializable {
}
