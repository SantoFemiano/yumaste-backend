package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.dto.response.AllergeneDTO;
import com.yumaste.yumasteapi.models.Allergene;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AllergeneMapper {

   AllergeneDTO toDto(Allergene allergene);

}