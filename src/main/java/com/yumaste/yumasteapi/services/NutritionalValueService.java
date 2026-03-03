package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.response.NutritionalValueDTO;
import com.yumaste.yumasteapi.mapper.NutritionalValueMapper;
import com.yumaste.yumasteapi.repositories.NutritionalValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NutritionalValueService {

    private final NutritionalValueRepository nutritionalValueRepository;
    private final NutritionalValueMapper nutritionalValueMapper;


    public List<NutritionalValueDTO> getAllNutritionalValue() {
        return nutritionalValueRepository.findAll().stream()
                .map(nutritionalValueMapper::toDto)
                .toList();
    }




}
