package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.IndirizzoUtente;
import com.yumaste.yumasteapi.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndirizzoUtenteRepository extends JpaRepository<IndirizzoUtente, Long> {
        List<IndirizzoUtente> findByUtente(Utente utente);
        List<IndirizzoUtente> findByUtenteAndStato(Utente utente, String stato);
}
