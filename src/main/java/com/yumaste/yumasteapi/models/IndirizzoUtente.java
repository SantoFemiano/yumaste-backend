package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "INDIRIZZO_UTENTE")
@NoArgsConstructor
public class IndirizzoUtente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(name = "stato", nullable = false, length = 10)
    private String stato;

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