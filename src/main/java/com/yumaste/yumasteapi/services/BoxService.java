package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.models.Sconto;
import com.yumaste.yumasteapi.DTO.request.BoxRequestDTO;
import com.yumaste.yumasteapi.DTO.response.*;
import com.yumaste.yumasteapi.mapper.BoxMapper;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.IngredienteAllergeneRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Page<CatalogBoxDTO> getAllActiveBoxes(String categoria,Pageable pageable){
        if(categoria!=null && !categoria.isBlank()) {

            return boxRepository.findByCategoriaAndAttivoTrue(categoria,pageable).map(this::mapToCatalogBoxDTOConSconto);
        }

        return boxRepository.findByAttivoTrue(pageable).map(this::mapToCatalogBoxDTOConSconto);
    }

    public Page<CatalogBoxDTO> getBoxById(Long Id,Pageable pageable){
        return boxRepository.findById(Id,pageable).map(this::mapToCatalogBoxDTOConSconto);
    }

    public BoxResponseDTO insertBox(BoxRequestDTO boxRequestDTO){
        Box nuovaBox = boxMapper.toBox(boxRequestDTO);
        if(nuovaBox.getAttivo()==null){
            nuovaBox.setAttivo(true);
        }
        Box boxsalvata = boxRepository.save(nuovaBox);
        return boxMapper.toResponseDTO(boxsalvata);
    }

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

            if (ingrediente.chilocalorie() != null) {
                totKcal = totKcal.add(ingrediente.chilocalorie());
            }
            if (ingrediente.proteine() != null) {
                totProteine = totProteine.add(ingrediente.proteine());
            }
            if (ingrediente.carboidrati() != null) {
                totCarboidrati = totCarboidrati.add(ingrediente.carboidrati());
            }
            if (ingrediente.grassi() != null) {
                totGrassi = totGrassi.add(ingrediente.grassi());
            }
            if (ingrediente.zuccheri() != null) {
                totZuccheri = totZuccheri.add(ingrediente.zuccheri());
            }
            if (ingrediente.fibre() != null) {
                totFibre = totFibre.add(ingrediente.fibre());
            }
            if (ingrediente.sale() != null) {
                totSale = totSale.add(ingrediente.sale());
            }


        }


        NutritionalValueDetailDTO macroTotali = new NutritionalValueDetailDTO(
                totProteine,
                totCarboidrati,
                totGrassi,
                totFibre,
                totZuccheri,
                totSale,
                totKcal.intValue() // Trasformo le Kcal totali in numero intero (es. 450 invece di 450.00)
        );

        //ricaviamo la lista degli allergeni.
        List<String> allergeniDellaBox = ingredienteAllergeneRepository.findNomiAllergeniByBoxId(boxId);


Dati_Sconto datiScontobox = calcolaSconto(box);

        return new BoxDetailDTO(
                box.getId(),
                box.getNome(),
                box.getCategoria(),
                datiScontobox.originale,
                datiScontobox.scontato,
                datiScontobox.percentuale,
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
                box.getImmagineUrl()
        );
    }


}
