package com.yumaste.yumasteapi.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngredienteMagazzinoRequest(
        @NotNull Long ingredienteId,
        @NotNull Long magazzinoId,
        BigDecimal quantita,
        @NotBlank String lotto,
        @NotNull LocalDate dataIngresso
        ) {}
