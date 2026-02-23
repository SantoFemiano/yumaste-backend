package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;

public record BoxIngredientDTO (
    String nomeIngrediente,
    BigDecimal quantita,
    String unita
) {}