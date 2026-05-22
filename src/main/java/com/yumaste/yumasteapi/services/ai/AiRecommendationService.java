package com.yumaste.yumasteapi.services.ai;

import com.yumaste.yumasteapi.DTO.request.AiRecommendationRequestDTO;
import com.yumaste.yumasteapi.DTO.response.AiRecommendationResponseDTO;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiRecommendationService {

    @Autowired
    private YumasteAdvisorAgent advisorAgent;

    @Autowired
    private BoxRepository boxRepository;

    public AiRecommendationResponseDTO ottieniRaccomandazione(AiRecommendationRequestDTO request) {

        // 1. Recuperiamo solo le box attive dal database
        List<Box> boxAttive = boxRepository.findAll().stream()
                .filter(Box::getAttivo)
                .toList();

        // 2. Creiamo una stringa compatta del catalogo per l'IA (per risparmiare token)
        String catalogoCompatto = boxAttive.stream()
                .map(b -> "ID: " + b.getId() + " | Nome: " + b.getNome() + " | Categoria: " + b.getCategoria())
                .collect(Collectors.joining("\n"));

        // 3. Facciamo fare il lavoro a LangChain4j (gestisce parsing JSON e serializzazione)
        return advisorAgent.consigliaBoxAlCliente(request.obiettivo(), catalogoCompatto);
    }
}