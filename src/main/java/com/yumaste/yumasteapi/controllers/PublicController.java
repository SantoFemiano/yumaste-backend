package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.DTO.response.CatalogBoxDTO;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.services.BoxService;
import com.yumaste.yumasteapi.services.testService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final testService testService;
    private final BoxService boxService;

    public PublicController(testService ser, BoxService boxService) {
        this.testService= ser;
        this.boxService = boxService;
    }

    @GetMapping("/boxes")
    public ResponseEntity<Page<CatalogBoxDTO>> getCatalog(
            @RequestParam(required=false) String categoria, Pageable pageable){

        Page<CatalogBoxDTO> catolog = boxService.getAllActiveBoxes(categoria,pageable);
        return ResponseEntity.ok(catolog);
    }

    @GetMapping("/box/{id}")
    public ResponseEntity<CatalogBoxDTO> getBoxById(@PathVariable Long id){
        Page<CatalogBoxDTO> box= boxService.getBoxById(id,Pageable.unpaged());
    return ResponseEntity.ok(box.getContent().stream().findFirst().orElse(null));
    }


}
