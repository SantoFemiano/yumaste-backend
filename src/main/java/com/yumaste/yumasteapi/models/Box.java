package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "BOX")
public class Box {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ean", nullable = false, length = 13)
    private String ean;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "porzioni", nullable = false)
    private Byte porzioni;

    @Column(name = "quantita_in_box", nullable = false)
    private Integer quantitaInBox;

    @Column(name = "prezzo", nullable = false, precision = 5, scale = 2)
    private BigDecimal prezzo;

    @Column(name = "immagine_url", length = 500)
    private String immagineUrl;

    @ColumnDefault("1")
    @Column(name = "attivo")
    private Boolean attivo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_creazione")
    private Instant dataCreazione;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_aggiornamento")
    private Instant dataAggiornamento;


}