package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.DettaglioOrdine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface DettaglioOrdineRepository extends JpaRepository<DettaglioOrdine, Long> {

    List<DettaglioOrdine> findByOrdine_Id(Long ordineId);


    @Query("""
        SELECT d FROM DettaglioOrdine d
        JOIN FETCH d.box
        JOIN FETCH d.ordine o
        WHERE o.utente.id = :utenteId
        ORDER BY o.dataOrdine DESC
        LIMIT :limit
    """)
    List<DettaglioOrdine> findUltimiDettagliByUtenteId(
            @Param("utenteId") Long utenteId,
            @Param("limit") int limit
    );

    @Query("SELECT DISTINCT d.box.id FROM DettaglioOrdine d WHERE d.ordine.utente.id = :utenteId")
    List<Long> findBoxIdOrdinateByUtenteId(@Param("utenteId") Long utenteId);
}



