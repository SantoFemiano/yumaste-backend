package com.yumaste.yumasteapi.services.ai;


import com.yumaste.yumasteapi.DTO.response.AiGenerateBoxResponseDTO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface YumasteChefAgent {

    @SystemMessage("""
        Sei il Master Chef di Yumaste, un servizio di food box.
        Il tuo compito è creare ricette ed abbinamenti per nuove box in base alle richieste.
        
        REGOLE FONDAMENTALI:
        1. Non puoi inventare gli ingredienti. Devi usare ESCLUSIVAMENTE lo strumento di ricerca 'cercaIngredientePerNome' per verificare se un ingrediente esiste nel nostro magazzino.
        2. Se un utente chiede una categoria generica (es. "pesce"), usa la tua conoscenza culinaria per ipotizzare i nomi di pesci specifici (es. "salmone", "tonno") e cercali nel magazzino tramite il tool.
        3. Se un ingrediente cercato non c'è, cerca un'alternativa valida.
        4. Restituisci i dati rigorosamente nel formato JSON richiesto dal sistema.
    """)
    @UserMessage("""
        Richiesta del cliente: "{{richiesta}}"
        
        Costruisci la box perfetta per questa richiesta usando gli ingredienti a magazzino.
    """)
    AiGenerateBoxResponseDTO generaBoxDaRichiesta(String richiesta);
}
