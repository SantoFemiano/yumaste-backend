package com.yumaste.yumasteapi.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record IngredientiConValoriDTO(
        String nomeIngrediente,
        BigDecimal quantitaNellaBox,
        String unitaMisura,

        BigDecimal chilocalorie,
        BigDecimal proteine,
        BigDecimal carboidrati,
        BigDecimal zuccheri,
        BigDecimal fibre,
        BigDecimal grassi,
        BigDecimal sale
)implements Serializable {
}