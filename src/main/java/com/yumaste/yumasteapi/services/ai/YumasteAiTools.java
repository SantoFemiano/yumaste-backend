package com.yumaste.yumasteapi.services.ai;

import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.repositories.IngredienteRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class YumasteAiTools {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Tool("Cerca gli ingredienti nel magazzino tramite una parola chiave o il nome (es. 'salmone', 'farina', 'zucchine').")
    public String cercaIngredientePerNome(String parolaChiave) {

        List<Ingrediente> ingredienti = ingredienteRepository.findByNomeContainingIgnoreCase(parolaChiave);

        if (ingredienti.isEmpty()) {
            return "Nessun ingrediente trovato in magazzino contenente : " + parolaChiave;
        }

        return ingredienti.stream()
                .map(i-> String.format("ID: %d, Nome: %s, Prezzo: €%.2f",i.getId(),i.getNome(),i.getPrezzoPerUnita()))
                .collect(Collectors.joining(" | "));

    }
}
