package com.yumaste.yumasteapi.mapper;


import com.yumaste.yumasteapi.dto.response.UtenteAggDTO;
import com.yumaste.yumasteapi.dto.response.UtenteProfileDTO;
import com.yumaste.yumasteapi.models.Utente;
import org.mapstruct.Mapper;



@Mapper(componentModel = "spring")
public interface UtenteMapper {
    UtenteAggDTO toDto(Utente utente);

    UtenteProfileDTO toDtoUp(Utente utente);

}
