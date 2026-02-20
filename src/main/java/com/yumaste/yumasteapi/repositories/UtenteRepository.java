package com.yumaste.yumasteapi.repositories;
import com.yumaste.yumasteapi.models.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente,Long> {
    Optional<Utente> findByEmail(String email);
}
