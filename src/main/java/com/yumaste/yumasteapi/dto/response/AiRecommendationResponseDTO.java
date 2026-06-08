package com.yumaste.yumasteapi.dto.response;

public record AiRecommendationResponseDTO(
        Long boxId,
        String messaggio,
        String nomeBox
) {}