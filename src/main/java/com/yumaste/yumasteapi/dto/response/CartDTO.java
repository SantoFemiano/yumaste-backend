package com.yumaste.yumasteapi.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record CartDTO(
    List<CartItemDTO> items,
    Integer totalItems,
    Integer totalQuantity,
    BigDecimal totalPrice

) implements Serializable {}
