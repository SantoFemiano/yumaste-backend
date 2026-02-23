package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.BoxIngredientDTO;
import com.yumaste.yumasteapi.models.ComposizioneBox;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BoxCompositionMapper {
    @Mapping(source = "ingrediente.nome", target = "nomeIngrediente")
    @Mapping(source= "ingrediente.unitaMisura", target = "unita")
    BoxIngredientDTO toDto(ComposizioneBox composizione);
}
