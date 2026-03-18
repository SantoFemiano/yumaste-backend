package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.MagazzinoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.IndirizzoResponseDTO;
import com.yumaste.yumasteapi.DTO.response.MagazzinoResponseDTO;
import com.yumaste.yumasteapi.DTO.response.OrdineResponseDTO;

import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.models.Ordine;
import jakarta.persistence.Column;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface OrderMapper {

     @Mapping(source = "utente.id", target = "utenteId")
     @Mapping(source = "utente.nome", target = "nomeCliente")
     @Mapping(source = "utente.cognome", target = "cognomeCliente")
     OrdineResponseDTO toDto(Ordine ordine);
}
