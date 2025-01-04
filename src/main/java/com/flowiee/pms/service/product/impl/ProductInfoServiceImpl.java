package com.flowiee.pms.service.product.impl;

import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.product.*;
import com.flowiee.pms.entity.sales.Order;
import com.flowiee.pms.entity.sales.OrderDetail;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.exception.*;
import com.flowiee.pms.model.ProductHeld;
import com.flowiee.pms.repository.product.ProductDescriptionRepository;
import com.flowiee.pms.repository.sales.OrderRepository;
import com.flowiee.pms.service.category.CategoryService;
import com.flowiee.pms.utils.ChangeLog;
import com.flowiee.pms.utils.CoreUtils;
import com.flowiee.pms.utils.FileUtils;
import com.flowiee.pms.utils.constants.*;
import com.flowiee.pms.model.dto.ProductDTO;
import com.flowiee.pms.model.dto.VoucherApplyDTO;
import com.flowiee.pms.model.dto.VoucherInfoDTO;
import com.flowiee.pms.repository.category.CategoryRepository;
import com.flowiee.pms.repository.product.ProductRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.*;
import com.flowiee.pms.service.sales.VoucherApplyService;
import com.flowiee.pms.service.sales.VoucherService;
import com.flowiee.pms.utils.converter.ProductConvert;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductInfoServiceImpl extends BaseService implements ProductInfoService {
    private final ProductDescriptionRepository mvProductDescriptionRepository;
    private final ProductStatisticsService mvProductStatisticsService;
    private final ProductVariantService mvProductVariantService;
    private final ProductHistoryService mvProductHistoryService;
    private final VoucherApplyService mvVoucherApplyService;
    private final CategoryRepository mvCategoryRepository;
    private final ProductRepository mvProductRepository;
    private final VoucherService mvVoucherInfoService;
    private final OrderRepository mvOrderRepository;
    private final CategoryService mvCategoryService;
    //ProductImageService mvProductImageService;

    @Override
    public List<ProductDTO> findAll() {
        return this.findAll(null, -1, -1, null, null, null, null, null, null, null).getContent();
    }

    @Override
    public Page<ProductDTO> findAll(PID pPID , int pageSize, int pageNum, String pTxtSearch, Long pBrand, Long pProductType,
                                    Long pColor, Long pSize, Long pUnit, String pStatus) {
        Pageable pageable = getPageable(pageNum, pageSize, Sort.by("createdAt").descending());
        Page<Product> products = mvProductRepository.findAll(pPID.getId(), pTxtSearch, pBrand, pProductType, pColor, pSize, pUnit, pStatus, pageable);
        List<ProductDTO> productDTOs = ProductConvert.convertToDTOs(products);
        this.setImageActiveAndLoadVoucherApply(productDTOs);
        this.setInfoVariantOfProduct(productDTOs);
        return new PageImpl<>(productDTOs, pageable, products.getTotalElements());
    }

    @Override
    public Page<ProductDTO> findClothes(int pageSize, int pageNum, String pTxtSearch, Long pBrand, Long pProductType, Long pColor, Long pSize, Long pUnit, String pStatus) {
        return findAll(PID.CLOTHES, pageSize, pageNum, pTxtSearch, pBrand, pProductType, pColor, pSize, pUnit, pStatus);
    }

    @Override
    public Page<ProductDTO> findFruits(int pageSize, int pageNum, String pTxtSearch, String pStatus) {
        return findAll(PID.FRUIT, pageSize, pageNum, pTxtSearch, null, null, null, null, null, pStatus);
    }

    @Override
    public Page<ProductDTO> findSouvenirs(int pageSize, int pageNum, String pTxtSearch, Long pColor, String pStatus) {
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
    public ProductDTO saveClothes(ProductDTO productDTO) {
        productDTO.setPID(PID.CLOTHES.getId());
        return save(productDTO);
    }

    @Override
    public ProductDTO saveSouvenir(ProductDTO productDTO) {
        productDTO.setPID(PID.SOUVENIR.getId());
        return save(productDTO);
    }

    @Override
    public ProductDTO saveFruit(ProductDTO productDTO) {
        productDTO.setPID(PID.FRUIT.getId());
        return save(productDTO);
    }

    @Override
    public ProductDTO findById(Long id, boolean pThrowException) {
        Optional<Product> productOpt = mvProductRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            ProductDescription productDescription = product.getProductDescription();
            return ProductConvert.convertToDTO(product, productDescription != null ? productDescription.getDescription() : null);
        }
        if (pThrowException) {
            throw new EntityNotFoundException(new Object[] {"product base"}, null, null);
        } else {
            return null;
        }
    }

    @Override
    public ProductDTO save(ProductDTO product) {
        try {
            Product productToSave = ProductConvert.convertToEntity(product);

            vldCategory(productToSave.getProductType().getId(), productToSave.getBrand().getId(), productToSave.getUnit().getId());
            if (CoreUtils.isNullStr(productToSave.getProductName()))
                throw new BadRequestException("Product name is not null!");

            //productToSave.setCreatedBy(CommonUtils.getUserPrincipal().getId());
            productToSave.setStatus(ProductStatus.I.name());
            Product productSaved = mvProductRepository.save(productToSave);

            ProductDescription productDescription = null;
            if (ObjectUtils.isNotEmpty(product.getDescription())) {
                productDescription = mvProductDescriptionRepository.save(ProductDescription.builder()
                        .product(productSaved)
                        .description(product.getDescription()).build());
            }

            systemLogService.writeLogCreate(MODULE.PRODUCT, ACTION.PRO_PRD_C, MasterObject.Product, "Thêm mới sản phẩm", product.getProductName());
            logger.info("Insert product success! {}", product);
            return ProductConvert.convertToDTO(productSaved, productDescription != null ? productDescription.getDescription() : null);
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.CREATE_ERROR_OCCURRED.getDescription(), "product"), ex);
        }
    }

    @Transactional
    @Override
    public ProductDTO update(ProductDTO productDTO, Long productId) {
        Optional<Product> productOpt = mvProductRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_NOT_FOUND, new Object[]{"product"}, null, getClass(), null);
        }

        Long lvProductTypeId = productDTO.getProductTypeId();
        Long lvBrandId = productDTO.getBrandId();
        Long lvUnitId = productDTO.getUnitId();

        VldModel vldModel = vldCategory(lvProductTypeId, lvBrandId, lvUnitId);
        Category lvProductType = vldModel.getProductType();
        Category lvBrand = vldModel.getBrand();
        Category lvUnit = vldModel.getUnit();

        Product lvProduct = productOpt.get();
        Product productBefore = ObjectUtils.clone(productOpt.get());

        //product.setId(productId);
        lvProduct.setProductName(productDTO.getProductName());
        lvProduct.setProductType(lvProductType);
        lvProduct.setBrand(lvBrand);
        lvProduct.setUnit(lvUnit);
        lvProduct.setStatus(productDTO.getStatus());

        ProductDescription productDescription = lvProduct.getProductDescription();
        if (productDescription != null) {
            productDescription.setDescription(productDTO.getDescription());
        } else {
            productDescription = ProductDescription.builder()
                .product(lvProduct)
                .description(productDTO.getDescription()).build();
        }
        ProductDescription productDescriptionUpdated = mvProductDescriptionRepository.save(productDescription);

        lvProduct.setProductDescription(productDescriptionUpdated);
        Product productUpdated = mvProductRepository.save(lvProduct);

        String logTitle = "Cập nhật sản phẩm: " + productUpdated.getProductName();
        ChangeLog changeLog = new ChangeLog(productBefore, productUpdated);
        mvProductHistoryService.save(changeLog.getLogChanges(), logTitle, productUpdated.getId(), null, null);
        systemLogService.writeLogUpdate(MODULE.PRODUCT, ACTION.PRO_PRD_U, MasterObject.Product, logTitle, changeLog);
        logger.info("Update product success! productId={}", productId);
        return ProductConvert.convertToDTO(productUpdated, productDescriptionUpdated.getDescription());
    }

    @Transactional
    @Override
    public String delete(Long id) {
        try {
            ProductDTO productToDelete = this.findById(id, true);
            if (productInUse(productToDelete.getId())) {
                throw new DataInUseException(ErrorCode.ERROR_DATA_LOCKED.getDescription());
            }
            mvProductRepository.deleteById(productToDelete.getId());
            systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_PRD_D, MasterObject.Product, "Xóa sản phẩm", productToDelete.getProductName());
            logger.info("Delete product success! productId={}", id);
            return MessageCode.DELETE_SUCCESS.getDescription();
        } catch (RuntimeException ex) {
            throw new AppException("Delete product fail! productId=" + id, ex);
        }
    }

    @Override
    public boolean productInUse(Long productId) throws RuntimeException {
        return !mvProductVariantService.findAll(-1, -1, productId, null, null, null, null, null, false).getContent().isEmpty();
    }

    @Override
    public List<ProductHeld> getProductHeldInUnfulfilledOrder() {
        List<ProductHeld> productHeldList = new ArrayList<>();
        List<Order> orderPage = mvOrderRepository.findByOrderStatus(List.of(OrderStatus.PROC, OrderStatus.DLVD));
        if (orderPage.isEmpty()) {
            return productHeldList;
        }
        Map<Long, ProductHeld> productHeldMap = new HashMap<>();
        for (Order ord : orderPage) {
            for (OrderDetail ordDetail : ord.getListOrderDetail()) {
                ProductDetail lvProductVariant = ordDetail.getProductDetail();
                Long lvProductVariantId = lvProductVariant.getId();

                ProductHeld productHeldExisted = productHeldMap.get(lvProductVariantId);
                if (productHeldExisted != null) {
                    int currentQuantity = productHeldExisted.getQuantity();
                    productHeldExisted.setQuantity(currentQuantity + ordDetail.getQuantity());
                } else {
                    ProductHeld productHeld = ProductHeld.builder()
                            .productVariantId(lvProductVariantId)
                            .productName(lvProductVariant.getVariantName())
                            .orderCode(ord.getCode())
                            .quantity(ordDetail.getQuantity())
                            .orderStatus(ord.getOrderStatus())
                            .build();
                    productHeldList.add(productHeld);
                    productHeldMap.put(lvProductVariantId, productHeld);
                }
            }
        }
        return productHeldList;
    }

    @Override
    public List<ProductDTO> getDiscontinuedProducts() {
        return findAll(null, -1, -1, null, null, null, null, null, null, ProductStatus.I.name()).getContent();
    }

    private void setImageActiveAndLoadVoucherApply(List<ProductDTO> products) {
        if (products == null) {
            return;
        }
        for (ProductDTO p : products) {
            FileStorage imageActive = p.getImage();//mvProductImageService.findImageActiveOfProduct(p.getId());
            if (imageActive != null) {
                p.setImageActive(FileUtils.getImageUrl(imageActive, true));
            }
            List<Long> listVoucherInfoId = new ArrayList<>();
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
            int totalDefective = 0;
            int totalQtySell = mvProductStatisticsService.findProductVariantTotalQtySell(p.getId());

            for (Category color : mvCategoryRepository.findColorOfProduct(p.getId())) {
                StringBuilder sizeName = new StringBuilder();
                List<Category> listSize = mvCategoryRepository.findSizeOfColorOfProduct(p.getId(), color.getId());
                int sizeLength = listSize.size();
                for (int i = 0; i < sizeLength; i++) {
                    Category categorySize = listSize.get(i);
                    int qtyStorage = mvProductStatisticsService.findProductVariantQuantityBySizeOfEachColor(p.getId(), color.getId(), categorySize.getId());
                    if (i == sizeLength - 1) {
                        sizeName.append(categorySize.getName()).append(" (").append(qtyStorage).append(")");
                    } else {
                        sizeName.append(categorySize.getName()).append(" (").append(qtyStorage).append(")").append(", ");
                    }
                    totalQtyStorage += qtyStorage;
                }
                variantInfo.put(color.getName(), sizeName.toString());//Đen: S (5)
            }
            p.setProductVariantInfo(variantInfo);
            p.setTotalQtyStorage(totalQtyStorage);
            p.setTotalQtySell(totalQtySell);
            p.setTotalQtyAvailableSales(totalQtyStorage - totalDefective);
        }
    }

    private VldModel vldCategory(Long pProductTypeId, Long pBrandId, Long pUnitId) {
        Category lvProductType = mvCategoryService.findById(pProductTypeId, false);
        if (lvProductType == null)
            throw new BadRequestException("Product type invalid!");

        Category lvBrand = mvCategoryService.findById(pBrandId, false);
        if (lvBrand == null)
            throw new BadRequestException("Brand invalid!");

        Category lvUnit = mvCategoryService.findById(pUnitId, false);
        if (lvUnit == null)
            throw new BadRequestException("Unit invalid!");

        VldModel vldModel = new VldModel();
        vldModel.setProductType(lvProductType);
        vldModel.setBrand(lvBrand);
        vldModel.setUnit(lvUnit);

        return vldModel;
    }
}