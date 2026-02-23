package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.ComposizioneBox;
import com.yumaste.yumasteapi.models.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoxCompositionRepository extends JpaRepository<ComposizioneBox,Long> {
    List<ComposizioneBox> findByBox(Box box);
    Optional<ComposizioneBox> findByBoxAndIngrediente(Box box, Ingrediente ingrediente);
}
