package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.dto.request.*;
import com.yumaste.yumasteapi.dto.response.*;
import com.yumaste.yumasteapi.services.*;
import com.yumaste.yumasteapi.services.ai.AiBoxGenerationService;
import com.yumaste.yumasteapi.services.ai.AiDescriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private BoxService boxService;
    @Mock private IngredienteService ingredienteService;
    @Mock private BoxCompositionService boxCompositionService;
    @Mock private AllergeneService allergeneService;
    @Mock private NutritionalValueService nutritionalValueService;
    @Mock private ScontoService scontoService;
    @Mock private FornitoreService fornitoreService;
    @Mock private MagazzinoService magazzinoService;
    @Mock private IngredienteMagazzinoService ingredienteMagazzinoService;
    @Mock private OrderService orderService;
    @Mock private UserService userService;
    @Mock private CartService cartService;
    @Mock private DashboardService dashboardService;
    @Mock private AiDescriptionService aiDescriptionService;
    @Mock private AiBoxGenerationService aiBoxGenerationService;

    @InjectMocks
    private AdminController adminController;

    // --- Box ---

    @Test
    @DisplayName("addBox - restituisce 201 con la box creata")
    void addBox_returns201() {
        BoxRequestDTO req = mock(BoxRequestDTO.class);
        BoxResponseDTO dto = mock(BoxResponseDTO.class);
        when(boxService.insertBox(req)).thenReturn(dto);

        ResponseEntity<BoxResponseDTO> resp = adminController.addBox(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    @Test
    @DisplayName("updateBox - restituisce 200 con la box aggiornata")
    void updateBox_returns200() {
        BoxRequestDTO req = mock(BoxRequestDTO.class);
        BoxResponseDTO dto = mock(BoxResponseDTO.class);
        when(boxService.updateBox(1L, req)).thenReturn(dto);

        ResponseEntity<BoxResponseDTO> resp = adminController.updateBox(1L, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    @Test
    @DisplayName("deleteBox - restituisce 204")
    void deleteBox_returns204() {
        doNothing().when(boxService).deleteBox(1L);

        ResponseEntity<Void> resp = adminController.deleteBox(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(boxService).deleteBox(1L);
    }

    @Test
    @DisplayName("getInattiveBoxes - lista non vuota restituisce 200")
    void getInattiveBoxes_nonEmpty_returns200() {
        CatalogBoxDTO box = mock(CatalogBoxDTO.class);
        PagedResponseDTO<CatalogBoxDTO> paged = new PagedResponseDTO<>(List.of(box), 1, 1, 0, false);
        when(boxService.getAllInattiveBoxes(Pageable.unpaged())).thenReturn(paged);

        ResponseEntity<PagedResponseDTO<CatalogBoxDTO>> resp = adminController.getInattiveBoxes(Pageable.unpaged());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getInattiveBoxes - lista vuota restituisce 204")
    void getInattiveBoxes_empty_returns204() {
        PagedResponseDTO<CatalogBoxDTO> paged = new PagedResponseDTO<>(List.of(), 0, 0, 0, false);
        when(boxService.getAllInattiveBoxes(Pageable.unpaged())).thenReturn(paged);

        ResponseEntity<PagedResponseDTO<CatalogBoxDTO>> resp = adminController.getInattiveBoxes(Pageable.unpaged());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // --- Ingrediente ---

    @Test
    @DisplayName("addIngredient - restituisce 201")
    void addIngredient_returns201() {
        IngredienteRequestDTO req = mock(IngredienteRequestDTO.class);
        IngredienteResponseDTO dto = mock(IngredienteResponseDTO.class);
        when(ingredienteService.creaIngrediente(req)).thenReturn(dto);

        ResponseEntity<IngredienteResponseDTO> resp = adminController.addIngredient(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    @Test
    @DisplayName("getIngredienti - restituisce 200 con lista")
    void getIngredienti_returns200() {
        IngredienteResponseDTO dto = mock(IngredienteResponseDTO.class);
        when(ingredienteService.getAllIngredienti()).thenReturn(List.of(dto));

        ResponseEntity<List<IngredienteResponseDTO>> resp = adminController.getIngredienti();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(dto);
    }

    @Test
    @DisplayName("updateIngrediente - restituisce 200")
    void updateIngrediente_returns200() {
        IngredienteRequestDTO req = mock(IngredienteRequestDTO.class);
        IngredienteResponseDTO dto = mock(IngredienteResponseDTO.class);
        when(ingredienteService.updateIngrediente(1L, req)).thenReturn(dto);

        ResponseEntity<IngredienteResponseDTO> resp = adminController.updateIngrediente(1L, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteIngrediente - restituisce 204")
    void deleteIngrediente_returns204() {
        doNothing().when(ingredienteService).deleteIngrediente(1L);

        ResponseEntity<Void> resp = adminController.deleteIngrediente(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("getIngredientiInattivi - restituisce 200")
    void getIngredientiInattivi_returns200() {
        IngredienteResponseDTO dto = mock(IngredienteResponseDTO.class);
        when(ingredienteService.getAllIngredientiInattivi()).thenReturn(List.of(dto));

        ResponseEntity<List<IngredienteResponseDTO>> resp = adminController.getIngredientiInattivi();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(dto);
    }

    // --- Allergeni / Valori nutrizionali ---

    @Test
    @DisplayName("getAllAllergenes - restituisce 200 con lista allergeni")
    void getAllAllergenes_returns200() {
        AllergeneDTO dto = mock(AllergeneDTO.class);
        when(allergeneService.getAllAllergeni()).thenReturn(List.of(dto));

        ResponseEntity<List<AllergeneDTO>> resp = adminController.getAllAllergenes();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(dto);
    }

    @Test
    @DisplayName("getNutritionalValues - restituisce 200")
    void getNutritionalValues_returns200() {
        NutritionalValueDTO dto = mock(NutritionalValueDTO.class);
        when(nutritionalValueService.getAllNutritionalValue()).thenReturn(List.of(dto));

        ResponseEntity<List<NutritionalValueDTO>> resp = adminController.getNutritionalValues();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // --- Sconto ---

    @Test
    @DisplayName("addDiscount - restituisce 201")
    void addDiscount_returns201() {
        ScontoRequestDTO req = mock(ScontoRequestDTO.class);
        ScontoResponseDTO dto = mock(ScontoResponseDTO.class);
        when(scontoService.addSconto(req)).thenReturn(dto);

        ResponseEntity<ScontoResponseDTO> resp = adminController.addDiscount(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("getSconto - restituisce 200")
    void getSconto_returns200() {
        when(scontoService.getSconti()).thenReturn(List.of());
        assertThat(adminController.getSconto().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getScontiattivi - restituisce 200")
    void getScontiattivi_returns200() {
        when(scontoService.getScontiValidi()).thenReturn(List.of());
        assertThat(adminController.getScontiattivi().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateSconto - restituisce 200")
    void updateSconto_returns200() {
        ScontoRequestDTO req = mock(ScontoRequestDTO.class);
        ScontoResponseDTO dto = mock(ScontoResponseDTO.class);
        when(scontoService.updateSconto(1L, req)).thenReturn(dto);
        assertThat(adminController.updateSconto(1L, req).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteSconto - restituisce 204")
    void deleteSconto_returns204() {
        doNothing().when(scontoService).deleteSconto(1L);
        assertThat(adminController.deleteSconto(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // --- Fornitore ---

    @Test
    @DisplayName("addFornitore - restituisce 201")
    void addFornitore_returns201() {
        FornitoreRequestDTO req = mock(FornitoreRequestDTO.class);
        FornitoreResponseDTO dto = mock(FornitoreResponseDTO.class);
        when(fornitoreService.addFornitore(req)).thenReturn(dto);

        assertThat(adminController.addFornitore(req).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("getFornitori - restituisce 200")
    void getFornitori_returns200() {
        when(fornitoreService.getAllFornitore()).thenReturn(List.of());
        assertThat(adminController.getFornitori().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteFornitore - restituisce 204")
    void deleteFornitore_returns204() {
        doNothing().when(fornitoreService).deleteFornitore(1L);
        assertThat(adminController.deleteFornitore(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("updateFornitore - restituisce 200")
    void updateFornitore_returns200() {
        FornitoreRequestDTO req = mock(FornitoreRequestDTO.class);
        FornitoreResponseDTO dto = mock(FornitoreResponseDTO.class);
        when(fornitoreService.updateFornitore(1L, req)).thenReturn(dto);
        assertThat(adminController.updateFornitore(1L, req).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // --- Magazzino ---

    @Test
    @DisplayName("addMagazzino - restituisce 201")
    void addMagazzino_returns201() {
        MagazzinoRequestDTO req = mock(MagazzinoRequestDTO.class);
        MagazzinoResponseDTO dto = mock(MagazzinoResponseDTO.class);
        when(magazzinoService.addMagazzino(req)).thenReturn(dto);
        assertThat(adminController.addMagazzino(req).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("getMagazzini - restituisce 200")
    void getMagazzini_returns200() {
        when(magazzinoService.getAllMagazzino()).thenReturn(List.of());
        assertThat(adminController.getMagazzini().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deleteMagazzino - restituisce 204")
    void deleteMagazzino_returns204() {
        doNothing().when(magazzinoService).deleteMagazzino(1L);
        assertThat(adminController.deleteMagazzino(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("updateMagazzino - restituisce 200")
    void updateMagazzino_returns200() {
        MagazzinoRequestDTO req = mock(MagazzinoRequestDTO.class);
        MagazzinoResponseDTO dto = mock(MagazzinoResponseDTO.class);
        when(magazzinoService.updateMagazzino(1L, req)).thenReturn(dto);
        assertThat(adminController.updateMagazzino(1L, req).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // --- Clienti / Ordini ---

    @Test
    @DisplayName("getClienti - restituisce 200 con lista clienti")
    void getClienti_returns200() {
        UtenteProfileDTO dto = mock(UtenteProfileDTO.class);
        when(userService.getClienti()).thenReturn(List.of(dto));
        assertThat(adminController.getClienti().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("removeCliente - restituisce 200")
    void removeCliente_returns200() {
        doNothing().when(userService).deleteUser(1L);
        assertThat(adminController.removeCliente(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getOrdiniClienti - restituisce 200")
    void getOrdiniClienti_returns200() {
        when(orderService.findAllOrdini()).thenReturn(List.of());
        assertThat(adminController.getOrdiniClienti().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getDettagliOrdine - restituisce 200")
    void getDettagliOrdine_returns200() {
        OrdiniDettagliDTO det = mock(OrdiniDettagliDTO.class);
        when(orderService.getDettagliOrdineAdmin(1L)).thenReturn(List.of(det));
        assertThat(adminController.getDettagliOrdine(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("updateStatoOrdine - restituisce 200")
    void updateStatoOrdine_returns200() {
        OrdineResponseDTO dto = mock(OrdineResponseDTO.class);
        when(orderService.updateStatoOrdine(1L, "SPEDITO", null)).thenReturn(dto);
        assertThat(adminController.updateStatoOrdine(1L, "SPEDITO", null).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // --- Dashboard ---

    @Test
    @DisplayName("getDashboardStats - restituisce 200 con stats")
    void getDashboardStats_returns200() {
        DashboardStatsDTO dto = mock(DashboardStatsDTO.class);
        when(dashboardService.getStats()).thenReturn(dto);

        ResponseEntity<DashboardStatsDTO> resp = adminController.getDashboardStats();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    // --- AI ---

    @Test
    @DisplayName("generateBoxAi - con request non null restituisce 200")
    void generateBoxAi_withRequest_returns200() {
        AiGenerateBoxRequestDTO req = mock(AiGenerateBoxRequestDTO.class);
        AiGenerateBoxResponseDTO dto = mock(AiGenerateBoxResponseDTO.class);
        when(aiBoxGenerationService.generaBoxAutomatica(req)).thenReturn(dto);

        assertThat(adminController.generateBoxAi(req).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("generateBoxAi - con request null crea DTO vuoto e restituisce 200")
    void generateBoxAi_nullRequest_returns200() {
        AiGenerateBoxResponseDTO dto = mock(AiGenerateBoxResponseDTO.class);
        when(aiBoxGenerationService.generaBoxAutomatica(any(AiGenerateBoxRequestDTO.class))).thenReturn(dto);

        assertThat(adminController.generateBoxAi(null).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
