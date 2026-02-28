package com.yumaste.yumasteapi.DTO.response;

public record FornitoreResponseDTO(
        String partitaIva,
        String nome,
        String via,
        String civico,
        String cap,
        String citta,
        String provincia
) {
}
