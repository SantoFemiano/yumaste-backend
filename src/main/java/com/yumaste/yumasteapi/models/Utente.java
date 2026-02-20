package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "UTENTE")
public class Utente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "cf", nullable = false, length = 16)
    private String cf;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "cognome", nullable = false, length = 50)
    private String cognome;

    @Column(name = "data_nascita", nullable = false)
    private LocalDate dataNascita;

    @Column(name = "telefono", nullable = false, length = 15)
    private String telefono;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_c", nullable = false)
    private String passwordC;

    @ColumnDefault("'USER'")
    @Column(name = "ruolo", length = 20)
    private String ruolo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_registrazione")
    private Instant dataRegistrazione;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_aggiornamento")
    private Instant dataAggiornamento;


}