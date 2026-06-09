package com.yumaste.yumasteapi.services;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionalValueServiceTest {

    @Mock
    private NutritionalValueRepository nutritionalValueRepository;

    @InjectMocks
    private NutritionalValueService nutritionalValueService;

    @Test
    @DisplayName("getAllNutritionalValues - restituisce lista completa")
    void getAllNutritionalValues_returnsList() {
        ValoriNutrizionali vn1 = new ValoriNutrizionali();
        ValoriNutrizionali vn2 = new ValoriNutrizionali();
        when(nutritionalValueRepository.findAll()).thenReturn(List.of(vn1, vn2));

        List<ValoriNutrizionali> result = nutritionalValueService.getAllNutritionalValues();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAllNutritionalValues - lista vuota")
    void getAllNutritionalValues_emptyList() {
        when(nutritionalValueRepository.findAll()).thenReturn(List.of());

        List<ValoriNutrizionali> result = nutritionalValueService.getAllNutritionalValues();

        assertThat(result).isEmpty();
    }
}
