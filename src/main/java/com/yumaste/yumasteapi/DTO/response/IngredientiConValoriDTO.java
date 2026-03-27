package com.yumaste.yumasteapi.DTO.response;

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
) {
}