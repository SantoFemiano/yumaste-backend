package com.yumaste.yumasteapi.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Embeddable
public class ScontoCategoriaId implements Serializable {
    @Serial
    private static final long serialVersionUID = 7416820500468643602L;
    @NotNull
    @Column(name = "sconto_id", nullable = false)
    private Long scontoId;

    @Size(max = 255)
    @NotNull
    @Column(name = "categoria", nullable = false)
    private String categoria;



}