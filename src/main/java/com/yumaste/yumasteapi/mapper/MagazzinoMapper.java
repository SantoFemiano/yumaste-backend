package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.dto.request.MagazzinoRequestDTO;
import com.yumaste.yumasteapi.dto.response.MagazzinoResponseDTO;
import com.yumaste.yumasteapi.models.Magazzino;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MagazzinoMapper {
    MagazzinoResponseDTO toDto(Magazzino magazzino);
    Magazzino toEntity(MagazzinoRequestDTO magazzinoRequestDTO);
}
