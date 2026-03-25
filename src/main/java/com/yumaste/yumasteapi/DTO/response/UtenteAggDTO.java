package com.yumaste.yumasteapi.DTO.response;


public record UtenteAggDTO(
        Long id,
        String nome,
        String cognome,
        String email,
        String cf
) {
}
