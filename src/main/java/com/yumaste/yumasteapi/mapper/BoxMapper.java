package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.CatalogBoxDTO;
import com.yumaste.yumasteapi.models.Box;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoxMapper {
    CatalogBoxDTO toCatalogBoxDTO(Box box);
}
