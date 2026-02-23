package com.yumaste.yumasteapi.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BoxRequestDTO(
        @NotBlank(message = "L'EAN è obbligatorio")
        String ean,

        @NotBlank(message = "Il nome è obbligatorio")
        String nome,

        String categoria, // Questo può rimanere opzionale, quindi può essere null

        @NotNull(message = "Il prezzo è obbligatorio")
        @Positive(message = "Il prezzo deve essere maggiore di zero")
        Double prezzo,

        @NotNull(message = "Il numero di porzioni è obbligatorio")
        @Positive(message = "Le porzioni devono essere maggiori di zero")
        Byte porzioni,

        @NotNull(message = "La quantità in box è obbligatoria")
        @Positive(message = "La quantità deve essere maggiore di zero")
        Integer quantitaInBox,

        String immagineUrl,

        Boolean attivo
) {}