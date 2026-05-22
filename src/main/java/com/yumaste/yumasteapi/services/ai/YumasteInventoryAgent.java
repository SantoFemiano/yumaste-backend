package com.yumaste.yumasteapi.services.ai;

import com.yumaste.yumasteapi.DTO.request.IngredienteRequestDTO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
public interface YumasteInventoryAgent {

    @SystemMessage("""
        Sei il Responsabile Approvvigionamenti e Nutrizione di Yumaste.
        Il tuo compito è analizzare le richieste dell'utente e generare un elenco di nuovi ingredienti culinari realistici.
        
        REGOLE:
        1. Compila tutti i campi richiesti dal sistema (nome, categoria, unitaMisura, prezzo, valori nutrizionali, ecc.).
        2. I prezzi devono essere realistici per il mercato italiano.
        3. Devi restituire ESATTAMENTE un array JSON, senza alcuna formattazione markdown (niente ```json) o testo introduttivo.
    """)
    @UserMessage("""
        Genera {quantita} nuovi ingredienti seguendo esattamente questa descrizione o vincolo: "{descrizione}".
    """)
        // La magia è qui: LangChain4j capisce che deve restituire una List di DTO!
    List<IngredienteRequestDTO> generaIngredientiDaDescrizione(@V("quantita") int quantita, @V("descrizione") String descrizione);
}