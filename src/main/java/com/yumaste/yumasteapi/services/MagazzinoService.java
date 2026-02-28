package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.MagazzinoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.MagazzinoResponseDTO;
import com.yumaste.yumasteapi.mapper.MagazzinoMapper;
import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.repositories.MagazzinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MagazzinoService {
    private final MagazzinoMapper magazzinoMapper;
    private final MagazzinoRepository magazzinoRepository;

    public MagazzinoResponseDTO addMagazzino(MagazzinoRequestDTO request) {
        Magazzino magazzino = magazzinoRepository.save(magazzinoMapper.toEntity(request));
        return magazzinoMapper.toDto(magazzino);
    }

    public List<MagazzinoResponseDTO> getAllMagazzino() {
        return magazzinoRepository.findAll().stream().map(magazzinoMapper::toDto).toList();
    }

}
