package com.flowiee.app.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.flowiee.app.entity.Price;
import com.flowiee.app.entity.ProductVariant;

import java.util.List;

public interface PriceRepository extends JpaRepository<Price, Integer> {
    @Query("from Price p where p.productVariant=:productVariantId order by p.trangThai desc, p.createdAt desc")
    List<Price> findPricesByProductVariant(ProductVariant productVariantId);

    @Query("select g.giaBan from Price g where g.productVariant=:productVariantId and g.trangThai=:trangThai")
    Double findGiaBanHienTai(ProductVariant productVariantId, boolean trangThai);
}