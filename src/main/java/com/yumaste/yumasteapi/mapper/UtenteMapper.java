package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.OrdineResponseDTO;
import com.yumaste.yumasteapi.DTO.response.UtenteAggDTO;
import com.yumaste.yumasteapi.DTO.response.UtenteProfileDTO;
import com.yumaste.yumasteapi.models.Ordine;
import com.yumaste.yumasteapi.models.Utente;
import org.mapstruct.Mapper;



@Mapper(componentModel = "spring")
public interface UtenteMapper {
    UtenteAggDTO toDto(Utente utente);

    UtenteProfileDTO toDTO(Utente utente);

}
