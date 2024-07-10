package com.flowiee.pms.controller.category;

import com.flowiee.pms.controller.BaseController;
import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.exception.ResourceNotFoundException;
import com.flowiee.pms.model.EximModel;
import com.flowiee.pms.service.ExportService;
import com.flowiee.pms.service.ImportService;
import com.flowiee.pms.service.category.CategoryService;
import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.constants.Pages;

import com.flowiee.pms.utils.constants.CategoryType;
import com.flowiee.pms.utils.constants.TemplateExport;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@CrossOrigin
@RestController
@RequestMapping("/system/category")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryControllerView extends BaseController {
    CategoryService categoryService;
    ExportService   exportService;
    ImportService   importService;

    public CategoryControllerView(CategoryService categoryService, @Qualifier("categoryExportServiceImpl") ExportService exportService, @Qualifier("categoryImportServiceImpl") ImportService importService) {
        this.categoryService = categoryService;
        this.exportService = exportService;
        this.importService = importService;
    }

    @GetMapping
    @PreAuthorize("@vldModuleCategory.readCategory(true)")
    public ModelAndView viewRootCategory() {
        ModelAndView modelAndView = new ModelAndView(Pages.CTG_CATEGORY.getTemplate());
        modelAndView.addObject("category", new Category());
        modelAndView.addObject("listCategory", categoryService.findRootCategory());
        return baseView(modelAndView);
    }

    @GetMapping("/{type}")
    @PreAuthorize("@vldModuleCategory.readCategory(true)")
    public ModelAndView viewSubCategory(@PathVariable("type") String categoryType) {
        if (CommonUtils.getCategoryType(categoryType) == null) {
            throw new ResourceNotFoundException("Category not found!");
        }
        ModelAndView modelAndView = new ModelAndView(Pages.CTG_CATEGORY_DETAIL.getTemplate());
        modelAndView.addObject("categoryType", categoryType);
        modelAndView.addObject("ctgRootName", CategoryType.valueOf(CommonUtils.getCategoryType(categoryType)).getLabel());
        modelAndView.addObject("templateImportName", TemplateExport.EX_LIST_OF_CATEGORIES);
        modelAndView.addObject("url_template", "");
        modelAndView.addObject("url_import", "");
        modelAndView.addObject("url_export", "");
        return baseView(modelAndView);
    }

    @GetMapping("/{type}/template")
    @PreAuthorize("@vldModuleCategory.importCategory(true)")
    public ResponseEntity<InputStreamResource> exportTemplate(@PathVariable("type") String categoryType) {
        EximModel model = exportService.exportToExcel(TemplateExport.EX_LIST_OF_CATEGORIES, null, true);
        return ResponseEntity.ok().headers(model.getHttpHeaders()).body(model.getContent());
    }

    @PostMapping("/{type}/import")
    @PreAuthorize("@vldModuleCategory.importCategory(true)")
    public ModelAndView importData(@PathVariable("type") String categoryType, @RequestParam("file") MultipartFile file) {
        if (CommonUtils.getCategoryType(categoryType) == null) {
            throw new ResourceNotFoundException("Category not found!");
        }
        importService.importFromExcel(TemplateExport.IM_LIST_OF_CATEGORIES, file);
        return new ModelAndView("redirect:/{type}");
    }

    @GetMapping("/{type}/export")
    @PreAuthorize("@vldModuleCategory.readCategory(true)")
    public ResponseEntity<InputStreamResource> exportData(@PathVariable("type") String categoryType) {
        EximModel model = exportService.exportToExcel(TemplateExport.EX_LIST_OF_CATEGORIES, null, false);
        return ResponseEntity.ok().headers(model.getHttpHeaders()).body(model.getContent());
    }
}