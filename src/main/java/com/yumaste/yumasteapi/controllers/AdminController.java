package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.dto.request.*;
import com.yumaste.yumasteapi.dto.response.*;
import com.yumaste.yumasteapi.services.*;
import com.yumaste.yumasteapi.services.ai.AiBoxGenerationService;
import com.yumaste.yumasteapi.services.ai.AiDescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BoxService boxService;
    private final IngredienteService ingredienteService;
    private final BoxCompositionService boxCompositionService;
    private final AllergeneService allergeneService;
    private final NutritionalValueService nutritionalValueService;
    private final ScontoService ScontoService;
    private final FornitoreService fornitoreService;
    private final MagazzinoService magazzinoService;
    private final IngredienteMagazzinoService ingredienteMagazzinoService;
    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final DashboardService dashboardService;
    private final AiDescriptionService aiDescriptionService;
    private final AiBoxGenerationService aiBoxGenerationService;

    @PostMapping("/ai/genera-box")
    public ResponseEntity<AiGenerateBoxResponseDTO> generateBoxAi(@RequestBody(required = false) AiGenerateBoxRequestDTO request) {
        if (request == null) request = new AiGenerateBoxRequestDTO(null);
        return ResponseEntity.ok(aiBoxGenerationService.generaBoxAutomatica(request));
    }



    @PostMapping("/ai/genera-inserisci-ingredienti")
    public ResponseEntity<List<IngredienteResponseDTO>> generateAndInsertIngredientiAi(
            @RequestParam(defaultValue = "1") int quantita, @RequestParam(defaultValue = "") String suggerimento) {

        // 1. Chiedi all'IA di elaborare i DTO
        List<IngredienteRequestDTO> requestsGenerati = aiDescriptionService.generaIngredientiNuovi(quantita,suggerimento);

        List<IngredienteResponseDTO> responseList = new ArrayList<>();

        // 2. Riutilizziamo la tua logica di business blindata e transazionale
        for (IngredienteRequestDTO req : requestsGenerati) {
            responseList.add(ingredienteService.creaIngrediente(req));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(responseList);
    }

    @PostMapping("/addBox")
    public ResponseEntity<BoxResponseDTO> addBox(@Valid @RequestBody BoxRequestDTO boxRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boxService.insertBox(boxRequestDTO));
    }


    @PostMapping("/addIngredient")
    public ResponseEntity<IngredienteResponseDTO> addIngredient(@Valid @RequestBody IngredienteRequestDTO ingredienteRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredienteService.creaIngrediente(ingredienteRequestDTO));
    }

    @PostMapping("/addIngredientToBox/{boxId}")
    public ResponseEntity<BoxIngredientDTO> addIngredientToBox(@PathVariable Long boxId,
                                                               @Valid @RequestBody AddIngredienteToBoxRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boxCompositionService.addBoxIngredient(boxId, request));

    }
    @GetMapping("/allergeni")
    public ResponseEntity<List<AllergeneDTO>> getAllAllergenes() {
        return ResponseEntity.ok().body(allergeneService.getAllAllergeni());

    }

    @GetMapping("/valorinutrizionali")
    public ResponseEntity<List<NutritionalValueDTO>> getNutritionalValues() {
        return ResponseEntity.ok().body(nutritionalValueService.getAllNutritionalValue());
    }

    @GetMapping("/ingredienti")
    public ResponseEntity<List<IngredienteResponseDTO>> getIngredienti() {
        return ResponseEntity.ok(ingredienteService.getAllIngredienti());
    }

    @GetMapping("/ingredienti/allergeni")
    public ResponseEntity<List<IngredienteAllergeneResponseDTO>> getListaIngredientiAllergeni() {
        return ResponseEntity.ok(ingredienteService.getAllIngredientiConAllergeni());
    }

    @PostMapping("/add/sconto")
    public ResponseEntity<ScontoResponseDTO> addDiscount(@Valid @RequestBody ScontoRequestDTO scontoRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ScontoService.addSconto(scontoRequestDTO));
    }

    @GetMapping("/sconti")
    public ResponseEntity<List<ScontoResponseDTO>> getSconto()
    {
        return ResponseEntity.ok(ScontoService.getSconti());
    }

    @GetMapping("/scontiattivi")
    public ResponseEntity<List<ScontoResponseDTO>> getScontiattivi() {
        List<ScontoResponseDTO> sconti = ScontoService.getScontiValidi();
        return ResponseEntity.ok(sconti);
    }

    @PostMapping("/add/scontobox")
    public ResponseEntity<List<ScontoBoxResponseDTO>> collegaScontoBox(@Valid @RequestBody ScontoBoxRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ScontoService.addScontoBox(request));
    }

    @PostMapping("/add/fornitore")
    public ResponseEntity<FornitoreResponseDTO> addFornitore(@Valid @RequestBody FornitoreRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(fornitoreService.addFornitore(request));
    }

    @GetMapping("/fornitori")
    public ResponseEntity<List<FornitoreResponseDTO>> getFornitori() {
        return ResponseEntity.ok().body(fornitoreService.getAllFornitore());
    }

    @PostMapping("/add/magazzino")
    public ResponseEntity<MagazzinoResponseDTO> addMagazzino(@Valid @RequestBody MagazzinoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(magazzinoService.addMagazzino(request));
    }

    @DeleteMapping("/delete/magazzino/{id}")
    public ResponseEntity<MagazzinoResponseDTO> deleteMagazzino(@PathVariable Long id) {
        magazzinoService.deleteMagazzino(id);
        return ResponseEntity.noContent().build();    }

    @PutMapping("/update/magazzino/{id}")
    public ResponseEntity<MagazzinoResponseDTO> updateMagazzino(@PathVariable Long id, @Valid @RequestBody MagazzinoRequestDTO magazzinoRequestDTO) {
        return ResponseEntity.ok(magazzinoService.updateMagazzino(id, magazzinoRequestDTO));
    }

    @DeleteMapping("/delete/fornitore/{id}")
    public ResponseEntity<FornitoreResponseDTO> deleteFornitore(@PathVariable Long id) {
       fornitoreService.deleteFornitore(id);
       return ResponseEntity.noContent().build();    }

    @PutMapping("/update/fornitore/{id}")
    public ResponseEntity<FornitoreResponseDTO> updateFornitore(@PathVariable Long id, @Valid @RequestBody FornitoreRequestDTO fornitoreRequestDTO) {
        return ResponseEntity.ok(fornitoreService.updateFornitore(id, fornitoreRequestDTO));
    }


    @GetMapping("/magazzini")
    public ResponseEntity<List<MagazzinoResponseDTO>> getMagazzini() {
        return ResponseEntity.ok().body(magazzinoService.getAllMagazzino());
    }

    @PostMapping("/add/ingrediente/magazzino")
    public ResponseEntity<IngredienteMagazzinoResponse> addIngredienteMagazzino(@Valid @RequestBody IngredienteMagazzinoRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredienteMagazzinoService.caricaMerci(request));
    }

    @GetMapping("/ingrediente/magazzino")
    public ResponseEntity<List<IngredienteMagazzinoResponse>> getIngredienteMagazzino() {
        return ResponseEntity.ok().body(ingredienteMagazzinoService.getAllIngredienteMagazzino());
    }

    @GetMapping("/ordini/clienti")
    public ResponseEntity<List<OrdineResponseDTO>> getOrdiniClienti() {
        return ResponseEntity.ok().body(orderService.findAllOrdini());
    }


    @GetMapping("/clienti")
    public ResponseEntity<List<UtenteProfileDTO>> getClienti() {
        return ResponseEntity.ok().body(userService.getClienti());
    }

    @DeleteMapping("/delete/cliente/{id}")
    public ResponseEntity<Void> removeCliente(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ordine/{id}/dettagli")
     public ResponseEntity<List<OrdiniDettagliDTO>> getDettagliOrdine(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getDettagliOrdineAdmin(id));
    }

    @GetMapping("/utente/{id}/cliente")
     public ResponseEntity<CartDTO> getCarrelloUtente(@PathVariable Long id){
        CartDTO carrello = cartService.getCarrelloUtenteById(id);
        return ResponseEntity.ok(carrello);
}

    @PutMapping("/box/{id}")
    public ResponseEntity<BoxResponseDTO> updateBox(@PathVariable Long id, @Valid @RequestBody BoxRequestDTO boxRequestDTO) {
        return ResponseEntity.ok(boxService.updateBox(id, boxRequestDTO));
    }

    @DeleteMapping("/box/{id}")
    public ResponseEntity<Void> deleteBox(@PathVariable Long id) {
        boxService.deleteBox(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ingrediente/{id}")
    public ResponseEntity<IngredienteResponseDTO> updateIngrediente(@PathVariable Long id, @Valid @RequestBody IngredienteRequestDTO request) {
        return ResponseEntity.ok(ingredienteService.updateIngrediente(id, request));
    }

    @DeleteMapping("/ingrediente/{id}")
    public ResponseEntity<Void> deleteIngrediente(@PathVariable Long id) {
        ingredienteService.deleteIngrediente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/box/{boxId}/ingrediente/{ingredienteId}")
    public ResponseEntity<Void> removeIngredienteFromBox(@PathVariable Long boxId, @PathVariable Long ingredienteId) {
        boxCompositionService.removeIngredienteFromBox(boxId, ingredienteId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sconto/{scontoId}/box/{boxId}")
    public ResponseEntity<Void> removeScontoFromBox(@PathVariable Long scontoId, @PathVariable Long boxId) {
        ScontoService.removeScontoBox(scontoId, boxId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/ordine/{id}/stato")
    public ResponseEntity<OrdineResponseDTO> updateStatoOrdine(@PathVariable Long id, @RequestParam String statoOrdine, @RequestParam(required = false) String statoSpedizione) {
        return ResponseEntity.ok(orderService.updateStatoOrdine(id, statoOrdine, statoSpedizione));
    }

    @GetMapping("/sconto/sconto-box")
    public ResponseEntity<List<ScontoBoxResponseDTO>> getAssociazioniScontoBox() {
        return ResponseEntity.ok(ScontoService.getAllScontoBox());
    }

    @PutMapping("/sconto/{id}")
    public ResponseEntity<ScontoResponseDTO> updateSconto(@PathVariable Long id, @Valid @RequestBody ScontoRequestDTO request) {
        return ResponseEntity.ok(ScontoService.updateSconto(id, request));
    }

    @DeleteMapping("/delete/sconto/{id}")
    public ResponseEntity<Void> deleteSconto(@PathVariable Long id) {
        ScontoService.deleteSconto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/boxes/inattive")
    public ResponseEntity<PagedResponseDTO<CatalogBoxDTO>> getInattiveBoxes(Pageable pageable) {

        PagedResponseDTO<CatalogBoxDTO> catalogo = boxService.getAllInattiveBoxes(pageable);


        if(catalogo.content().isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(catalogo);
    }

    @GetMapping("/ingredienti/inattivi")
    public ResponseEntity<List<IngredienteResponseDTO>> getIngredientiInattivi() {
        return ResponseEntity.ok(ingredienteService.getAllIngredientiInattivi());
    }


    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}