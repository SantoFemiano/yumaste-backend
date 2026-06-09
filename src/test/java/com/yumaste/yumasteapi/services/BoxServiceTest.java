package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.BoxRequestDTO;
import com.yumaste.yumasteapi.dto.response.*;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.BoxMapper;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Sconto;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.IngredienteAllergeneRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock BoxRepository boxRepository;
    @Mock BoxMapper boxMapper;
    @Mock BoxCompositionService boxCompositionService;
    @Mock IngredienteAllergeneRepository ingredienteAllergeneRepository;
    @Mock ScontoRepository scontoRepository;

    @InjectMocks BoxService boxService;

    private Box box;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        box = new Box();
        box.setId(1L);
        box.setNome("Box Vegana");
        box.setCategoria("Vegano");
        box.setPrezzo(BigDecimal.valueOf(20.00));
        box.setAttivo(true);
        pageable = PageRequest.of(0, 10);
    }

    private void stubNoSconto() {
        when(scontoRepository.findMigliorScontoAttivoPerBox(anyLong(), any())).thenReturn(Optional.empty());
    }

    // --- getAllActiveBoxes ---

    @Test
    @DisplayName("getAllActiveBoxes - senza filtri restituisce tutte le box attive")
    void getAllActiveBoxes_noFilters_returnsAll() {
        Page<Box> page = new PageImpl<>(List.of(box));
        when(boxRepository.findByAttivoTrue(pageable)).thenReturn(page);
        stubNoSconto();

        PagedResponseDTO<CatalogBoxDTO> result = boxService.getAllActiveBoxes(null, null, pageable);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("getAllActiveBoxes - filtro solo categoria")
    void getAllActiveBoxes_onlyCategoria() {
        Page<Box> page = new PageImpl<>(List.of(box));
        when(boxRepository.findByCategoriaAndAttivoTrue("Vegano", pageable)).thenReturn(page);
        stubNoSconto();

        PagedResponseDTO<CatalogBoxDTO> result = boxService.getAllActiveBoxes("Vegano", null, pageable);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("getAllActiveBoxes - filtro solo ricerca")
    void getAllActiveBoxes_onlySearch() {
        Page<Box> page = new PageImpl<>(List.of(box));
        when(boxRepository.findByNomeContainingIgnoreCaseAndAttivoTrue("veg", pageable)).thenReturn(page);
        stubNoSconto();

        PagedResponseDTO<CatalogBoxDTO> result = boxService.getAllActiveBoxes(null, "veg", pageable);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("getAllActiveBoxes - filtro categoria + ricerca")
    void getAllActiveBoxes_categoriaAndSearch() {
        Page<Box> page = new PageImpl<>(List.of(box));
        when(boxRepository.findByCategoriaAndNomeContainingIgnoreCaseAndAttivoTrue("Vegano", "veg", pageable)).thenReturn(page);
        stubNoSconto();

        PagedResponseDTO<CatalogBoxDTO> result = boxService.getAllActiveBoxes("Vegano", "veg", pageable);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("getAllActiveBoxes - categoria 'Tutte' ignorata")
    void getAllActiveBoxes_categoriasTutte_treatedAsNoFilter() {
        Page<Box> page = new PageImpl<>(List.of(box));
        when(boxRepository.findByAttivoTrue(pageable)).thenReturn(page);
        stubNoSconto();

        PagedResponseDTO<CatalogBoxDTO> result = boxService.getAllActiveBoxes("Tutte", null, pageable);
        assertThat(result.content()).hasSize(1);
    }

    // --- getBoxById ---

    @Test
    @DisplayName("getBoxById - restituisce DTO se box trovata senza sconto")
    void getBoxById_found_noDiscount() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        stubNoSconto();

        CatalogBoxDTO result = boxService.getBoxById(1L);
        assertThat(result).isNotNull();
        assertThat(result.percentualeSconto()).isEqualTo(0);
        assertThat(result.prezzoScontato()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
    }

    @Test
    @DisplayName("getBoxById - applica sconto se sconto attivo presente")
    void getBoxById_appliesDiscount() {
        Sconto sconto = new Sconto();
        sconto.setValore(20);
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(scontoRepository.findMigliorScontoAttivoPerBox(1L, "Vegano")).thenReturn(Optional.of(sconto));

        CatalogBoxDTO result = boxService.getBoxById(1L);
        assertThat(result.prezzoScontato()).isEqualByComparingTo(new BigDecimal("16.00"));
        assertThat(result.percentualeSconto()).isEqualTo(20);
    }

    @Test
    @DisplayName("getBoxById - lancia ResourceNotFoundException se non trovata")
    void getBoxById_notFound_throws() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boxService.getBoxById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- insertBox ---

    @Test
    @DisplayName("insertBox - salva e restituisce DTO")
    void insertBox_savesAndReturns() {
        BoxRequestDTO req = mock(BoxRequestDTO.class);
        BoxResponseDTO dto = mock(BoxResponseDTO.class);
        when(boxMapper.toBox(req)).thenReturn(box);
        when(boxRepository.save(box)).thenReturn(box);
        when(boxMapper.toResponseDTO(box)).thenReturn(dto);

        BoxResponseDTO result = boxService.insertBox(req);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("insertBox - se attivo è null, lo imposta a true")
    void insertBox_nullAttivo_setsTrue() {
        box.setAttivo(null);
        BoxRequestDTO req = mock(BoxRequestDTO.class);
        when(boxMapper.toBox(req)).thenReturn(box);
        when(boxRepository.save(box)).thenReturn(box);
        when(boxMapper.toResponseDTO(box)).thenReturn(mock(BoxResponseDTO.class));

        boxService.insertBox(req);
        assertThat(box.getAttivo()).isTrue();
    }

    // --- deleteBox ---

    @Test
    @DisplayName("deleteBox - soft delete imposta attivo=false")
    void deleteBox_softDelete() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        boxService.deleteBox(1L);
        assertThat(box.getAttivo()).isFalse();
        verify(boxRepository).save(box);
    }

    @Test
    @DisplayName("deleteBox - lancia ResourceNotFoundException se non trovata")
    void deleteBox_notFound_throws() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boxService.deleteBox(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- updateBox ---

    @Test
    @DisplayName("updateBox - aggiorna campi e restituisce DTO")
    void updateBox_updatesFields() {
        BoxRequestDTO req = mock(BoxRequestDTO.class);
        when(req.ean()).thenReturn("123");
        when(req.nome()).thenReturn("NuovoNome");
        when(req.categoria()).thenReturn("Carne");
        when(req.prezzo()).thenReturn(25.0);
        when(req.porzioni()).thenReturn((byte) 2);
        when(req.quantitaInBox()).thenReturn(3);
        when(req.immagineUrl()).thenReturn("img.jpg");
        when(req.attivo()).thenReturn(false);
        BoxResponseDTO dto = mock(BoxResponseDTO.class);

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(boxRepository.save(box)).thenReturn(box);
        when(boxMapper.toResponseDTO(box)).thenReturn(dto);

        BoxResponseDTO result = boxService.updateBox(1L, req);
        assertThat(result).isEqualTo(dto);
        assertThat(box.getNome()).isEqualTo("NuovoNome");
    }

    @Test
    @DisplayName("updateBox - lancia ResourceNotFoundException se non trovata")
    void updateBox_notFound_throws() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boxService.updateBox(99L, mock(BoxRequestDTO.class)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getAllInattiveBoxes ---

    @Test
    @DisplayName("getAllInattiveBoxes - restituisce box inattive")
    void getAllInattiveBoxes_returnsInactive() {
        box.setAttivo(false);
        Page<Box> page = new PageImpl<>(List.of(box));
        when(boxRepository.findByAttivoFalse(pageable)).thenReturn(page);
        stubNoSconto();

        PagedResponseDTO<CatalogBoxDTO> result = boxService.getAllInattiveBoxes(pageable);
        assertThat(result.content()).hasSize(1);
    }
}
