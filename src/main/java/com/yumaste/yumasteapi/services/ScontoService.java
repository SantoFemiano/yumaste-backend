package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.ScontoBoxRequestDTO;
import com.yumaste.yumasteapi.DTO.request.ScontoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.ScontoBoxResponseDTO;
import com.yumaste.yumasteapi.DTO.response.ScontoResponseDTO;
import com.yumaste.yumasteapi.mapper.ScontoBoxMapper;
import com.yumaste.yumasteapi.mapper.ScontoMapper;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Sconto;
import com.yumaste.yumasteapi.models.ScontoBox;
import com.yumaste.yumasteapi.models.ScontoBoxId;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.ScontoBoxRepository;
import com.yumaste.yumasteapi.repositories.ScontoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScontoService {


    private final ScontoRepository scontoRepository;
    private final ScontoBoxRepository scontoBoxRepository;
    private final BoxRepository boxRepository;
    private final ScontoMapper scontoMapper;
    private final ScontoBoxMapper scontoBoxMapper;


    public ScontoResponseDTO addSconto(ScontoRequestDTO scontoRequestDTO) {
        Sconto sconto = scontoMapper.toEntity(scontoRequestDTO);
        sconto = scontoRepository.save(sconto);
        return scontoMapper.toDto(sconto);
    }

    @Transactional
    public List<ScontoBoxResponseDTO> addScontoBox(ScontoBoxRequestDTO request) {

        Sconto sconto = scontoRepository.findById(request.scontoId())
                .orElseThrow(() -> new RuntimeException("Sconto non trovato con ID: " + request.scontoId()));

        List<Box> boxesTrovate = boxRepository.findAllById(request.boxIds());
        List<ScontoBox> associazioniDaSalvare = new ArrayList<>();

        for (Box box : boxesTrovate) {
            ScontoBoxId idComposto = new ScontoBoxId();
            idComposto.setScontoId(sconto.getId());
            idComposto.setBoxId(box.getId());

            if (!scontoBoxRepository.existsById(idComposto)) {
                ScontoBox nuovaAssociazione = new ScontoBox();
                nuovaAssociazione.setId(idComposto);
                nuovaAssociazione.setSconto(sconto);
                nuovaAssociazione.setBox(box);
                associazioniDaSalvare.add(nuovaAssociazione);
            } else {
                log.warn("Lo sconto {} è già applicato alla Box {}", sconto.getNome(), box.getNome());
            }
        }


        if (!associazioniDaSalvare.isEmpty()) {
            associazioniDaSalvare = scontoBoxRepository.saveAll(associazioniDaSalvare);
            log.info("Salvate con successo {} nuove associazioni Sconto-Box", associazioniDaSalvare.size());
        } else {
            log.info("Nessuna nuova associazione da salvare (erano già tutte presenti).");
        }


        return associazioniDaSalvare.stream()
                .map(scontoBoxMapper::toDto)
                .toList();
    }
}