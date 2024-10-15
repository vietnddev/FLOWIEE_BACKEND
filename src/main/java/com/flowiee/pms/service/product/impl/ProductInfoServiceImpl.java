package com.flowiee.pms.service.product.impl;

import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.product.*;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.exception.ResourceNotFoundException;
import com.flowiee.pms.repository.product.ProductDescriptionRepository;
import com.flowiee.pms.utils.ChangeLog;
import com.flowiee.pms.utils.constants.*;
import com.flowiee.pms.model.dto.ProductDTO;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.DataInUseException;
import com.flowiee.pms.model.dto.VoucherApplyDTO;
import com.flowiee.pms.model.dto.VoucherInfoDTO;
import com.flowiee.pms.repository.category.CategoryRepository;
import com.flowiee.pms.repository.product.ProductRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.*;
import com.flowiee.pms.service.sales.VoucherApplyService;
import com.flowiee.pms.service.sales.VoucherService;
import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.converter.ProductConvert;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductInfoServiceImpl extends BaseService implements ProductInfoService {
    VoucherService mvVoucherInfoService;
    ProductRepository mvProductRepository;
    CategoryRepository mvCategoryRepository;
    VoucherApplyService mvVoucherApplyService;
    ProductImageService mvProductImageService;
    ProductVariantService mvProductVariantService;
    ProductHistoryService mvProductHistoryService;
    ProductStatisticsService mvProductStatisticsService;
    ProductDescriptionRepository mvProductDescriptionRepository;

    @Override
    public List<ProductDTO> findAll() {
        return this.findAll(null, -1, -1, null, null, null, null, null, null, null).getContent();
    }

    @Override
    public Page<ProductDTO> findAll(PID pPID , int pageSize, int pageNum, String pTxtSearch, Integer pBrand, Integer pProductType,
                                    Integer pColor, Integer pSize, Integer pUnit, String pStatus) {
        Pageable pageable = Pageable.unpaged();
        if (pageSize >= 0 && pageNum >= 0) {
            pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        }
        Page<Product> products = mvProductRepository.findAll(pPID.getId(), pTxtSearch, pBrand, pProductType, pColor, pSize, pUnit, pStatus, pageable);
        List<ProductDTO> productDTOs = ProductConvert.convertToDTOs(products);
        this.setImageActiveAndLoadVoucherApply(productDTOs);
        this.setInfoVariantOfProduct(productDTOs);
        return new PageImpl<>(productDTOs, pageable, products.getTotalElements());
    }

    @Override
    public Page<ProductDTO> findClothes(int pageSize, int pageNum, String pTxtSearch, Integer pBrand, Integer pProductType, Integer pColor, Integer pSize, Integer pUnit, String pStatus) {
        return findAll(PID.CLOTHES, pageSize, pageNum, pTxtSearch, pBrand, pProductType, pColor, pSize, pUnit, pStatus);
    }

    @Override
    public Page<ProductDTO> findFruits(int pageSize, int pageNum, String pTxtSearch, String pStatus) {
        return findAll(PID.FRUIT, pageSize, pageNum, pTxtSearch, null, null, null, null, null, pStatus);
    }

    @Override
    public Page<ProductDTO> findSouvenirs(int pageSize, int pageNum, String pTxtSearch, Integer pColor, String pStatus) {
        return findAll(PID.SOUVENIR, pageSize, pageNum, pTxtSearch, null, null, pColor, null, null, pStatus);
    }

    @Override
    public List<Product> findProductsIdAndProductName() {
        List<Product> products = new ArrayList<>();
        for (Object[] objects : mvProductRepository.findIdAndName(ProductStatus.A.name())) {
            products.add(new Product(Integer.parseInt(String.valueOf(objects[0])), String.valueOf(objects[1])));
        }
        return products;
    }

    @Override
    public Optional<ProductDTO> findById(Integer id) {
        Optional<Product> product = mvProductRepository.findById(id);
        if (product.isPresent()) {
            ProductDescription productDescription = product.get().getProductDescription();
            return Optional.of(ProductConvert.convertToDTO(product.get(), productDescription != null ? productDescription.getDescription() : null));
        }
        return Optional.empty();
    }

    @Override
    public ProductDTO save(ProductDTO product) {
        try {
            Product productToSave = ProductConvert.convertToEntity(product);
            productToSave.setCreatedBy(CommonUtils.getUserPrincipal().getId());
            productToSave.setStatus(ProductStatus.I.name());
            Product productSaved = mvProductRepository.save(productToSave);

            ProductDescription productDescription = null;
            if (product.getDescription() != null) {
                productDescription = mvProductDescriptionRepository.save(ProductDescription.builder()
                        .product(productSaved)
                        .description(product.getDescription()).build());
            }

            systemLogService.writeLogCreate(MODULE.PRODUCT, ACTION.PRO_PRD_C, MasterObject.Product, "Thêm mới sản phẩm", product.getProductName());
            logger.info("Insert product success! {}", product);
            return ProductConvert.convertToDTO(productSaved, productDescription.getDescription());
        } catch (RuntimeException ex) {
            throw new AppException("Insert product fail!", ex);
        }
    }

    @Transactional
    @Override
    public ProductDTO update(ProductDTO productDTO, Integer productId) {
        Optional<Product> productOpt = mvProductRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new BadRequestException();
        }
        Product productBefore = ObjectUtils.clone(productOpt.get());
        productOpt.get().setId(productId);
        productOpt.get().setProductName(productDTO.getProductName());
        productOpt.get().setProductType(mvCategoryRepository.findById(productDTO.getProductTypeId()).get());
        productOpt.get().setUnit(mvCategoryRepository.findById(productDTO.getUnitId()).get());
        productOpt.get().setBrand(mvCategoryRepository.findById(productDTO.getBrandId()).get());
        productOpt.get().setStatus(productDTO.getStatus());

        ProductDescription productDescription = productOpt.get().getProductDescription();
        if (productDescription != null) {
            productDescription.setDescription(productDTO.getDescription());
        } else {
            productDescription = ProductDescription.builder()
                .product(productOpt.get())
                .description(productDTO.getDescription()).build();
        }
        ProductDescription productDescriptionUpdated = mvProductDescriptionRepository.save(productDescription);

        productOpt.get().setProductDescription(productDescriptionUpdated);
        Product productUpdated = mvProductRepository.save(productOpt.get());

        String logTitle = "Cập nhật sản phẩm: " + productUpdated.getProductName();
        ChangeLog changeLog = new ChangeLog(productBefore, productUpdated);
        mvProductHistoryService.save(changeLog.getLogChanges(), logTitle, productUpdated.getId(), null, null);
        systemLogService.writeLogUpdate(MODULE.PRODUCT, ACTION.PRO_PRD_U, MasterObject.Product, logTitle, changeLog);
        logger.info("Update product success! productId={}", productId);
        return ProductConvert.convertToDTO(productUpdated, productDescriptionUpdated.getDescription());
    }

    @Transactional
    @Override
    public String delete(Integer id) {
        try {
            Optional<ProductDTO> productToDelete = this.findById(id);
            if (productToDelete.isEmpty()) {
                throw new ResourceNotFoundException("Product not found!");
            }
            if (productInUse(id)) {
                throw new DataInUseException(ErrorCode.ERROR_DATA_LOCKED.getDescription());
            }
            mvProductRepository.deleteById(id);
            systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_PRD_D, MasterObject.Product, "Xóa sản phẩm", productToDelete.get().getProductName());
            logger.info("Delete product success! productId={}", id);
            return MessageCode.DELETE_SUCCESS.getDescription();
        } catch (RuntimeException ex) {
            throw new AppException("Delete product fail! productId=" + id, ex);
        }
    }

    @Override
    public boolean productInUse(Integer productId) throws RuntimeException {
        return !mvProductVariantService.findAll(-1, -1, productId, null, null, null, null, null).getContent().isEmpty();
    }

    private void setImageActiveAndLoadVoucherApply(List<ProductDTO> products) {
        if (products == null) {
            return;
        }
        for (ProductDTO p : products) {
            FileStorage imageActive = mvProductImageService.findImageActiveOfProduct(p.getId());
            if (imageActive != null) {
                p.setImageActive("/" + imageActive.getDirectoryPath() + "/" + imageActive.getStorageName());
            }
            List<Integer> listVoucherInfoId = new ArrayList<>();
            for (VoucherApplyDTO voucherApplyDTO : mvVoucherApplyService.findByProductId(p.getId())) {
                listVoucherInfoId.add(voucherApplyDTO.getVoucherInfoId());
            }
            if (!listVoucherInfoId.isEmpty()) {
                List<VoucherInfoDTO> voucherInfoDTOs = mvVoucherInfoService.findAll(-1, -1, listVoucherInfoId, null, null, null, VoucherStatus.A.name()).getContent();
                p.setListVoucherInfoApply(voucherInfoDTOs);
            }
        }
    }

    private void setInfoVariantOfProduct(List<ProductDTO> products) {
        if (products == null) {
            return;
        }
        for (ProductDTO p : products) {
            LinkedHashMap<String, String> variantInfo = new LinkedHashMap<>();
            int totalQtyStorage = 0;
            for (Category color : mvCategoryRepository.findColorOfProduct(p.getId())) {
                StringBuilder sizeName = new StringBuilder();
                List<Category> listSize = mvCategoryRepository.findSizeOfColorOfProduct(p.getId(), color.getId());
                for (int i = 0; i < listSize.size(); i++) {
                    int qtyStorage = mvProductStatisticsService.findProductVariantQuantityBySizeOfEachColor(p.getId(), color.getId(), listSize.get(i).getId());
                    if (i == listSize.size() - 1) {
                        sizeName.append(listSize.get(i).getName()).append(" (").append(qtyStorage).append(")");
                    } else {
                        sizeName.append(listSize.get(i).getName()).append(" (").append(qtyStorage).append(")").append(", ");
                    }
                    totalQtyStorage += qtyStorage;
                }
                variantInfo.put(color.getName(), sizeName.toString());//Đen: S (5)
            }
            p.setProductVariantInfo(variantInfo);
            p.setTotalQtyStorage(totalQtyStorage);
            p.setTotalQtySell(mvProductStatisticsService.findProductVariantTotalQtySell(p.getId()));
            int totalDefective = 0;
            p.setTotalQtyAvailableSales(totalQtyStorage - totalDefective);
        }
    }
}