package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.services.testService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface testrepository extends JpaRepository<Allergene, Long> {

}
