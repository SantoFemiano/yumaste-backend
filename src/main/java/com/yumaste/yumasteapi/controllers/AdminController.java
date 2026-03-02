package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.request.*;
import com.yumaste.yumasteapi.DTO.response.*;
import com.yumaste.yumasteapi.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/ingredienti/allergeni")
    public ResponseEntity<List<IngredienteAllergeneResponseDTO>> getListaIngredientiAllergeni() {
        return ResponseEntity.ok(ingredienteService.getAllIngredientiConAllergeni());
    }

    @PostMapping("/add/sconto")
    public ResponseEntity<ScontoResponseDTO> addDiscount(@Valid @RequestBody ScontoRequestDTO scontoRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ScontoService.addSconto(scontoRequestDTO));
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

}