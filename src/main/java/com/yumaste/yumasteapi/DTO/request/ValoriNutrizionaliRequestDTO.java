package com.yumaste.yumasteapi.DTO.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ValoriNutrizionaliRequestDTO(
        @NotNull BigDecimal proteine,
        @NotNull BigDecimal carboidrati,
        @NotNull BigDecimal zuccheri,
        @NotNull BigDecimal fibre,
        @NotNull BigDecimal grassi,
        @NotNull BigDecimal sale,
        @NotNull Integer chilocalorie
) {
}
