package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "SCONTO")
public class Sconto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "valore", nullable = false)
    private Integer valore;

    @Column(name = "inizio_sconto", nullable = false)
    private LocalDate inizioSconto;

    @Column(name = "fine_sconto", nullable = false)
    private LocalDate fineSconto;


}