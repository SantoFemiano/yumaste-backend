package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.MagazzinoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.MagazzinoResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.mapper.MagazzinoMapper;
import com.yumaste.yumasteapi.models.Magazzino;
import com.yumaste.yumasteapi.repositories.MagazzinoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MagazzinoService {
    private final MagazzinoMapper magazzinoMapper;
    private final MagazzinoRepository magazzinoRepository;

    @Transactional
    @CacheEvict(value = "magazzino", allEntries = true)
    public MagazzinoResponseDTO addMagazzino(MagazzinoRequestDTO request) {
        Magazzino magazzino = magazzinoRepository.save(magazzinoMapper.toEntity(request));
        return magazzinoMapper.toDto(magazzino);
    }

    @Cacheable(value = "magazzino")
    public List<MagazzinoResponseDTO> getAllMagazzino() {
        return magazzinoRepository.findAll().stream().map(magazzinoMapper::toDto).toList();
    }

    @Transactional
    @CacheEvict(value = "magazzino", allEntries = true)
    public void deleteMagazzino(long id){
        Magazzino magazzino = magazzinoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Magazzino non trovato con id: " + id));
        magazzinoRepository.delete(magazzino);
    }

    @Transactional
    @CacheEvict(value = "magazzino", allEntries = true)
    public MagazzinoResponseDTO updateMagazzino(Long id, MagazzinoRequestDTO request) {
        Magazzino magazzino = magazzinoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Magazzino non trovato"));

        magazzino.setNome(request.nome());
        magazzino.setCap(request.cap());
        magazzino.setVia(request.via());
        magazzino.setCivico(request.civico());
        magazzino.setProvincia(request.provincia());
        magazzino.setCitta(request.citta());

        return magazzinoMapper.toDto(magazzinoRepository.save(magazzino));

    }

}
