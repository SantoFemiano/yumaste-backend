package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.AllergeneDTO;
import com.yumaste.yumasteapi.mapper.AllergeneMapper;
import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.repositories.AllergeneRepository;
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
class AllergeneServiceTest {

    @Mock AllergeneRepository allergeneRepository;
    @Mock AllergeneMapper allergeneMapper;
    @InjectMocks AllergeneService allergeneService;

    @Test
    @DisplayName("getAllAllergeni - restituisce lista DTO mappata")
    void getAllAllergeni_returnsMappedDTOs() {
        Allergene a1 = new Allergene();
        a1.setId(1L);
        a1.setNome("Glutine");
        AllergeneDTO dto = mock(AllergeneDTO.class);

        when(allergeneRepository.findAll()).thenReturn(List.of(a1));
        when(allergeneMapper.toDto(a1)).thenReturn(dto);

        List<AllergeneDTO> result = allergeneService.getAllAllergeni();
        assertThat(result).containsExactly(dto);
    }

    @Test
    @DisplayName("getAllAllergeni - lista vuota se nessun allergene")
    void getAllAllergeni_emptyList() {
        when(allergeneRepository.findAll()).thenReturn(List.of());
        List<AllergeneDTO> result = allergeneService.getAllAllergeni();
        assertThat(result).isEmpty();
    }
}
