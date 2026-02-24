package com.yumaste.yumasteapi.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IndirizzoRequestDTO(
        @NotBlank(message = "La via è obbligatoria")
        @Size(max = 100, message = "La via non può superare i 100 caratteri")
        String via,

        @NotBlank(message = "Il civico è obbligatorio")
        @Size(max = 10, message = "Il civico non può superare i 10 caratteri")
        String civico,

        @NotBlank(message = "Il CAP è obbligatorio")
        @Size(min = 5, max = 5, message = "Il CAP deve essere di 5 caratteri")
        String cap,

        @NotBlank(message = "La città è obbligatoria")
        @Size(max = 100, message = "La città non può superare i 100 caratteri")
        String citta,

        @NotBlank(message = "La provincia è obbligatoria")
        @Size(min = 2, max = 4, message = "La provincia deve essere valida (es. RM)")
        String provincia
) {
}
