package com.yumaste.yumasteapi.services;

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
    @InjectMocks AllergeneService allergeneService;

    @Test
    @DisplayName("getAllAllergeni - restituisce lista dal repository")
    void getAllAllergeni_returnsList() {
        Allergene a1 = new Allergene();
        a1.setId(1L);
        a1.setNome("Glutine");
        when(allergeneRepository.findAll()).thenReturn(List.of(a1));

        List<Allergene> result = allergeneService.getAllAllergeni();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Glutine");
    }

    @Test
    @DisplayName("getAllAllergeni - restituisce lista vuota se nessun allergene")
    void getAllAllergeni_emptyList() {
        when(allergeneRepository.findAll()).thenReturn(List.of());
        List<Allergene> result = allergeneService.getAllAllergeni();
        assertThat(result).isEmpty();
    }
}
