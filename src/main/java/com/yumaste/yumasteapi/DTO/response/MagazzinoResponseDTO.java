package com.yumaste.yumasteapi.DTO.response;

import java.io.Serializable;

public record MagazzinoResponseDTO(
        Long id,
        String nome,
        String via,
        String civico,
        String cap,
        String citta,
        String provincia
)implements Serializable {
}
