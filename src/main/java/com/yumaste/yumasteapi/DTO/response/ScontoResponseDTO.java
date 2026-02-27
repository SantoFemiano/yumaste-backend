package com.yumaste.yumasteapi.DTO.response;

import java.time.LocalDate;

public record ScontoResponseDTO(Long id,
                                String nome,
                                Integer valore,
                                Boolean attivo,
                                LocalDate inizioSconto,
                                LocalDate fineSconto
) {
}
