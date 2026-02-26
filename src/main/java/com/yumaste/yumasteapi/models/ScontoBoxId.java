package com.yumaste.yumasteapi.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ScontoBoxId implements Serializable {
    private static final long serialVersionUID = 6433577579064131038L;
    @NotNull
    @Column(name = "sconto_id", nullable = false)
    private Long scontoId;

    @NotNull
    @Column(name = "box_id", nullable = false)
    private Long boxId;


}