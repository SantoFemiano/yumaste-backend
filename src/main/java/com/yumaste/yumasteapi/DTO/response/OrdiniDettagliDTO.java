package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrdiniDettagliDTO(
        Long ordineid,
        Long boxid,
        Integer quantita,
        BigDecimal prezzounitario,
        String metodopagamento,
        LocalDate datapagamento,
        BigDecimal importo,
        String corriere,
        String statospedizione,
        IndirizzoResponseDTO indirizzoresponsedto
) {
}
