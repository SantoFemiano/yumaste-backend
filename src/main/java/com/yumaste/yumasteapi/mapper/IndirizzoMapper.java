package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.dto.request.IndirizzoRequestDTO;
import com.yumaste.yumasteapi.dto.response.IndirizzoResponseDTO;
import com.yumaste.yumasteapi.models.IndirizzoUtente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IndirizzoMapper {

    IndirizzoResponseDTO toDTO (IndirizzoUtente indirizzo);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utente", ignore = true)
    @Mapping(target = "stato", ignore = true)
    IndirizzoUtente toEntity (IndirizzoRequestDTO dto);
}
