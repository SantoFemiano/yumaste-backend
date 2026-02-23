package com.yumaste.yumasteapi.DTO.response;


import java.math.BigDecimal;

public record NutritionalValueDTO(
        Long id,
        String nome_Ingrediente,
        BigDecimal proteine,
        BigDecimal carboidrati,
        BigDecimal zuccheri,
        BigDecimal fibre,
        BigDecimal grassi,
        BigDecimal sale,
        Integer chilocalorie
) {}
