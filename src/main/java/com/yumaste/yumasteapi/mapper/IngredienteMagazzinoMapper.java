package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.IngredienteMagazzinoResponse;
import com.yumaste.yumasteapi.models.IngredienteMagazzino;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IngredienteMagazzinoMapper {
    @Mapping(source = "id", target="ingredienteMagazzinoId")
    @Mapping(source ="magazzino.id", target="magazzinoId")
    @Mapping(source ="ingrediente.id", target="ingredienteId")
    @Mapping(source = "magazzino.nome", target = "nomeMagazzino")
    @Mapping(source = "ingrediente.nome", target = "nomeIngrediente")
    @Mapping(source = "ingrediente.unitaMisura", target ="unitaMisura")
    IngredienteMagazzinoResponse ToDto(IngredienteMagazzino ingredienteMagazzino);
}
