package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.models.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
}
