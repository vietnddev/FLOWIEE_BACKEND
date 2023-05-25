package com.flowiee.app.danhmuc.repository;

import com.flowiee.app.danhmuc.entity.LoaiTaiLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoaiTaiLieuRepository extends JpaRepository<LoaiTaiLieu, Integer> {
    LoaiTaiLieu findByTen(String ten);

    List<LoaiTaiLieu> findByTrangThai(boolean trangThai);
}