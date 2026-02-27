package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.ScontoBoxRequestDTO;
import com.yumaste.yumasteapi.DTO.response.ScontoBoxResponseDTO;
import com.yumaste.yumasteapi.models.ScontoBox;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScontoBoxMapper {


    @Mapping(source = "box.id", target = "boxId")
    @Mapping(source = "box.nome", target = "nomeBox")
    @Mapping(source = "sconto.id", target = "scontoId")
    @Mapping(source = "sconto.nome", target = "nomeSconto")
    @Mapping(source = "sconto.valore", target = "valoreSconto")
    @Mapping(source = "sconto.fineSconto", target = "fineSconto")
    ScontoBoxResponseDTO toDto(ScontoBox scontoBox);
}
