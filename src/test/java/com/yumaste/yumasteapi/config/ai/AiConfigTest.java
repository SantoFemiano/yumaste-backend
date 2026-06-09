package com.yumaste.yumasteapi.config.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class AiConfigTest {

    private AiConfig aiConfig;

    @BeforeEach
    void setUp() {
        aiConfig = new AiConfig();
        // Inietta la chiave API fittizia tramite ReflectionTestUtils (simula @Value)
        ReflectionTestUtils.setField(aiConfig, "geminiApiKey", "fake-test-api-key-1234");
    }

    @Test
    @DisplayName("chatLanguageModel - bean non null")
    void chatLanguageModel_beanIsNotNull() {
        ChatLanguageModel model = aiConfig.chatLanguageModel();

        assertThat(model).isNotNull();
    }

    @Test
    @DisplayName("chatLanguageModel - bean e' un'istanza di GoogleAiGeminiChatModel")
    void chatLanguageModel_isGoogleAiGeminiInstance() {
        ChatLanguageModel model = aiConfig.chatLanguageModel();

        assertThat(model.getClass().getSimpleName())
                .isEqualTo("GoogleAiGeminiChatModel");
    }

    @Test
    @DisplayName("chatLanguageModel - chiamate successive restituiscono istanze diverse (prototype-like)")
    void chatLanguageModel_eachCallReturnsNewInstance() {
        ChatLanguageModel first  = aiConfig.chatLanguageModel();
        ChatLanguageModel second = aiConfig.chatLanguageModel();

        // I bean @Bean Spring di default sono singleton nel contesto,
        // ma fuori contesto ogni chiamata diretta al metodo crea una nuova istanza.
        assertThat(first).isNotSameAs(second);
    }

    @Test
    @DisplayName("chatLanguageModel - chiave API diversa produce comunque un bean valido")
    void chatLanguageModel_differentApiKey_stillBuildsBean() {
        ReflectionTestUtils.setField(aiConfig, "geminiApiKey", "another-fake-key-5678");

        ChatLanguageModel model = aiConfig.chatLanguageModel();

        assertThat(model).isNotNull();
    }
}
