package com.flowiee.app.service.product;

import com.flowiee.app.base.BaseService;
import com.flowiee.app.entity.ProductVariant;
import com.flowiee.app.model.product.ProductVariantResponse;

import java.util.List;

public interface ProductVariantService extends BaseService<ProductVariant> {
    List<ProductVariantResponse>  getListVariantOfProduct(Integer sanPhamId);

    Double getGiaBan(int id);

    String updateSoLuong(Integer soLuong, Integer id);

    List<ProductVariant> findByImportId(Integer importId);
}