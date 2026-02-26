package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.NutritionalValueDTO;
import com.yumaste.yumasteapi.models.ValoriNutrizionali;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NutritionalValueMapper {

    @Mapping(source = "ingrediente.nome", target = "nome_Ingrediente") // Mappa l'ID dell'entità al nome del DTO
    NutritionalValueDTO toDto(ValoriNutrizionali nutritionalValue);

}
