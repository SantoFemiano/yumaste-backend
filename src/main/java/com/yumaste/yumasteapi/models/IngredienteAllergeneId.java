package com.yumaste.yumasteapi.models;


import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Obbligatorio per le chiavi composte in JPA
public class IngredienteAllergeneId implements Serializable {

    private Long ingredienteId;
    private Long allergeneId;

}