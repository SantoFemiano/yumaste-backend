package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.response.*;
import com.yumaste.yumasteapi.services.BoxCompositionService;
import com.yumaste.yumasteapi.services.BoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicController {


    private final BoxService boxService;
    private final BoxCompositionService boxCompositionService;

    @GetMapping("/boxes")
    public ResponseEntity<PagedResponseDTO<CatalogBoxDTO>> getCatalog(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        PagedResponseDTO<CatalogBoxDTO> catalogo = boxService.getAllActiveBoxes(categoria, search, pageable);

        if(catalogo.content().isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(catalogo);
    }

    @GetMapping("/box/{id}")
    public ResponseEntity<CatalogBoxDTO> getBoxById(@PathVariable Long id){
        CatalogBoxDTO box = boxService.getBoxById(id);
        return ResponseEntity.ok(box);
    }

    @GetMapping("/box/ingredienti/{idBox}")
    public ResponseEntity<List<BoxIngredientDTO>> getIngredientiByBoxId(@PathVariable Long idBox){
      return ResponseEntity.ok().body(boxCompositionService.getBoxIngredients(idBox));
    }

    @GetMapping("/valori/box/{boxId}")
    public ResponseEntity<List<IngredientiConValoriDTO>> getIngredientiBox(
            @PathVariable Long boxId
    ) {
        List<IngredientiConValoriDTO> ingredienti = boxCompositionService.getIngredientiConValoriDellaBox(boxId);
        return ResponseEntity.ok(ingredienti);
    }

    @GetMapping("/box/detail/{boxId}")
    public ResponseEntity<BoxDetailDTO> getBoxDetail(@PathVariable Long boxId){
        BoxDetailDTO boxDetail = boxService.getDettaglioBox(boxId);
        return ResponseEntity.ok(boxDetail);
    }


}
