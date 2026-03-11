package com.yumaste.yumasteapi.repositories;


import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.Carrello;
import com.yumaste.yumasteapi.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Carrello, Long> {

    List<Carrello> findByUtente(Utente utente);

    Optional<Carrello> findByUtenteAndBox(Utente utente, Box box);

    Optional<Carrello> findByUtenteAndBoxId(Utente utente, Long aLong);
}
