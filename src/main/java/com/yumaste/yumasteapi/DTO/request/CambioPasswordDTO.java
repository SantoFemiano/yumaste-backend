package com.yumaste.yumasteapi.DTO.request;

public record CambioPasswordDTO(
     String vecchiaPassword,
     String nuovaPassword
) {}
