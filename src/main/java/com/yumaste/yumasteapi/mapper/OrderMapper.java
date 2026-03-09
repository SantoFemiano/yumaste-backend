package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.request.MagazzinoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.MagazzinoResponseDTO;
import com.yumaste.yumasteapi.DTO.response.OrdineResponseDTO;
import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.models.Ordine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") // Fondamentale per l'iniezione @Autowired
public interface OrderMapper {
     OrdineResponseDTO toDto(Ordine ordine);
}
