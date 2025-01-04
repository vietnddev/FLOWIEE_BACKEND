package com.flowiee.pms.repository.product;

import com.flowiee.pms.entity.product.ProductRelated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRelatedRepository extends JpaRepository<ProductRelated, Long> {
    List<ProductRelated> findByProductId(Long productId);
}