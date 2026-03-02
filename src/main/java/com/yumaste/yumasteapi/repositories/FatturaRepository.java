package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Fattura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FatturaRepository extends JpaRepository<Fattura,Long> {
}
