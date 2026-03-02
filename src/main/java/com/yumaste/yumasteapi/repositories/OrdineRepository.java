package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {
}
