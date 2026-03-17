package com.yumaste.yumasteapi.repositories;
import com.yumaste.yumasteapi.models.Sconto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScontoRepository extends JpaRepository<Sconto, Long> {

    @Query("""
        SELECT s FROM Sconto s
        LEFT JOIN ScontoBox sb ON sb.sconto.id = s.id
        LEFT JOIN ScontoCategoria sc ON sc.sconto.id= s.id
        WHERE s.attivo = true
        AND CURRENT_DATE BETWEEN s.inizioSconto AND s.fineSconto
        AND (sb.box.id = :boxId OR sc.id.categoria = :categoria)
        ORDER BY s.valore DESC
        LIMIT 1
        """)
    Optional<Sconto> findMigliorScontoAttivoPerBox(
            @Param("boxId") Long boxId,
            @Param("categoria") String categoria
    );



    // Seleziona gli sconti dove attivo è true e la data di oggi è compresa tra inizio e fine
    @Query("SELECT s FROM Sconto s WHERE s.attivo = true AND s.inizioSconto <= :oggi AND s.fineSconto >= :oggi")
    List<Sconto> findScontiAttiviEValidiOggi(@Param("oggi") LocalDate oggi);
}