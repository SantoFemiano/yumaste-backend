package com.yumaste.yumasteapi.DTO.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record NutritionalValueDetailDTO(
        BigDecimal proteine,
        BigDecimal carboidrati,
        BigDecimal grassi,
        BigDecimal zuccheri,
        BigDecimal fibre,
        BigDecimal sale,
        Integer chilocalorie
)implements Serializable {
}
