package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "SPEDIZIONE")
public class Spedizione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "ordine_id", nullable = false)
    private Ordine ordine;

    @Column(name = "corriere", nullable = false, length = 100)
    private String corriere;

    @ColumnDefault("'IN_PREPARAZIONE'")
    @Column(name = "stato_spedizione", nullable = false, length = 20)
    private String statoSpedizione;

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