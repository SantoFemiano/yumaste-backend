package com.yumaste.yumasteapi.DTO.request;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ScontoBoxRequestDTO(

        @NotNull(message = "L'ID dello sconto è obbligatorio")
        Long scontoId,

        @NotEmpty(message = "Devi selezionare almeno una Box a cui applicare lo sconto")
        List<Long> boxIds

) {}