package com.yumaste.yumasteapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingrediente_allergene")
@Getter
@Setter
@NoArgsConstructor // Lombok genera il costruttore vuoto, fondamentale per JPA
public class IngredienteAllergene {

    // 1. Incorporo la chiave composta creata prima
    @EmbeddedId
    private IngredienteAllergeneId id = new IngredienteAllergeneId();

    // 2. Relazione Molti a Uno verso Ingrediente
    @ManyToOne(fetch = FetchType.LAZY) // LAZY ottimizza le prestazioni: non carica l'ingrediente se non lo chiedi esplicitamente
    @MapsId("ingredienteId")           // Collega questa entità al pezzo della chiave composta
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    // 3. Relazione Molti a Uno verso Allergene
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("allergeneId")
    @JoinColumn(name = "allergene_id")
    private Allergene allergene;

    // 4. La colonna "extra" di questa tabella ponte
    @Column(name = "tipo_presenza", length = 50)
    private String tipoPresenza; // Es. "PRESENTE" o "TRACCE"

}