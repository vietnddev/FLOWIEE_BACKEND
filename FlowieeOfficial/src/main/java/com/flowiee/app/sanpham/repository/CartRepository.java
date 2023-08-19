package com.flowiee.app.sanpham.repository;

import com.flowiee.app.sanpham.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    @Query("from Cart c where c.createdBy=:createdBy")
    List<Cart> findByAccountId(String createdBy);
}