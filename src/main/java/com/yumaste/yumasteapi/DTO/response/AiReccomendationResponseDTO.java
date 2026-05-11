package com.yumaste.yumasteapi.DTO.response;

public record AiRecommendationResponseDTO(
        Long boxId,
        String messaggio,
        String nomeBox
) {}