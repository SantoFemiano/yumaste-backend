package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Magazzino;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MagazzinoRepository extends JpaRepository<Magazzino,Integer> {
    Optional<Magazzino> findById(@NotNull Long aLong);
}
