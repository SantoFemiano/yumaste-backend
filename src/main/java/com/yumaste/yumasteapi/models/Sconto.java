package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "sconto")
public class Sconto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @ColumnDefault("'Sconto Generico'")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotNull
    @Column(name = "valore", nullable = false)
    private Integer valore;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "attivo", nullable = false)
    private Boolean attivo;

    @NotNull
    @Column(name = "inizio_sconto", nullable = false)
    private LocalDate inizioSconto;

    @NotNull
    @Column(name = "fine_sconto", nullable = false)
    private LocalDate fineSconto;


}