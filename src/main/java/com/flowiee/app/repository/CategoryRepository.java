package com.flowiee.app.repository;

import com.flowiee.app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("from Category c where c.code = 'ROOT' order by c.sort")
    List<Category> findRootCategory();

    @Query("from Category c " +
           "where 1=1 " +
           "and c.type=:type " +
           "and (c.code is null or c.code <> 'ROOT') " +
           "and (:parentId is null or c.parentId=:parentId) " +
           "order by c.sort")
    List<Category> findSubCategory(@Param("type") String type, @Param("parentId") Integer parentId);

    @Query("from Category c where c.type in (:type) and (c.code is null or c.code <> 'ROOT') order by c.sort")
    List<Category> findSubCategory(@Param("type") List<String> type);

    @Query("from Category c " +
           "where 1=1 " +
           "and c.type=:type " +
           "and (c.code is null or c.code <> 'ROOT') " +
           "and (c.isDefault is null or trim(c.isDefault) = '' or c.isDefault = 'N') " +
           "order by c.sort")
    List<Category> findSubCategoryUnDefault(@Param("type") String type);

    @Query("from Category c where c.type=:type and (c.code is null or c.code <> 'ROOT') and c.isDefault = 'Y'")
    Category findSubCategoryDefault(@Param("type") String type);

    @Query("from Category c where c.id in (select p.color.id from ProductVariant p where p.product.id=:productId)")
    List<Category> findColorOfProduct(Integer productId);

    @Query("from Category c where c.id in (select p.size.id from ProductVariant p where p.product.id=:productId and p.color.id=:colorId)")
    List<Category> findSizeOfColorOfProduct(Integer productId, Integer colorId);
}