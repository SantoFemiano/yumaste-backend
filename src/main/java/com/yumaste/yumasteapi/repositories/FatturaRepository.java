package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Fattura;
import com.yumaste.yumasteapi.models.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FatturaRepository extends JpaRepository<Fattura,Long> {

    Optional<Fattura> findByOrdine(Ordine ordine);
}
