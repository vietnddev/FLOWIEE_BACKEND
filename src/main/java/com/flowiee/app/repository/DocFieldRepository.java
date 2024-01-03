package com.flowiee.app.repository;

import com.flowiee.app.entity.DocField;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocFieldRepository extends JpaRepository<DocField, Integer> {
    @Query("from DocField d where d.loaiTaiLieu.id=:docTypeId order by d.sapXep")
    List<DocField> findByDoctype(@Param("docTypeId") Integer docTypeId);
}