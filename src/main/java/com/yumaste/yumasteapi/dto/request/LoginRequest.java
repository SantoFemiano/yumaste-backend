package com.yumaste.yumasteapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "L'email non è valida")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    private String password;
}