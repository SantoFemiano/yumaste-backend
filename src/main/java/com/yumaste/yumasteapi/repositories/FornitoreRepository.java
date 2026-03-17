package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Fornitore;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FornitoreRepository extends JpaRepository<Fornitore, Long> {

    Optional<Fornitore> findByPartitaIva(String partitaIva);
}
