package com.yumaste.yumasteapi.DTO.response;

import java.time.LocalDate;
import java.util.List;

public record ScontoBoxResponseDTO(// Dati della Box
                                   Long boxId,
                                   String nomeBox,

                                   // Dati dello Sconto
                                   Long scontoId,
                                   String nomeSconto,
                                   Integer valoreSconto,
                                   LocalDate fineSconto ){
}
