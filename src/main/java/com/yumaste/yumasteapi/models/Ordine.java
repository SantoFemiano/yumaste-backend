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
@Table(name = "ORDINE")
@NoArgsConstructor
public class Ordine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "codice_ordine", nullable = false, length = 50)
    private String codiceOrdine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;


    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_ordine")
    private Instant dataOrdine;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_aggiornamento")
    private Instant dataAggiornamento;

    @Column(name = "totale_prezzo", nullable = false, precision = 7, scale = 2)
    private BigDecimal totalePrezzo;

    @Column(name = "totale_quantita", nullable = false)
    private Integer totaleQuantita;

    @ColumnDefault("'IN_ATTESA'")
    @Column(name = "stato_ordine", length = 20)
    private String statoOrdine;


}