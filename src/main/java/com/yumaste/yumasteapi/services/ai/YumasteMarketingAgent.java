package com.yumaste.yumasteapi.services.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface YumasteMarketingAgent {
    @SystemMessage("""
        Sei il Chief Copywriter di Yumaste, un e-commerce di food box di alta qualità.
        Il tuo compito è scrivere descrizioni accattivanti, SEO-friendly e persuasive per i prodotti.
        Mantieni un tono caldo, invitante e professionale, focalizzandoti sui sapori e sull'esperienza culinaria.
        IMPORTANTE: Restituisci SOLO il testo della descrizione. Niente formattazioni extra, niente frasi come "Ecco la tua descrizione:".
    """)
    @UserMessage("""
        Scrivi una descrizione di massimo {maxParole} parole per la box chiamata "{nomeBox}".
        La box contiene questi ingredienti principali: {ingredienti}.
    """)
    String generaDescrizioneBox(@V("nomeBox") String nomeBox, @V("ingredienti") String ingredienti, @V("maxParole") int maxParole);

    @UserMessage("Scrivi una breve ed elegante frase commerciale (max 20 parole) per valorizzare questo ingrediente: {nomeIngrediente}.")
    String generaDescrizioneIngrediente(@V("nomeIngrediente") String nomeIngrediente);
}
