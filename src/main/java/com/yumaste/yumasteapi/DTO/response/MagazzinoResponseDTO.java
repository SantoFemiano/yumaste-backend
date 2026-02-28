package com.yumaste.yumasteapi.DTO.response;

public record MagazzinoResponseDTO(
        String nome,
        String via,
        String civico,
        String cap,
        String citta,
        String provincia
) {
}
