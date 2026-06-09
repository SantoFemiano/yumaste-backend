package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.AllergeneDTO;
import com.yumaste.yumasteapi.mapper.AllergeneMapper;
import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.repositories.AllergeneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllergeneServiceTest {

    @Mock private AllergeneRepository allergeneRepository;
    @Mock private AllergeneMapper allergeneMapper;
    @InjectMocks private AllergeneService allergeneService;

    private Allergene allergene;
    private AllergeneDTO allergeneDTO;

    @BeforeEach
    void setUp() {
        allergene = new Allergene();
        allergene.setId(1L);
        allergene.setNome("Glutine");
        // AllergeneDTO(Long id, String nome, String descrizione)
        allergeneDTO = new AllergeneDTO(1L, "Glutine", "Cereali contenenti glutine");
    }

    @Test
    @DisplayName("getAllAllergeni - restituisce lista DTO mappata")
    void getAllAllergeni_returnsMappedList() {
        when(allergeneRepository.findAll()).thenReturn(List.of(allergene));
        when(allergeneMapper.toDto(allergene)).thenReturn(allergeneDTO);

        List<AllergeneDTO> result = allergeneService.getAllAllergeni();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Glutine");
        verify(allergeneRepository).findAll();
    }

    @Test
    @DisplayName("getAllAllergeni - repository vuoto restituisce lista vuota")
    void getAllAllergeni_emptyList() {
        when(allergeneRepository.findAll()).thenReturn(List.of());

        List<AllergeneDTO> result = allergeneService.getAllAllergeni();

        assertThat(result).isEmpty();
    }
}
