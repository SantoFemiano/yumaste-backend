package com.yumaste.yumasteapi.repositories;

import com.yumaste.yumasteapi.models.ScontoBox;
import com.yumaste.yumasteapi.models.ScontoBoxId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScontoBoxRepository extends JpaRepository<ScontoBox, ScontoBoxId> {
}
