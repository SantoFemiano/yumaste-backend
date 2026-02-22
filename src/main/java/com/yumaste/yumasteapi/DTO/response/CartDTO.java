package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;
import java.util.List;

public record CartDTO(
    List<CartItemDTO> items,
    Integer totalItems,
    Integer totalQuantity,
    BigDecimal totalPrice

) {}
