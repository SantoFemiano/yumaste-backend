package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.BoxRequestDTO;
import com.yumaste.yumasteapi.DTO.response.BoxResponseDTO;
import com.yumaste.yumasteapi.DTO.response.CatalogBoxDTO;
import com.yumaste.yumasteapi.mapper.BoxMapper;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoxService {
    private final BoxRepository boxRepository; //interfaccia per accedere ai dati delle box
    private final BoxMapper boxMapper; //interfaccia per mappare le entità Box in DTO


    public Page<CatalogBoxDTO> getAllActiveBoxes(String categoria,Pageable pageable){
        if(categoria!=null && !categoria.isBlank()) {

            return boxRepository.findByCategoriaAndAttivoTrue(categoria,pageable).map(boxMapper::toCatalogBoxDTO);
        }

        return boxRepository.findByAttivoTrue(pageable).map(boxMapper::toCatalogBoxDTO);
    }

    public Page<CatalogBoxDTO> getBoxById(Long Id,Pageable pageable){
        return boxRepository.findById(Id,pageable).map(boxMapper::toCatalogBoxDTO);
    }

    public BoxResponseDTO insertBox(BoxRequestDTO boxRequestDTO){
        Box nuovaBox = boxMapper.toBox(boxRequestDTO);
        if(nuovaBox.getAttivo()==null){
            nuovaBox.setAttivo(true);
        }
        Box boxsalvata = boxRepository.save(nuovaBox);
        return boxMapper.toResponseDTO(boxsalvata);
    }


}
