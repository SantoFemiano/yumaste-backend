package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Ordine;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoxRepository extends JpaRepository<Box,Long> {

    //Metodo per trovare tutte le box attive con paginazione
    Page<Box> findByAttivoTrue(Pageable pageable);

    //Metodo per trovare tutte le box attive di una categoria specifica con paginazione
    Page<Box> findByCategoriaAndAttivoTrue(String categoria, Pageable pageable);

    //Metodo per trovare una box attiva per id con paginazione
    Page<Box> findById(Long id, Pageable pageable);


    @Override
    @NonNull
    Optional<Box> findById(Long id);

    // 1. Cerca SOLO per nome (es. l'utente è in "Tutte" e cerca "Pollo")
    Page<Box> findByNomeContainingIgnoreCaseAndAttivoTrue(String nome, Pageable pageable);

    // 2. Cerca per Categoria E per nome (es. l'utente è in "Asiatica" e cerca "Pollo")
    Page<Box> findByCategoriaAndNomeContainingIgnoreCaseAndAttivoTrue(String categoria, String nome, Pageable pageable);

}
