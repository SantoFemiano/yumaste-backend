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
     OrdineResponseDTO toDto(Ordine ordine);
}
