package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Box;
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



}
