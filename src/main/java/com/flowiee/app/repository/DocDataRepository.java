package com.flowiee.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.flowiee.app.entity.DocData;

import java.util.List;

@Repository
public interface DocDataRepository extends JpaRepository<DocData, Integer> {
    @Query("from DocData d where d.docField.id=:docFieldId")
    List<DocData> findByDocField(@Param("docFieldId") Integer docFieldId);

    @Query("from DocData d where d.docField.id=:docFieldId and d.document.id=:documentId")
    DocData findByFieldIdAndDocId(@Param("docFieldId") Integer docFieldId, @Param("documentId") Integer documentId);
}