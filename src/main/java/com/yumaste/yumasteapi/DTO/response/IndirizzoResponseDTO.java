package com.yumaste.yumasteapi.DTO.response;

public record IndirizzoResponseDTO(
        Long id,
        String via,
        String civico,
        String cap,
        String citta,
        String provincia,
        String stato
) {
}
