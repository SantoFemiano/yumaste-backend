package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Ordine;
import com.yumaste.yumasteapi.models.Spedizione;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpedizioneRepository extends JpaRepository<Spedizione,Long> {

    Optional<Spedizione> findByOrdine(Ordine ordine);

}
