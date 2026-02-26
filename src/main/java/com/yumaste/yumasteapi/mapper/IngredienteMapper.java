package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.DTO.response.IngredienteResponseDTO;
import com.yumaste.yumasteapi.models.Ingrediente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IngredienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCreazione", ignore = true)
    @Mapping(target = "dataAggiornamento", ignore = true)
    @Mapping(target = "fornitore", ignore = true)
    Ingrediente toEntity(IngredienteRequestDTO request);

    @Mapping(source = "fornitore.id", target = "fornitoreId")
    @Mapping(source = "fornitore.nome", target = "nomeFornitore")
    IngredienteResponseDTO toResponseDTO(Ingrediente ingrediente);


}