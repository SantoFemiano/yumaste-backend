package com.yumaste.yumasteapi.services.ai;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;
import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import com.yumaste.yumasteapi.DTO.response.IngredientiConValoriDTO;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Fornitore;
import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.repositories.AllergeneRepository;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.FornitoreRepository;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import com.yumaste.yumasteapi.services.BoxCompositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.yumaste.yumasteapi.DTO.request.AiRecommendationRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumaste.yumasteapi.DTO.response.AiRecommendationResponseDTO;
import com.yumaste.yumasteapi.DTO.request.ValoriNutrizionaliRequestDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiDescriptionService {


    @Autowired
    private YumasteMarketingAgent marketingAgent;

    public String generaDescrizionePerBox(String nomeBox, String listaIngredienti) {
        // Chiama LangChain4j chiedendo una descrizione di circa 60 parole
        return marketingAgent.generaDescrizioneBox(nomeBox, listaIngredienti, 60);
    }

    public String generaDescrizionePerIngrediente(String nomeIngrediente) {
        return marketingAgent.generaDescrizioneIngrediente(nomeIngrediente);
    }

}
