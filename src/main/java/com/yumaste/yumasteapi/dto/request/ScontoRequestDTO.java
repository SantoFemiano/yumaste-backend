package com.yumaste.yumasteapi.dto.request;
import java.time.LocalDate;

public record ScontoRequestDTO(
        String nome,
        Integer valore,
        Boolean attivo,
        LocalDate inizioSconto,
        LocalDate fineSconto
) {}
