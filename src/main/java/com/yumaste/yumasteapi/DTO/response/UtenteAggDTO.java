package com.yumaste.yumasteapi.DTO.response;

import java.util.List;

public record UtenteAggDTO(
        Long id,
        String nome,
        String cognome,
        String email,
        String cf
) {
}
