package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "FORNITORE")
public class Fornitore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "partita_iva", nullable = false, length = 11)
    private String partitaIva;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "via", nullable = false, length = 100)
    private String via;

    @Column(name = "civico", nullable = false, length = 10)
    private String civico;

    @Column(name = "cap", nullable = false, length = 5)
    private String cap;

    @Column(name = "citta", nullable = false, length = 100)
    private String citta;

    @Column(name = "provincia", nullable = false, length = 4)
    private String provincia;


}