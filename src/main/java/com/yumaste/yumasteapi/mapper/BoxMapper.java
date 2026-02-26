package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.BoxRequestDTO;
import com.yumaste.yumasteapi.DTO.response.BoxResponseDTO;
import com.yumaste.yumasteapi.models.Box;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BoxMapper {




    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCreazione", ignore = true)
    @Mapping(target = "dataAggiornamento", ignore = true)
    Box toBox(BoxRequestDTO requestBoxRequestDTO);

    @Mapping(target = "prezzoOriginale", source = "prezzo")
    @Mapping(target = "prezzoScontato", ignore = true)
    @Mapping(target = "percentualeSconto", ignore = true)
    BoxResponseDTO toResponseDTO(Box box);

}
