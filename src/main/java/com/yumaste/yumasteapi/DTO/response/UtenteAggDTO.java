package com.yumaste.yumasteapi.DTO.response;


import java.io.Serializable;

public record UtenteAggDTO(
        Long id,
        String nome,
        String cognome,
        String email,
        String cf
)implements Serializable {
}
