package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Ordine;
import com.yumaste.yumasteapi.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {


    List<Ordine> findByUtente(Utente utente);
}
