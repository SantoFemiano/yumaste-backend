package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.models.IngredienteMagazzino;
import com.yumaste.yumasteapi.models.Magazzino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredienteMagazzinoRepository extends JpaRepository<IngredienteMagazzino, Long> {
    Optional<IngredienteMagazzino> findByMagazzinoAndIngredienteAndLotto(
            Magazzino magazzino, Ingrediente ingrediente, String lotto
    );
}
