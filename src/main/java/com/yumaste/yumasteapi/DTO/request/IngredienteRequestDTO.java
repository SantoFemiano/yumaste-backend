package com.yumaste.yumasteapi.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record IngredienteRequestDTO(
        @NotBlank(message = "L'EAN è obbligatorio")
        @Size(max = 13, message = "L'EAN non può superare i 13 caratteri")
        String ean,

        @NotNull(message = "L'ID del fornitore è obbligatorio")
        String partitaIva,

        @NotBlank(message = "Il nome è obbligatorio")
        String nome,

        String descrizione,
        String unitaMisura,

        @NotNull(message = "Il prezzo è obbligatorio")
        @Positive(message = "Il prezzo deve essere positivo")
        BigDecimal prezzoPerUnita,

        Boolean attivo,

        List<Long>allergeniIds, // Lista degli ID degli allergeni spuntati nel frontend
        ValoriNutrizionaliRequestDTO valoriNutrizionali
) {
    // Costruttore compatto per i default (così ignoriamo i @ColumnDefault di Hibernate)
    public IngredienteRequestDTO {
        if (attivo == null) attivo = true;
        if (unitaMisura == null || unitaMisura.isBlank()) unitaMisura = "g"; // Default ai grammi
    }
}