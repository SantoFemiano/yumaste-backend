package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.BoxRequestDTO;
import com.yumaste.yumasteapi.DTO.response.*;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.BoxMapper;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Sconto;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.IngredienteAllergeneRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.cfg.MapperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoxService {
    private final BoxRepository boxRepository;
    private final BoxMapper boxMapper;
    private final BoxCompositionService boxCompositionService;
    private final IngredienteAllergeneRepository ingredienteAllergeneRepository;
    private final ScontoRepository scontoRepository;
    private final MapperBuilder mapperBuilder;

    @Cacheable(value = "catalogo_box", key = "{#categoria, #search, #pageable.pageNumber, #pageable.pageSize,#pageable.sort}")
    public PagedResponseDTO<CatalogBoxDTO> getAllActiveBoxes(String categoria, String search, Pageable pageable) {
        Page<Box> boxes;

        // Puliamo i parametri per evitare stringhe vuote o spazi
        boolean haCategoria = categoria != null && !categoria.trim().isEmpty() && !categoria.equalsIgnoreCase("Tutte");
        boolean haRicerca = search != null && !search.trim().isEmpty();

        // Logica di instradamento
        if (haCategoria && haRicerca) {
            // Filtra per ENTRAMBI
            boxes = boxRepository.findByCategoriaAndNomeContainingIgnoreCaseAndAttivoTrue(categoria, search, pageable);
        } else if (haCategoria) {
            // Filtra SOLO per Categoria
            boxes = boxRepository.findByCategoriaAndAttivoTrue(categoria, pageable);
        } else if (haRicerca) {
            // Filtra SOLO per Ricerca
            boxes = boxRepository.findByNomeContainingIgnoreCaseAndAttivoTrue(search, pageable);
        } else {
            // NESSUN filtro: prendi tutto
            boxes = boxRepository.findByAttivoTrue(pageable);
        }


        Page<CatalogBoxDTO> pageResult = boxes.map(this::mapToCatalogBoxDTOConSconto);
        return new PagedResponseDTO<>(pageResult);
    }

    @Cacheable(value = "box", key="#id")
    public CatalogBoxDTO getBoxById(Long id) {
        Box box = boxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Box non trovata con ID: " + id));

        return mapToCatalogBoxDTOConSconto(box);
    }

    @CacheEvict(value = {"catalogo_box", "box_inattive"}, allEntries = true)
    public BoxResponseDTO insertBox(BoxRequestDTO boxRequestDTO){
        Box nuovaBox = boxMapper.toBox(boxRequestDTO);
        if(nuovaBox.getAttivo()==null){
            nuovaBox.setAttivo(true);
        }
        Box boxsalvata = boxRepository.save(nuovaBox);
        return boxMapper.toResponseDTO(boxsalvata);
    }

    @Cacheable(value = "box_dettagli", key = "#boxId")
    public BoxDetailDTO getDettaglioBox(Long boxId) {

        //Prendo la Box base dal Database
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Box non trovata con ID: " + boxId));

        //Prendo gli ingredienti, con loro anche i valori nutrizionali
        List<IngredientiConValoriDTO> ingredientiBox = boxCompositionService.getIngredientiConValoriDellaBox(boxId);

        BigDecimal totProteine = BigDecimal.ZERO;
        BigDecimal totCarboidrati = BigDecimal.ZERO;
        BigDecimal totGrassi = BigDecimal.ZERO;
        BigDecimal totZuccheri = BigDecimal.ZERO;
        BigDecimal totFibre = BigDecimal.ZERO;
        BigDecimal totSale = BigDecimal.ZERO;
        BigDecimal totKcal = BigDecimal.ZERO;

        //Ciclo gli ingredienti e faccio le addizioni in totale sicurezza
        for (IngredientiConValoriDTO ingrediente : ingredientiBox) {
            if (ingrediente.chilocalorie() != null) totKcal = totKcal.add(ingrediente.chilocalorie());
            if (ingrediente.proteine() != null) totProteine = totProteine.add(ingrediente.proteine());
            if (ingrediente.carboidrati() != null) totCarboidrati = totCarboidrati.add(ingrediente.carboidrati());
            if (ingrediente.grassi() != null) totGrassi = totGrassi.add(ingrediente.grassi());
            if (ingrediente.zuccheri() != null) totZuccheri = totZuccheri.add(ingrediente.zuccheri());
            if (ingrediente.fibre() != null) totFibre = totFibre.add(ingrediente.fibre());
            if (ingrediente.sale() != null) totSale = totSale.add(ingrediente.sale());
        }

        NutritionalValueDetailDTO macroTotali = new NutritionalValueDetailDTO(
                totProteine,
                totCarboidrati,
                totGrassi,
                totZuccheri, // FIX: Ora l'ordine è corretto!
                totFibre,    // FIX: Ora l'ordine è corretto!
                totSale,
                totKcal.intValue()
        );

        //ricaviamo la lista degli allergeni.
        List<String> allergeniDellaBox = ingredienteAllergeneRepository.findNomiAllergeniByBoxId(boxId);

        Dati_Sconto datiScontobox = calcolaSconto(box);

        return new BoxDetailDTO(
                box.getId(),
                box.getNome(),
                box.getCategoria(),
                box.getPorzioni(),
                datiScontobox.originale(),
                datiScontobox.scontato(),
                datiScontobox.percentuale(),
                box.getImmagineUrl(),
                macroTotali,
                allergeniDellaBox,
                ingredientiBox
        );
    }



    private Dati_Sconto calcolaSconto(Box box) {
        BigDecimal prezzoOriginale = box.getPrezzo();
        BigDecimal prezzoScontato = prezzoOriginale;
        Integer percentuale_sconto = 0;


      Optional<Sconto> scontoOpt = scontoRepository.findMigliorScontoAttivoPerBox(box.getId(), box.getCategoria());


        if (scontoOpt.isPresent()) {
            Sconto sconto = scontoOpt.get();
            percentuale_sconto = sconto.getValore();
            BigDecimal moltiplicatore = BigDecimal.valueOf(100 - percentuale_sconto).divide(BigDecimal.valueOf(100));
            prezzoScontato = prezzoOriginale.multiply(moltiplicatore).setScale(2, RoundingMode.HALF_UP);

        }

        return  new Dati_Sconto(box.getPrezzo(),prezzoScontato,percentuale_sconto);
    }

    @Cacheable(value = "box_inattive", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public PagedResponseDTO<CatalogBoxDTO> getAllInattiveBoxes(Pageable pageable) {
        Page<Box> boxes = boxRepository.findByAttivoFalse(pageable);
        return new PagedResponseDTO<>(boxes.map(this::mapToCatalogBoxDTOConSconto));
    }

    private record Dati_Sconto(BigDecimal originale, BigDecimal scontato, Integer percentuale) {}


    private CatalogBoxDTO mapToCatalogBoxDTOConSconto(Box box) {


        Dati_Sconto sconto = calcolaSconto(box);

        //DTO per catalogo con sconti
        return new CatalogBoxDTO(
                box.getId(),
                box.getEan(),
                box.getNome(),
                box.getCategoria(),
                sconto.originale(),   // Prezzo base (es. 20.00)
                sconto.scontato(),    // Prezzo scontrato (es. 16.00)
                sconto.percentuale(), // Percentuale (es. 20)
                box.getPorzioni(),
                box.getImmagineUrl(),
                box.getAttivo()
        );
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "box", key = "#id"),
            @CacheEvict(value = "box_dettagli", key = "#id"),
            @CacheEvict(value = "catalogo_box", allEntries = true),
            @CacheEvict(value = "box_inattive", allEntries = true)
    })
    public BoxResponseDTO updateBox(Long id, BoxRequestDTO request) {
        Box box = boxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Box non trovata con ID: " + id));

        box.setEan(request.ean());
        box.setNome(request.nome());
        box.setCategoria(request.categoria());
        box.setPrezzo(BigDecimal.valueOf(request.prezzo()));
        box.setPorzioni(request.porzioni());
        box.setQuantitaInBox(request.quantitaInBox());
        box.setImmagineUrl(request.immagineUrl());
        box.setAttivo(request.attivo() != null ? request.attivo() : box.getAttivo());

        return boxMapper.toResponseDTO(boxRepository.save(box));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "box", key = "#id"),
            @CacheEvict(value = "box_dettagli", key = "#id"),
            @CacheEvict(value = "catalogo_box", allEntries = true),
            @CacheEvict(value = "box_inattive", allEntries = true)
    })
    public void deleteBox(Long id) {
        Box box = boxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Box non trovata con ID: " + id));
        box.setAttivo(false);
        boxRepository.save(box);
    }





}
