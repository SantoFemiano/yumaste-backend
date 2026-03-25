package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Ordine;
import com.yumaste.yumasteapi.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {

    List<Ordine> findByUtente(Utente utente);

    // Conta tutti gli ordini che non sono stati annullati
    @Query("SELECT COUNT(o) FROM Ordine o WHERE o.statoOrdine != 'ANNULLATO'")
    Long countOrdiniValidi();

    // Somma il totale di tutti gli ordini non annullati
    @Query("SELECT SUM(o.totalePrezzo) FROM Ordine o WHERE o.statoOrdine != 'ANNULLATO'")
    BigDecimal sumIncassoTotale();


}
