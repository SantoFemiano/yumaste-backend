package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.DettaglioOrdine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DettaglioOrdineRepository extends JpaRepository<DettaglioOrdine, Long> {

    List<DettaglioOrdine> findByOrdine_Id(Long ordineId);




}
