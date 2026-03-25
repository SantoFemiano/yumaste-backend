package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.models.ComposizioneBox;
import com.yumaste.yumasteapi.models.ValoriNutrizionali;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface DettaglioBoxMapper {

    // Dati da ComposizioneBox
    @Mapping(source = "cb.ingrediente.nome", target = "nomeIngrediente")
    @Mapping(source = "cb.ingrediente.unitaMisura", target = "unitaMisura")
    @Mapping(source = "cb.quantita", target = "quantitaNellaBox")

    // Invece di fare il copia-incolla, chiamiamo il metodo "calcolaMacro" per fare la proporzione
    @Mapping(target = "chilocalorie", expression = "java(calcolaMacro(vn != null ? vn.getChilocalorie() : null, cb))")
    @Mapping(target = "proteine", expression = "java(calcolaMacro(vn != null ? vn.getProteine() : null, cb))")
    @Mapping(target = "carboidrati", expression = "java(calcolaMacro(vn != null ? vn.getCarboidrati() : null, cb))")
    @Mapping(target = "zuccheri", expression = "java(calcolaMacro(vn != null ? vn.getZuccheri() : null, cb))")
    @Mapping(target = "fibre", expression = "java(calcolaMacro(vn != null ? vn.getFibre() : null, cb))")
    @Mapping(target = "grassi", expression = "java(calcolaMacro(vn != null ? vn.getGrassi() : null, cb))")
    @Mapping(target = "sale", expression = "java(calcolaMacro(vn != null ? vn.getSale() : null, cb))")
    IngredientiConValoriDTO toDtoCalcolato(ComposizioneBox cb, ValoriNutrizionali vn);


    // =========================================================================
    // METODO MATEMATICO PER IL CALCOLO DELLE PROPORZIONI
    // =========================================================================
    default BigDecimal calcolaMacro(Object valoreBaseObj, ComposizioneBox cb) {
        // Se manca il valore base o la quantità, ritorniamo 0
        if (valoreBaseObj == null || cb == null || cb.getQuantita() == null) {
            return BigDecimal.ZERO;
        }

        // Convertiamo in modo sicuro il valore dal database (che sia Integer o BigDecimal)
        BigDecimal valoreBase = new BigDecimal(valoreBaseObj.toString());

        String unita = cb.getIngrediente().getUnitaMisura();
        BigDecimal quantita = cb.getQuantita();
        BigDecimal moltiplicatore;

        // Se l'unità è KG o Litri (1 kg contiene 10 porzioni da 100g)
        if (unita != null && (unita.equalsIgnoreCase("kg") || unita.equalsIgnoreCase("l"))) {
            // Es: se inserisci 0.5 kg (mezzo chilo), il moltiplicatore sarà 5 (500g).
            moltiplicatore = quantita.multiply(BigDecimal.valueOf(10));
        }
        // Se l'unità è GR o ML (divido i grammi per 100)
        else {
            // Es: se inserisci 250 g, il moltiplicatore sarà 2.5
            moltiplicatore = quantita.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }

        // Moltiplichiamo il valore base (per 100g) per il moltiplicatore calcolato
        return valoreBase.multiply(moltiplicatore).setScale(2, RoundingMode.HALF_UP);
    }
}