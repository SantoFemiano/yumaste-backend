package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.IngredienteAllergene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngredienteAllergeneRepository extends JpaRepository<IngredienteAllergene, Long> {


    @Query("SELECT DISTINCT a.nome FROM IngredienteAllergene ia " +
            "JOIN ia.allergene a " +
            "WHERE ia.ingrediente.id IN " +
            "(SELECT cb.ingrediente.id FROM ComposizioneBox cb WHERE cb.box.id = :boxId)")

    List<String> findNomiAllergeniByBoxId(@Param("boxId") Long boxId);


    @Query("SELECT ia FROM IngredienteAllergene ia JOIN FETCH ia.ingrediente JOIN FETCH ia.allergene")
    List<IngredienteAllergene> findAllWithDetails();


}
