package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;

public record NutritionalValueDetailDTO(
        BigDecimal proteine,
        BigDecimal carboidrati,
        BigDecimal grassi,
        BigDecimal zuccheri,
        BigDecimal fibre,
        BigDecimal sale,
        Integer chilocalorie
) {
}
