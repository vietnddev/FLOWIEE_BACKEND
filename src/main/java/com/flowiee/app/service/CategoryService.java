package com.flowiee.app.service;

import com.flowiee.app.base.BaseService;

import java.util.List;

import com.flowiee.app.entity.Category;
import org.springframework.web.multipart.MultipartFile;

public interface CategoryService extends BaseService<Category> {
    List<Category> findRootCategory();

    List<Category> findSubCategory(String categoryType);

    List<Category> findSubCategory(List<String> categoryTypes);

    Category findSubCategoryDefault(String categoryType);

    List<Category> findSubCategoryUnDefault(String categoryType);

    Boolean categoryInUse(Integer categoryId);
    
    String importData(MultipartFile fileImport, String categoryType);

    byte[] exportTemplate(String categoryType);

    byte[] exportData(String categoryType);
}