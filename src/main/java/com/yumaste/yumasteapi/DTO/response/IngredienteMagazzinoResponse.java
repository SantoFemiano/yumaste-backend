package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngredienteMagazzinoResponse(
        Long ingredienteMagazzinoId,
        Long magazzinoId,
        Long ingredienteId,
        String nomeMagazzino,
        String nomeIngrediente,
        BigDecimal quantita,
        String unitaMisura,
        String lotto,
        LocalDate dataIngresso)
{}
