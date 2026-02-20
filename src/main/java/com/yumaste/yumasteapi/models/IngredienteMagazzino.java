package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "INGREDIENTE_MAGAZZINO")
public class IngredienteMagazzino {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "magazzino_id", nullable = false)
    private Magazzino magazzino;

    @Column(name = "quantita", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantita;

    @Column(name = "lotto", length = 20)
    private String lotto;

    @Column(name = "data_ingresso")
    private LocalDate dataIngresso;


}