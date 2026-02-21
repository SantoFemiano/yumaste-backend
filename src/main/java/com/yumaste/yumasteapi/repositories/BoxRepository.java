package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Box;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoxRepository extends JpaRepository<Box,Long> {

    //Metodo per trovare tutte le box attive con paginazione
    Page<Box> findByAttivoTrue(Pageable pageable);

    //Metodo per trovare tutte le box attive di una categoria specifica con paginazione
    Page<Box> findByCategoriaAndAttivoTrue(String categoria, Pageable pageable);

    Page<Box> findById(Long id, Pageable pageable);

    Long id(Long id);
}
