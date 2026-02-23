package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Ingrediente;
import com.yumaste.yumasteapi.models.ValoriNutrizionali;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionalValueRepository extends JpaRepository<ValoriNutrizionali, Integer> {

    Optional<ValoriNutrizionali> findByIngrediente(Ingrediente ingrediente);

}
