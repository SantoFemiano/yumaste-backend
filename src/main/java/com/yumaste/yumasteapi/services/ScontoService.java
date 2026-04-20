package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.request.ScontoBoxRequestDTO;
import com.yumaste.yumasteapi.DTO.request.ScontoRequestDTO;
import com.yumaste.yumasteapi.DTO.response.ScontoBoxResponseDTO;
import com.yumaste.yumasteapi.DTO.response.ScontoResponseDTO;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScontoService {


    private final ScontoRepository scontoRepository;
    private final ScontoBoxRepository scontoBoxRepository;
    private final BoxRepository boxRepository;
    private final ScontoMapper scontoMapper;
    private final ScontoBoxMapper scontoBoxMapper;

    @Cacheable(value = "sconti")
    public List<ScontoResponseDTO> getSconti(){
        List<Sconto> lista_sconti;
        lista_sconti = scontoRepository.findAll();
        return lista_sconti.stream().map(scontoMapper::toDto).toList();
    }

    @Cacheable(value = "sconti_validi")
    public List<ScontoResponseDTO> getScontiValidi() {
        // Calcola la data di oggi nel momento esatto in cui viene chiamata l'API
        LocalDate oggi = LocalDate.now();

        List<Sconto> scontiValidi = scontoRepository.findScontiAttiviEValidiOggi(oggi);


        return scontiValidi.stream()
                .map(scontoMapper::toDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"sconti", "sconti_validi"}, allEntries = true)
    public ScontoResponseDTO addSconto(ScontoRequestDTO scontoRequestDTO) {
        Sconto sconto = scontoMapper.toEntity(scontoRequestDTO);
        sconto = scontoRepository.save(sconto);
        return scontoMapper.toDto(sconto);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "sconto_box", allEntries = true),
            @CacheEvict(value = "catalogo_box", allEntries = true), // I prezzi nel catalogo cambiano!
            @CacheEvict(value = {"box", "box_dettagli"}, allEntries = true) // I dettagli delle singole box cambiano!
    })
    public List<ScontoBoxResponseDTO> addScontoBox(ScontoBoxRequestDTO request) {

        Sconto sconto = scontoRepository.findById(request.scontoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sconto non trovato con ID: " + request.scontoId()));

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


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "sconto_box", allEntries = true),
            @CacheEvict(value = "catalogo_box", allEntries = true),
            @CacheEvict(value = {"box", "box_dettagli"}, allEntries = true)
    })
    public void removeScontoBox(Long scontoId, Long boxId) {
        ScontoBoxId idComposto = new ScontoBoxId();
        idComposto.setScontoId(scontoId);
        idComposto.setBoxId(boxId);

        if (scontoBoxRepository.existsById(idComposto)) {
            scontoBoxRepository.deleteById(idComposto);
            log.info("Rimosso sconto {} dalla box {}", scontoId, boxId);
        } else {
            throw new RuntimeException("Associazione Sconto-Box non trovata");
        }
    }

    @Cacheable(value = "sconto_box")
    public List<ScontoBoxResponseDTO> getAllScontoBox() {
        return scontoBoxRepository.findAll().stream()
                .map(scontoBoxMapper::toDto)
                .toList();
    }

    @Caching(evict = {
            @CacheEvict(value = {"sconti", "sconti_validi"}, allEntries = true),
            @CacheEvict(value = "catalogo_box", allEntries = true),
            @CacheEvict(value = {"box", "box_dettagli"}, allEntries = true)
    })
    public ScontoResponseDTO updateSconto(Long id, ScontoRequestDTO request) {
        Sconto sconto = scontoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sconto non trovato"));

        sconto.setNome(request.nome());
        sconto.setValore(request.valore());
        sconto.setInizioSconto(request.inizioSconto());
        sconto.setFineSconto(request.fineSconto());
        sconto.setAttivo(request.attivo());

        return scontoMapper.toDto(scontoRepository.save(sconto));
    }

    @Transactional
    @CacheEvict(value = {"sconti", "sconti_validi"}, allEntries = true)
    public void deleteSconto(Long id) {
        if (scontoBoxRepository.existsBySconto_Id(id)) {
            throw new RuntimeException("Impossibile eliminare: lo sconto è ancora associato a una o più Box.");
        }

        Sconto sconto = scontoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sconto non trovato"));

        scontoRepository.delete(sconto);
    }

}