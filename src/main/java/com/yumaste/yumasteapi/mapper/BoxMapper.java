package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.BoxRequestDTO;
import com.yumaste.yumasteapi.DTO.response.BoxResponseDTO;
import com.yumaste.yumasteapi.DTO.response.CatalogBoxDTO;
import com.yumaste.yumasteapi.models.Box;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BoxMapper {
    CatalogBoxDTO toCatalogBoxDTO(Box box);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCreazione", ignore = true)
    @Mapping(target = "dataAggiornamento", ignore = true)
    Box toBox(BoxRequestDTO requestBoxRequestDTO);

    BoxResponseDTO toResponseDTO(Box box);
//    BoxDTO toBOX(BoxDTO boxDTO);
}
