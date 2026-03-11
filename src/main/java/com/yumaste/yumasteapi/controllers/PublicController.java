package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.response.BoxDetailDTO;
import com.yumaste.yumasteapi.DTO.response.BoxIngredientDTO;
import com.yumaste.yumasteapi.DTO.response.CatalogBoxDTO;
import com.yumaste.yumasteapi.DTO.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.services.BoxCompositionService;
import com.yumaste.yumasteapi.services.BoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<CatalogBoxDTO>> getCatalog(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String search, // 👈 Aggiungiamo il parametro 'search'
            Pageable pageable) {

        // Passiamo 'search' al service
        Page<CatalogBoxDTO> catalogo = boxService.getAllActiveBoxes(categoria, search, pageable);

        if(catalogo.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(catalogo);
    }

    @GetMapping("/box/{id}")
    public ResponseEntity<CatalogBoxDTO> getBoxById(@PathVariable Long id){
        Page<CatalogBoxDTO> box= boxService.getBoxById(id,Pageable.unpaged());
    return ResponseEntity.ok(box.getContent().stream().findFirst().orElse(null));
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
