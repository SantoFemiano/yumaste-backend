package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.FornitoreRequestDTO;
import com.yumaste.yumasteapi.DTO.response.FornitoreResponseDTO;
import com.yumaste.yumasteapi.models.Fornitore;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FornitoreMapper {
    FornitoreResponseDTO toDto(Fornitore fornitore);

    Fornitore toEntity(FornitoreRequestDTO fornitoreRequestDTO);
}
