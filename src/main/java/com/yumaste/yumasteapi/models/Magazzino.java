package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "MAGAZZINO")
@NoArgsConstructor
public class Magazzino {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nome", nullable = false, length = 50)
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