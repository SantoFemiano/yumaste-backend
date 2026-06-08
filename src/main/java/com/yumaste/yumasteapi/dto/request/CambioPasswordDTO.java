package com.yumaste.yumasteapi.dto.request;

public record CambioPasswordDTO(
     String vecchiaPassword,
     String nuovaPassword
) {}
