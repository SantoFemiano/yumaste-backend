package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.NutritionalValueDTO;
import com.yumaste.yumasteapi.mapper.NutritionalValueMapper;
import com.yumaste.yumasteapi.models.ValoriNutrizionali;
import com.yumaste.yumasteapi.repositories.NutritionalValueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionalValueServiceTest {

    @Mock
    private NutritionalValueRepository nutritionalValueRepository;

    @Mock
    private NutritionalValueMapper nutritionalValueMapper;

    @InjectMocks
    private NutritionalValueService nutritionalValueService;

    @Test
    @DisplayName("getAllNutritionalValue - restituisce lista DTO mappata")
    void getAllNutritionalValue_returnsMappedList() {
        ValoriNutrizionali vn1 = new ValoriNutrizionali();
        ValoriNutrizionali vn2 = new ValoriNutrizionali();
        NutritionalValueDTO dto1 = mock(NutritionalValueDTO.class);
        NutritionalValueDTO dto2 = mock(NutritionalValueDTO.class);

        when(nutritionalValueRepository.findAll()).thenReturn(List.of(vn1, vn2));
        when(nutritionalValueMapper.toDto(vn1)).thenReturn(dto1);
        when(nutritionalValueMapper.toDto(vn2)).thenReturn(dto2);

        List<NutritionalValueDTO> result = nutritionalValueService.getAllNutritionalValue();

        assertThat(result).hasSize(2).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("getAllNutritionalValue - lista vuota")
    void getAllNutritionalValue_emptyList() {
        when(nutritionalValueRepository.findAll()).thenReturn(List.of());

        List<NutritionalValueDTO> result = nutritionalValueService.getAllNutritionalValue();

        assertThat(result).isEmpty();
        verify(nutritionalValueMapper, never()).toDto(any());
    }
}
