package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;

public record CartItemDTO(
         Long idRigaCarrello,
         Long boxId,
         String nomeBox,
         Integer quantita,
         String immagineUrl,
         BigDecimal prezzoUnitario
) {}