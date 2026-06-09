package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.response.CatalogBoxDTO;
import com.yumaste.yumasteapi.dto.response.PagedResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.BoxMapper;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.BoxCompositionRepository;
import com.yumaste.yumasteapi.repositories.IngredienteAllergeneRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock private BoxRepository boxRepository;
    @Mock private BoxMapper boxMapper;
    @Mock private BoxCompositionService boxCompositionService;
    @Mock private IngredienteAllergeneRepository ingredienteAllergeneRepository;
    @Mock private ScontoRepository scontoRepository;

    @InjectMocks private BoxService boxService;

    private Box box;

    @BeforeEach
    void setUp() {
        box = new Box();
        box.setId(1L);
        box.setNome("Box Vegana");
        box.setPrezzo(new BigDecimal("20.00"));
        box.setAttivo(true);
    }

    @Test
    @DisplayName("getAllActiveBoxes - senza filtri restituisce pagina")
    void getAllActiveBoxes_noFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        when(boxRepository.findByAttivoTrue(pageable)).thenReturn(new PageImpl<>(List.of(box)));
        when(scontoRepository.findMigliorScontoAttivoPerBox(anyLong(), any())).thenReturn(Optional.empty());
        PagedResponseDTO<CatalogBoxDTO> result = boxService.getAllActiveBoxes(null, null, pageable);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("getBoxById - non trovata lancia ResourceNotFoundException")
    void getBoxById_notFound() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boxService.getBoxById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteBox - non trovata lancia ResourceNotFoundException")
    void deleteBox_notFound() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boxService.deleteBox(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteBox - box trovata viene disattivata")
    void deleteBox_success() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(boxRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        boxService.deleteBox(1L);
        assertThat(box.getAttivo()).isFalse();
        verify(boxRepository).save(box);
    }
}
