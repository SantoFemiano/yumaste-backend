package com.yumaste.yumasteapi.dto.response;

import java.io.Serializable;

public record AllergeneDTO(
        Long id,
        String nome,
        String descrizione)implements Serializable {
}
