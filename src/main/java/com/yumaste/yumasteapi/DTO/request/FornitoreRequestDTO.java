package com.yumaste.yumasteapi.DTO.request;

public record FornitoreRequestDTO(
        String partitaIva,
        String nome,
        String via,
        String civico,
        String cap,
        String citta,
        String provincia
) {
}
