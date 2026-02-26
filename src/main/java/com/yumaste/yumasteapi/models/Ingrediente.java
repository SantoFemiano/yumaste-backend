package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "INGREDIENTE")
public class Ingrediente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ean", nullable = false, length = 13)
    private String ean;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornitore_id", nullable = false)
    private Fornitore fornitore;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descrizione")
    private String descrizione;

    @ColumnDefault("'g'")
    @Column(name = "unita_misura", nullable = false, length = 10)
    private String unitaMisura;

    @Column(name = "prezzo_per_unita", nullable = false, precision = 7, scale = 4)
    private BigDecimal prezzoPerUnita;

    @ColumnDefault("1")
    @Column(name = "attivo")
    private Boolean attivo;

    @CreationTimestamp
    @Column(name = "data_creazione")
    private Instant dataCreazione;

    @UpdateTimestamp
    @Column(name = "data_aggiornamento")
    private Instant dataAggiornamento;


    @OneToMany(mappedBy = "ingrediente", fetch = FetchType.LAZY)
    private List<IngredienteAllergene> allergeni;

}