package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.request.*;
import com.yumaste.yumasteapi.DTO.response.*;
import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.models.ValoriNutrizionali;
import com.yumaste.yumasteapi.services.*;
import jakarta.validation.Valid; // <-- IMPORTA QUESTO
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

    @GetMapping("/ingredienti-allergeni")
    public ResponseEntity<List<IngredienteAllergeneResponseDTO>> getListaIngredientiAllergeni() {
        return ResponseEntity.ok(ingredienteService.getAllIngredientiConAllergeni());
    }

    @PostMapping("/adddiscount")
    public ResponseEntity<ScontoResponseDTO> addDiscount(@Valid @RequestBody ScontoRequestDTO scontoRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ScontoService.addSconto(scontoRequestDTO));
    }

    @PostMapping("/collega-sconto-box")
    public ResponseEntity<List<ScontoBoxResponseDTO>> collegaScontoBox(@Valid @RequestBody ScontoBoxRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ScontoService.addScontoBox(request));
    }

}