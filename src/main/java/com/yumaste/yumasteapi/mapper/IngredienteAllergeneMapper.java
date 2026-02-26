package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.IngredienteAllergeneResponseDTO;
import com.yumaste.yumasteapi.models.IngredienteAllergene;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IngredienteAllergeneMapper {


    @Mapping(source = "ingrediente.id", target = "ingredienteId")
    @Mapping(source = "ingrediente.nome", target = "ingredienteNome")
    @Mapping(source = "allergene.id", target = "allergeneId")
    @Mapping(source = "allergene.nome", target = "allergeneNome")
    IngredienteAllergeneResponseDTO toDto(IngredienteAllergene ingredienteAllergene);

}