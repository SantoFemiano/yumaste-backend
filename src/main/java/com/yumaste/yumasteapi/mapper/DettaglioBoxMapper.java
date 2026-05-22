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

    @Mapping(target = "chilocalorie", expression = "java(calcolaMacro(vn != null ? vn.getChilocalorie() : null, cb))")
    @Mapping(target = "proteine", expression = "java(calcolaMacro(vn != null ? vn.getProteine() : null, cb))")
    @Mapping(target = "carboidrati", expression = "java(calcolaMacro(vn != null ? vn.getCarboidrati() : null, cb))")
    @Mapping(target = "zuccheri", expression = "java(calcolaMacro(vn != null ? vn.getZuccheri() : null, cb))")
    @Mapping(target = "fibre", expression = "java(calcolaMacro(vn != null ? vn.getFibre() : null, cb))")
    @Mapping(target = "grassi", expression = "java(calcolaMacro(vn != null ? vn.getGrassi() : null, cb))")
    @Mapping(target = "sale", expression = "java(calcolaMacro(vn != null ? vn.getSale() : null, cb))")
    IngredientiConValoriDTO toDtoCalcolato(ComposizioneBox cb, ValoriNutrizionali vn);

    default BigDecimal calcolaMacro(Object valoreBaseObj, ComposizioneBox cb) {
        if (valoreBaseObj == null || cb == null || cb.getQuantita() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal valoreBase = new BigDecimal(valoreBaseObj.toString());
        String unita = cb.getIngrediente().getUnitaMisura();
        BigDecimal quantita = cb.getQuantita();
        BigDecimal moltiplicatore;

        if (unita != null && unita.equalsIgnoreCase("pz")) {
            // Gestione per pezzi
            BigDecimal pesoPezzo = cb.getIngrediente().getPesoPerPezzo();

            // Se il peso per pezzo non è stato inserito, restituiamo zero (o puoi lanciare un'eccezione)
            if (pesoPezzo == null || pesoPezzo.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            // 1. Calcolo il peso totale in grammi (quantità pezzi * peso in grammi di un pezzo)
            BigDecimal pesoTotaleInGrammi = quantita.multiply(pesoPezzo);

            // 2. I valori nutrizionali sono su 100g, quindi divido per 100
            moltiplicatore = pesoTotaleInGrammi.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        } else if (unita != null && (unita.equalsIgnoreCase("kg") || unita.equalsIgnoreCase("l"))) {
            // Unità in kg o litri (moltiplico per 10 perché (kg * 1000) / 100 = 10)
            moltiplicatore = quantita.multiply(BigDecimal.valueOf(10));
        } else {
            // Unità base in grammi o millilitri
            moltiplicatore = quantita.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }

        return valoreBase.multiply(moltiplicatore).setScale(2, RoundingMode.HALF_UP);
    }
}