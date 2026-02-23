package com.yumaste.yumasteapi.DTO.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record IngredientiConValoriDTO(
        String nomeIngrediente,
        BigDecimal quantitaNellaBox,
        String unitaMisura,

        // Valori nutrizionali che verranno ricalcolati
        BigDecimal chilocalorie,
        BigDecimal proteine,
        BigDecimal carboidrati,
        BigDecimal zuccheri, // Aggiunto
        BigDecimal fibre,    // Aggiunto
        BigDecimal grassi,
        BigDecimal sale      // Aggiunto
) {
    // COSTRUTTORE COMPATTO: scatta in automatico!
    public IngredientiConValoriDTO {
        if (quantitaNellaBox != null && chilocalorie != null && unitaMisura != null) {

            String unita = unitaMisura.trim().toLowerCase();
            BigDecimal fattoreDiMoltiplicazione;

            // Logica di calcolo in base all'unità di misura
            if (unita.equals("kg") || unita.equals("l") || unita.equals("litri")) {
                // Es. 1.5 Kg -> 1500g. Dividiamo per 100 -> fattore 15
                BigDecimal quantitaInGrammi = quantitaNellaBox.multiply(new BigDecimal("1000"));
                fattoreDiMoltiplicazione = quantitaInGrammi.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            } else if (unita.equals("g") || unita.equals("ml")) {
                // Es. 250g / 100 = fattore 2.5
                fattoreDiMoltiplicazione = quantitaNellaBox.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            } else if (unita.equals("pz") || unita.equals("pezzi") || unita.equals("unita")) {
                // Calcolo per unità singola (es. 3 uova = fattore 3)
                fattoreDiMoltiplicazione = quantitaNellaBox;

            } else {
                fattoreDiMoltiplicazione = quantitaNellaBox.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            }

            // APPLICHIAMO IL FATTORE A TUTTI I CAMPI E ARROTONDIAMO A 2 DECIMALI
            chilocalorie = chilocalorie.multiply(fattoreDiMoltiplicazione).setScale(2, RoundingMode.HALF_UP);

            if (proteine != null) {
                proteine = proteine.multiply(fattoreDiMoltiplicazione).setScale(2, RoundingMode.HALF_UP);
            }
            if (carboidrati != null) {
                carboidrati = carboidrati.multiply(fattoreDiMoltiplicazione).setScale(2, RoundingMode.HALF_UP);
            }
            if (zuccheri != null) {
                zuccheri = zuccheri.multiply(fattoreDiMoltiplicazione).setScale(2, RoundingMode.HALF_UP);
            }
            if (fibre != null) {
                fibre = fibre.multiply(fattoreDiMoltiplicazione).setScale(2, RoundingMode.HALF_UP);
            }
            if (grassi != null) {
                grassi = grassi.multiply(fattoreDiMoltiplicazione).setScale(2, RoundingMode.HALF_UP);
            }
            if (sale != null) {
                sale = sale.multiply(fattoreDiMoltiplicazione).setScale(2, RoundingMode.HALF_UP);
            }
        }
    }
}