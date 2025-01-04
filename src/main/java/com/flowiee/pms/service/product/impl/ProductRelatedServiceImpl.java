package com.flowiee.pms.service.product.impl;

import com.flowiee.pms.entity.product.Product;
import com.flowiee.pms.entity.product.ProductRelated;
import com.flowiee.pms.exception.EntityNotFoundException;
import com.flowiee.pms.repository.product.ProductRelatedRepository;
import com.flowiee.pms.repository.product.ProductRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.ProductRelatedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRelatedServiceImpl extends BaseService implements ProductRelatedService {
    private final ProductRelatedRepository mvProductRelatedRepository;
    private final ProductRepository mvProductRepository;

    @Override
    public List<ProductRelated> get(Long productId) {
        Product product = mvProductRepository.findById(productId)
                .orElseThrow(()-> new EntityNotFoundException(new Object[] {"product"}, null, null));
        return mvProductRelatedRepository.findByProductId(product.getId());
    }

    @Override
    public void add(Long productId, Long productRelatedId) {
        Product product = mvProductRepository.findById(productId)
                .orElseThrow(()-> new EntityNotFoundException(new Object[] {"product"}, null, null));

        Product relatedProduct = mvProductRepository.findById(productRelatedId)
                .orElseThrow(()-> new EntityNotFoundException(new Object[] {"related product"}, null, null));

        ProductRelated relation = new ProductRelated();
        relation.setProduct(product);
        relation.setRelatedProduct(relatedProduct);

        mvProductRelatedRepository.save(relation);
    }

    @Override
    public void remove(Long relationId) {
        mvProductRelatedRepository.findById(relationId)
                .orElseThrow(() -> new EntityNotFoundException(new Object[] {"relation product"}, null, null));
        mvProductRelatedRepository.deleteById(relationId);
    }
}