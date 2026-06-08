package com.yumaste.yumasteapi.dto.request;

public record MagazzinoRequestDTO(
        String nome,
        String via,
        String civico,
        String cap,
        String citta,
        String provincia
) {
}
