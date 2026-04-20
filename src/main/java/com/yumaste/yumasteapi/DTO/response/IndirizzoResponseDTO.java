package com.yumaste.yumasteapi.DTO.response;

import java.io.Serializable;

public record IndirizzoResponseDTO(
        Long id,
        String via,
        String civico,
        String cap,
        String citta,
        String provincia,
        String stato
)implements Serializable {
}
