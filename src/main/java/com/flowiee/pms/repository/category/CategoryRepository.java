package com.flowiee.pms.repository.category;

import com.flowiee.pms.entity.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
           "and (coalesce(:ignoreId) is null or c.id not in (:ignoreId))" +
           "order by c.sort")
    Page<Category> findSubCategory(@Param("type") String type,
                                   @Param("parentId") Integer parentId,
                                   @Param("ignoreId") List<Integer> ignoreId,
                                   Pageable pageable);

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

    @Query("from Category c where c.id in (select p.color.id from ProductDetail p where p.product.id=:productId)")
    List<Category> findColorOfProduct(@Param("productId") Integer productId);

    @Query("from Category c where c.id in (select p.size.id from ProductDetail p where p.product.id=:productId and p.color.id=:colorId)")
    List<Category> findSizeOfColorOfProduct(@Param("productId") Integer productId, @Param("colorId") Integer colorId);

    @Query("select c.type, nvl((select count(*) from Category where code <> 'ROOT' and type = c.type), 0) as total_records " +
           "from Category c " +
           "where c.code = 'ROOT'")
    List<Object[]> totalRecordsOfEachType();
}