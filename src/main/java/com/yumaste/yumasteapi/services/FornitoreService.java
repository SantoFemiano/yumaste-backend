package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.FornitoreRequestDTO;
import com.yumaste.yumasteapi.DTO.response.FornitoreResponseDTO;
import com.yumaste.yumasteapi.mapper.FornitoreMapper;
import com.yumaste.yumasteapi.models.Fornitore;
import com.yumaste.yumasteapi.repositories.FornitoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornitoreService {
    private final FornitoreRepository fornitoreRepository;
    private  final FornitoreMapper fornitoreMapper;

    public FornitoreResponseDTO addFornitore(FornitoreRequestDTO fornitoreRequestDTO) {
        Fornitore fornitore=fornitoreRepository.save(fornitoreMapper.toEntity(fornitoreRequestDTO));
        return fornitoreMapper.toDto(fornitore);
    }

    public List<FornitoreResponseDTO> getAllFornitore(){
        return fornitoreRepository.findAll().stream().map(fornitoreMapper::toDto).toList();
    }

}
