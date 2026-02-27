package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.ScontoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.ScontoResponseDTO;
import com.yumaste.yumasteapi.models.Sconto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScontoMapper {
    Sconto toEntity(ScontoRequestDTO request);

    @Mapping(target = "attivo", source = "attivo")
    ScontoResponseDTO toDto(Sconto sconto);
}
