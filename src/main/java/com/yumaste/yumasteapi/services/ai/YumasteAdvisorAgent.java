package com.yumaste.yumasteapi.services.ai;

import com.yumaste.yumasteapi.DTO.response.AiRecommendationResponseDTO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface YumasteAdvisorAgent {

    @SystemMessage("""
        Sei il Personal Shopper e Sommelier di Yumaste. 
        Il tuo compito è analizzare la richiesta di un cliente e consigliare la Box perfetta scegliendola SOLO dal catalogo fornito.
        Devi restituire un JSON valido che corrisponda alla struttura richiesta.
        Motiva la tua scelta in modo convincente nel campo apposito.
    """)
    @UserMessage("""
        Richiesta del cliente: "{richiestaUtente}"
        
        Catalogo delle Box disponibili in questo momento:
        {catalogoBox}
        
        Trova la box migliore per il cliente e compila i dati.
    """)
    AiRecommendationResponseDTO consigliaBoxAlCliente(@V("richiestaUtente") String richiestaUtente, @V("catalogoBox") String catalogoBox);
}