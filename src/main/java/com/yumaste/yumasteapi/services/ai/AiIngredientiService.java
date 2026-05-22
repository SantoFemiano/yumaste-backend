package com.yumaste.yumasteapi.services.ai;

import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiIngredientiService {

    @Autowired
    private YumasteInventoryAgent inventoryAgent;


    public List<IngredienteRequestDTO> generaNuoviIngredienti(int quantita, String descrizione) {

        // LangChain4j invia il prompt, riceve il JSON, lo valida e lo trasforma in una Lista di DTO
        return inventoryAgent.generaIngredientiDaDescrizione(quantita, descrizione);

    }
}