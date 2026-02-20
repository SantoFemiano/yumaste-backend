package com.yumaste.yumasteapi.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String cf;
    private String nome;
    private String cognome;
    private LocalDate dataNascita; // Es. "1990-05-20"
    private String telefono;
    private String email;
    private String password;
}