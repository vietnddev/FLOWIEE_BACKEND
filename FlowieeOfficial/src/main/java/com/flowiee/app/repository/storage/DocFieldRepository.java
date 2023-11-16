package com.flowiee.app.repository.storage;

import com.flowiee.app.entity.DocField;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocFieldRepository extends JpaRepository<DocField, Integer> {
    @Query("from DocField d where d.loaiTaiLieu.id=:loaiTaiLieu order by d.sapXep")
    List<DocField> findByDoctype(Integer loaiTaiLieu);
}