package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.models.ComposizioneBox;
import com.yumaste.yumasteapi.models.ValoriNutrizionali;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DettaglioBoxMapper {

    // Dati da ComposizioneBox (aggiunto il 'cb.' alla prima riga!)
    @Mapping(source = "cb.ingrediente.nome", target = "nomeIngrediente")
    @Mapping(source = "cb.ingrediente.unitaMisura", target = "unitaMisura")
    @Mapping(source = "cb.quantita", target = "quantitaNellaBox")

    // Dati da ValoriNutrizionali
    @Mapping(source = "vn.chilocalorie", target = "chilocalorie")
    @Mapping(source = "vn.proteine", target = "proteine")
    @Mapping(source = "vn.carboidrati", target = "carboidrati")
    @Mapping(source = "vn.zuccheri", target = "zuccheri")
    @Mapping(source = "vn.fibre", target = "fibre")
    @Mapping(source = "vn.grassi", target = "grassi")
    @Mapping(source = "vn.sale", target = "sale")

    IngredientiConValoriDTO toDtoCalcolato(ComposizioneBox cb, ValoriNutrizionali vn);

}