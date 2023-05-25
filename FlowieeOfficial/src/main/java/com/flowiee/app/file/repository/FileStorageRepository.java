package com.flowiee.app.file.repository;

import com.flowiee.app.file.entity.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Integer> {
} 
               