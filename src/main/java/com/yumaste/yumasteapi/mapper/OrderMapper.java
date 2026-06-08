package com.yumaste.yumasteapi.mapper;


import com.yumaste.yumasteapi.dto.response.OrdineResponseDTO;
import com.yumaste.yumasteapi.models.Ordine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface OrderMapper {

     @Mapping(source = "utente.id", target = "utenteId")
     @Mapping(source = "utente.nome", target = "nomeCliente")
     @Mapping(source = "utente.cognome", target = "cognomeCliente")
     OrdineResponseDTO toDto(Ordine ordine);
}
