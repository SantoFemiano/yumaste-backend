package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Allergene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AllergeneRepository extends JpaRepository<Allergene, Integer> {

}
