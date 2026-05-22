package com.yumaste.yumasteapi.DTO.response;

import java.io.Serializable;
import java.time.LocalDate;


public record ScontoBoxResponseDTO(
        Long boxId,
        String nomeBox,

        // Dati dello Sconto
        Long scontoId,
        String nomeSconto,
        Integer valoreSconto,
        LocalDate fineSconto )implements Serializable {
}
