package com.flowiee.app.products.repository;

import com.flowiee.app.products.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    public List<DonHang> findByTrangThai(String trangThai);
}
