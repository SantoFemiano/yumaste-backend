package com.yumaste.yumasteapi.config.gemini;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    // Recupera la chiave che abbiamo inserito in application.properties
    @Value("${langchain4j.googleai.gemini.chat-model.api-key}")
    private String geminiApiKey;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-3.1-flash-lite") // o "gemini-1.5-flash" se preferisci
                .temperature(0.7)
                .logRequestsAndResponses(true) // Utilissimo per vedere nei log cosa fa l'IA!
                .build();
    }
}