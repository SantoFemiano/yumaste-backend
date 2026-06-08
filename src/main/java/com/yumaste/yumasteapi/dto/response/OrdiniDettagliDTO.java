package com.yumaste.yumasteapi.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OrdiniDettagliDTO(
        Long ordineid,
        Long boxid,
        String nomeBox,
        Integer quantita,
        BigDecimal prezzounitario,
        String metodopagamento,
        LocalDate datapagamento,
        BigDecimal importo,
        String corriere,
        String statospedizione,
        IndirizzoResponseDTO indirizzoresponsedto
)implements Serializable {
}
