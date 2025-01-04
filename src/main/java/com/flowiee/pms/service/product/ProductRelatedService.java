package com.flowiee.pms.service.product;

import com.flowiee.pms.entity.product.ProductRelated;

import java.util.List;

public interface ProductRelatedService {
    List<ProductRelated> get(Long productId);

    void add(Long productId, Long productRelatedId);

    void remove(Long relationId);
}