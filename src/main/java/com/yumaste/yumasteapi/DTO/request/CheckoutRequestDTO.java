package com.yumaste.yumasteapi.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequestDTO(
        @NotNull(message = "L'indirizzo di spedizione è obbligatorio")
        Long indirizzoId,

        @NotBlank(message = "Il metodo di pagamento è obbligatorio")
        String metodoPagamento
) {
}
