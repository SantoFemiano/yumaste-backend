package com.yumaste.yumasteapi.DTO.response;

import java.util.List;

public record UtenteProfileDTO(
        Long id,
        String nome,
        String cognome,
        String email,
        String cf,
        List<IndirizzoResponseDTO> indirizzi
) {}