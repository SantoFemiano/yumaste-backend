package com.yumaste.yumasteapi.config.gemini;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Bean
    public Client geminiClient() {
        // Inizializza il client ufficiale di Google con la tua API Key
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}