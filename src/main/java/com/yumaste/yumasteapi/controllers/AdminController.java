package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.request.BoxRequestDTO;
import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.DTO.response.BoxResponseDTO;
import com.yumaste.yumasteapi.DTO.response.IngredienteResponseDTO;
import com.yumaste.yumasteapi.services.BoxService;
import com.yumaste.yumasteapi.services.IngredienteService;
import jakarta.validation.Valid; // <-- IMPORTA QUESTO
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BoxService boxService;
    private final IngredienteService ingredienteService;

    public AdminController(BoxService boxService, IngredienteService ingredienteService) {
        this.boxService = boxService;
        this.ingredienteService=ingredienteService;
    }

    @PostMapping("/addBox")
    public ResponseEntity<BoxResponseDTO> addBox(@Valid @RequestBody BoxRequestDTO boxRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boxService.insertBox(boxRequestDTO));
    }



    @PostMapping("/addIngredient")
    public ResponseEntity<IngredienteResponseDTO> addIngredient(@Valid @RequestBody IngredienteRequestDTO ingredienteRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredienteService.creaIngrediente(ingredienteRequestDTO));
    }

}