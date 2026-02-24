package com.yumaste.yumasteapi.DTO.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddIngredienteToBoxRequestDTO(

        @NotNull(message = "L'ID dell'ingrediente è obbligatorio")
        Long ingredienteId,

        @NotNull(message = "La quantità è obbligatoria")
        @Positive(message = "La quantità deve essere maggiore di zero")
        BigDecimal quantita

) {}