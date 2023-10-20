package com.flowiee.app.danhmuc.repository;

import com.flowiee.app.danhmuc.entity.FabricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FabricTypeRepository extends JpaRepository<FabricType, Integer> {
}