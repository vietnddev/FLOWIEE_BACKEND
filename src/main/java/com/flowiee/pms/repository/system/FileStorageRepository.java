package com.flowiee.pms.repository.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.flowiee.pms.entity.system.FileStorage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {
    @Query("from FileStorage f " +
           "where 1=1 " +
           "and (:module is null or f.module=:module) " +
           "and (:productId is null or (f.product.id=:productId and f.productDetail.id is null)) " +
           "and (:productVariantId is null or f.productDetail.id=:productVariantId) " +
           "order by f.createdAt")
    List<FileStorage> findAllImages(@Param("module") String module,
                                    @Param("productId") Long productId,
                                    @Param("productVariantId") Long productVariantId);

    @Query("from FileStorage f " +
           "where 1=1 " +
           "and (:productId is null or (f.product.id=:productId and f.productDetail.id is null)) " +
           "and (:productVariantId is null or f.productDetail.id=:productVariantId) " +
           "and f.isActive is true " +
           "order by f.createdAt")
    FileStorage findActiveImage(@Param("productId") Long productId, @Param("productVariantId") Long productVariantId);

    @Query("from FileStorage f where f.createdAt=:createdTime")
    FileStorage findByCreatedTime(@Param("createdTime") LocalDateTime createdTime);

    @Query("from FileStorage f where f.order.id=:orderId")
    FileStorage findQRCodeOfOrder(@Param("orderId") Long orderId);
}