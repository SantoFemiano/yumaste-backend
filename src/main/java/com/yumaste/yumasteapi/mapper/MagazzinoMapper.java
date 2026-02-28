package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.MagazzinoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.MagazzinoResponseDTO;
import com.yumaste.yumasteapi.models.Magazzino;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MagazzinoMapper {
    MagazzinoResponseDTO toDto(Magazzino magazzino);
    Magazzino toEntity(MagazzinoRequestDTO magazzinoRequestDTO);
}
