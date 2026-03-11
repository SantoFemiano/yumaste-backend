package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.models.DettaglioOrdine;
import com.yumaste.yumasteapi.models.Ordine;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DettaglioOrdineRepository extends JpaRepository<DettaglioOrdine, Long> {

    List<DettaglioOrdine> findByOrdine_Id(Long ordineId);




}
