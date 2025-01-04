package com.flowiee.pms.service.product.impl;

import com.flowiee.pms.config.Core;
import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.product.ProductDetail;
import com.flowiee.pms.entity.product.ProductPrice;
import com.flowiee.pms.entity.product.ProductVariantExim;
import com.flowiee.pms.entity.sales.OrderCart;
import com.flowiee.pms.entity.sales.TicketExport;
import com.flowiee.pms.entity.sales.TicketImport;
import com.flowiee.pms.entity.storage.Storage;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.entity.system.SystemConfig;
import com.flowiee.pms.exception.*;
import com.flowiee.pms.model.dto.ProductPriceDTO;
import com.flowiee.pms.repository.product.ProductPriceRepository;
import com.flowiee.pms.repository.sales.OrderCartRepository;
import com.flowiee.pms.repository.system.FileStorageRepository;
import com.flowiee.pms.service.category.CategoryService;
import com.flowiee.pms.service.sales.CartService;
import com.flowiee.pms.service.sales.GenerateQRCodeService;
import com.flowiee.pms.service.storage.StorageService;
import com.flowiee.pms.utils.ChangeLog;
import com.flowiee.pms.utils.CoreUtils;
import com.flowiee.pms.utils.FileUtils;
import com.flowiee.pms.utils.constants.ACTION;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.model.dto.ProductVariantDTO;
import com.flowiee.pms.model.dto.ProductVariantTempDTO;
import com.flowiee.pms.repository.product.ProductDetailRepository;
import com.flowiee.pms.repository.product.ProductDetailTempRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.ProductHistoryService;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.service.sales.TicketExportService;
import com.flowiee.pms.service.sales.TicketImportService;
import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.constants.*;
import com.flowiee.pms.utils.converter.ProductVariantConvert;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl extends BaseService implements ProductVariantService {
    private final ProductDetailTempRepository mvProductVariantTempRepository;
    private final ProductDetailRepository mvProductVariantRepository;
    private final ProductPriceRepository mvProductPriceRepository;
    private final FileStorageRepository mvFileStorageRepository;
    private final GenerateQRCodeService mvGenerateQRCodeService;
    private final ProductHistoryService mvProductHistoryService;
    private final TicketImportService mvTicketImportService;
    private final TicketExportService mvTicketExportService;
    private final CategoryService mvCategoryService;
    private final StorageService mvStorageService;
    private final OrderCartRepository mvCartRepository;
    @Autowired
    @Lazy
    private CartService mvCartService;

    @Override
    public List<ProductVariantDTO> findAll() {
        return this.findAll(-1, -1, null, null, null, null, null, null, false).getContent();
    }

    @Override
    public Page<ProductVariantDTO> findAll(int pageSize, int pageNum, Long pProductId, Long pTicketImport, Long pColor, Long pSize, Long pFabricType, Boolean pAvailableForSales, boolean checkInAnyCart) {
        Pageable pageable = getPageable(pageNum, pageSize, Sort.by("variantName").ascending());
        Page<ProductDetail> productVariantPage = mvProductVariantRepository.findAll(pProductId, pColor, pSize, pFabricType, pAvailableForSales, pageable);

        List<ProductVariantDTO> pDTOs = productVariantPage.getContent().stream()
        	    .map(ProductVariantConvert::entityToDTO)
        	    .peek(dto -> {
        	        setPriceInfo(dto, dto.getVariantPrice());
        	        setImageSrc(dto);
                    OrderCart currentCart = getCurrentCart(checkInAnyCart);
                    if (currentCart != null) {
                        dto.setCurrentInCart(mvCartService.isItemExistsInCart(currentCart.getId(), dto.getId()));
                    }
        	    })
        	    .collect(Collectors.toList());
        
        return new PageImpl<>(pDTOs, pageable, productVariantPage.getTotalElements());
    }

    private OrderCart getCurrentCart(boolean checkInAnyCart) {
        if (checkInAnyCart) {
            List<OrderCart> cartList = mvCartRepository.findByAccountId(CommonUtils.getUserPrincipal().getId());
            if (ObjectUtils.isNotEmpty(cartList)) {
                return cartList.get(0);
            }
        }
        return null;
    }

    @Override
    public ProductVariantDTO findById(Long pProductVariantId, boolean pThrowException) {
        Optional<ProductDetail> productVariant = mvProductVariantRepository.findById(pProductVariantId);
        if (productVariant.isPresent()) {
            ProductVariantDTO dto = ProductVariantConvert.entityToDTO(productVariant.get());
            //ProductPrice productPrice = mvProductPriceRepository.findPricePresent(null, dto.getId());
            setPriceInfo(dto, dto.getVariantPrice());
            return dto;
        }
        if (pThrowException) {
            throw new EntityNotFoundException(new Object[] {"product variant"}, null, null);
        } else {
            return null;
        }
    }

    @Override
    public ProductVariantDTO save(ProductVariantDTO inputDTO) {
        try {
            VldModel vldModel = vldCategory(inputDTO.getColorId(), inputDTO.getSizeId(), inputDTO.getFabricTypeId());

            ProductDetail pVariant = ProductVariantConvert.dtoToEntity(inputDTO);
            pVariant.setColor(vldModel.getColor());
            pVariant.setSize(vldModel.getSize());
            pVariant.setFabricType(vldModel.getFabricType());
            pVariant.setSoldQty(CoreUtils.coalesce(pVariant.getSoldQty()));
            pVariant.setStorageQty(CoreUtils.coalesce(pVariant.getStorageQty()));
            pVariant.setDefectiveQty(CoreUtils.coalesce(pVariant.getDefectiveQty()));
            pVariant.setStatus(ProductStatus.A.name());
            pVariant.setVariantCode(genProductCode(inputDTO.getVariantCode()));
            pVariant.setSku(generateSKUCode());
            ProductDetail productDetailSaved = mvProductVariantRepository.save(pVariant);

            ProductPriceDTO priceDTO = inputDTO.getPrice();
            savePrice(productDetailSaved, priceDTO);

            try {
                mvGenerateQRCodeService.generateProductVariantQRCode(productDetailSaved.getId());
            } catch (IOException | WriterException e ) {
                e.printStackTrace();
                logger.error(String.format("Can't generate QR Code for Product %s", productDetailSaved.getVariantCode()), e);
            }

            if (productDetailSaved.getStorageQty() > 0) {
                Storage lvStorage = mvStorageService.findById(inputDTO.getStorageIdInitStorageQty(), true);
                String initMessage = "Initialize storage quantity when create new products";

                TicketImport ticketImportSaved = mvTicketImportService.save(TicketImport.builder()
                        .title("Initialize storage")
                        .importer(CommonUtils.getUserPrincipal().getUsername())
                        .importTime(LocalDateTime.now())
                        .note(initMessage)
                        .status(TicketImportStatus.COMPLETED.name())
                        .storage(lvStorage)
                        .build());

                mvProductVariantTempRepository.save(ProductVariantExim.builder()
                        .ticketImport(ticketImportSaved)
                        .productVariant(productDetailSaved)
                        .quantity(productDetailSaved.getStorageQty())
                        .note(initMessage)
                        .build());
            }
            if (productDetailSaved.getSoldQty() > 0) {
                Storage lvStorage = mvStorageService.findById(inputDTO.getStorageIdInitStorageQty(), true);
                String initMessage = "Initialize storage quantity when create new products";

                TicketExport ticketExportSaved = mvTicketExportService.save(TicketExport.builder()
                        .title("Initialize storage")
                        .exporter(CommonUtils.getUserPrincipal().getUsername())
                        .exportTime(LocalDateTime.now())
                        .note(initMessage)
                        .status(TicketExportStatus.COMPLETED.name())
                        .storage(lvStorage)
                        .build());

                mvProductVariantTempRepository.save(ProductVariantExim.builder()
                        .ticketExport(ticketExportSaved)
                        .productVariant(productDetailSaved)
                        .quantity(productDetailSaved.getStorageQty())
                        .note(initMessage)
                        .build());
            }

            systemLogService.writeLogCreate(MODULE.PRODUCT, ACTION.PRO_PRD_U, MasterObject.ProductVariant, "Thêm mới biến thể sản phẩm", pVariant.toStringInsert());
            logger.info("Insert productVariant success! {}", pVariant);
            return ProductVariantConvert.entityToDTO(productDetailSaved);
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.CREATE_ERROR_OCCURRED.getDescription(), "product variant"), ex);
        }
    }

    @Transactional
    @Override
    public ProductVariantDTO update(ProductVariantDTO pProductDetail, Long productVariantId) {
        ProductDetail productVariantOptional = this.findById(productVariantId, true);
        //VldModel vldModel = vldCategory(pProductDetail.getColorId(), pProductDetail.getSizeId(), pProductDetail.getFabricTypeId());
        try {
            //Product variant info
            ProductDetail productBeforeUpdate = ObjectUtils.clone(productVariantOptional);

            ProductDetail productToUpdate = productVariantOptional;
            productToUpdate.setVariantName(pProductDetail.getVariantName());
            productToUpdate.setDefectiveQty(pProductDetail.getDefectiveQty());
            productToUpdate.setWeight(pProductDetail.getWeight());
            productToUpdate.setNote(pProductDetail.getNote());
            ProductDetail productVariantUpdated = mvProductVariantRepository.save(productToUpdate);

            //Update state of current Price to inactive
            ProductPrice productVariantPricePresent = mvProductPriceRepository.findPricePresent(null, productVariantUpdated.getId());
            if (productVariantPricePresent != null) {
                productVariantPricePresent.setState(ProductPrice.STATE_INACTIVE);
                mvProductPriceRepository.save(productVariantPricePresent);
            }
            ProductPriceDTO price = pProductDetail.getPrice();
            savePrice(productVariantUpdated, price);

            //Log
            String logTitle = "Cập nhật thông tin sản phẩm: " + productVariantUpdated.getVariantName();
            ChangeLog changeLog = new ChangeLog(productBeforeUpdate, productVariantUpdated);
            mvProductHistoryService.save(changeLog.getLogChanges(), logTitle, productVariantUpdated.getProduct().getId(), productVariantUpdated.getId(), null);
            systemLogService.writeLogUpdate(MODULE.PRODUCT, ACTION.PRO_PRD_U, MasterObject.ProductVariant, logTitle, changeLog.getOldValues(), changeLog.getNewValues());
            logger.info("Update productVariant success! {}", productVariantUpdated);

            return ProductVariantConvert.entityToDTO(productVariantUpdated);
        } catch (Exception e) {
            throw new AppException("Update productVariant fail! " + pProductDetail.toString(), e);
        }
    }

    @Override
    public String delete(Long productVariantId) {
        ProductVariantDTO productDetailToDelete = this.findById(productVariantId, true);
        try {
            mvProductVariantRepository.deleteById(productVariantId);
        } catch (ConstraintViolationException ex) {
            throw new DataInUseException("Không thể xóa sản phẩm đã được sử dụng!", ex);
        }
        systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_PRD_U, MasterObject.ProductVariant, "Xóa biến thể sản phẩm", productDetailToDelete.getVariantName());
        logger.info("Delete productVariant success! {}", productDetailToDelete);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }

    @Override
    public boolean isProductVariantExists(long productId, long colorId, long sizeId, long fabricTypeId) {
        ProductDetail productDetail = mvProductVariantRepository.findByColorAndSize(productId, colorId, sizeId, fabricTypeId);
        return ObjectUtils.isNotEmpty(productDetail);
    }

    @Override
    public List<ProductVariantTempDTO> findStorageHistory(Long productVariantId) {
        List<ProductVariantExim> storageHistory = mvProductVariantTempRepository.findByProductVariantId(productVariantId);
        List<ProductVariantTempDTO> storageHistoryDTOs = ProductVariantTempDTO.convertToDTOs(storageHistory);
        if (ObjectUtils.isEmpty(storageHistoryDTOs)) {
            return List.of();
        }
        for (ProductVariantTempDTO tempDTO : storageHistoryDTOs) {
            String staff = "";
            String actionLabel = "";
            String changeQty = "";
            String branchName = "";
            if (tempDTO.getQuantity() > 0) {
                changeQty = "+ " + tempDTO.getQuantity();
            }
            if (tempDTO.getTicketImport() != null) {
                staff = tempDTO.getTicketImport().getImporter();
                for (TicketImportAction importAction : TicketImportAction.values()) {
                    if (importAction.name().equals(tempDTO.getAction()) ) {
                        actionLabel = importAction.getLabel();
                        break;
                    }
                }
            }
            if (tempDTO.getTicketExport() != null) {
                staff = tempDTO.getTicketExport().getExporter();
                for (TicketExportAction exportAction : TicketExportAction.values()) {
                    if (exportAction.name().equals(tempDTO.getAction()) ) {
                        actionLabel = exportAction.getLabel();
                        break;
                    }
                }
            }
            tempDTO.setStaff(staff);
            tempDTO.setAction(actionLabel);
            tempDTO.setChangeQty(changeQty);
            tempDTO.setStorageQty(tempDTO.getStorageQty());
            tempDTO.setBranchName(branchName);
        }
        return storageHistoryDTOs;
    }

    @Override
    public void updateLowStockThreshold(Long pProductId, int pThreshold) {
        ProductDetail lvProduct = mvProductVariantRepository.findById(pProductId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        lvProduct.setLowStockThreshold(pThreshold);
        mvProductVariantRepository.save(lvProduct);
    }

    @Override
    public Page<ProductVariantDTO> getProductsOutOfStock(int pageSize, int pageNum) {
        Pageable pageable = getPageable(pageNum, pageSize);
        Page<ProductDetail> productVariants = mvProductVariantRepository.findProductsOutOfStock(pageable);
        List<ProductVariantDTO> productVariantDTOs = ProductVariantConvert.entitiesToDTOs(productVariants.getContent());
        for (ProductVariantDTO dto : productVariantDTOs) {
            setPriceInfo(dto, dto.getVariantPrice());
        }
        return new PageImpl<>(productVariantDTOs, pageable, productVariantDTOs.size());
    }

    private ProductVariantDTO setPriceInfo(ProductVariantDTO dto, ProductPrice productPrice) {
        if (dto != null) {
            if (productPrice != null) {
                dto.setPrice(ProductPriceDTO.builder()
                        .retailPrice(productPrice.getRetailPrice())
                        .retailPriceDiscount(productPrice.getRetailPriceDiscount())
                        .wholesalePrice(productPrice.getWholesalePrice())
                        .wholesalePriceDiscount(productPrice.getWholesalePriceDiscount())
                        .purchasePrice(productPrice.getPurchasePrice())
                        .costPrice(productPrice.getCostPrice())
                        .lastUpdated(productPrice.getLastUpdatedAt())
                        .build());
            } else {
                dto.setPrice(new ProductPriceDTO());
            }
        }
        return dto;
    }

    private ProductVariantDTO setImageSrc(ProductVariantDTO productVariantInfo) {
        if (productVariantInfo == null) {
            return null;
        }
        FileStorage imageModel = mvFileStorageRepository.findActiveImage(null, productVariantInfo.getId());
        if (imageModel == null) {
            return productVariantInfo;
        }
        productVariantInfo.setImageSrc(FileUtils.getImageUrl(imageModel, true));
        return productVariantInfo;
    }

    private String genProductCode(String defaultCode) {
        if (CoreUtils.isNullStr(defaultCode))
        {
            return CommonUtils.now("yyyyMMddHHmmss");
        }
        return defaultCode;
    }

    private VldModel vldCategory(Long pColorId, Long pSizeId, Long pFabricTypeId) {
        Category lvColor = mvCategoryService.findById(pColorId, false);
        if (lvColor == null)
            throw new BadRequestException("Color invalid!");

        Category lvSize = mvCategoryService.findById(pSizeId, false);
        if (lvSize == null)
            throw new BadRequestException("Size invalid!");

        Category lvFabricType = mvCategoryService.findById(pFabricTypeId, false);
        if (lvFabricType == null)
            throw new BadRequestException("Fabric type invalid!");

        VldModel vldModel = new VldModel();
        vldModel.setColor(lvColor);
        vldModel.setSize(lvSize);
        vldModel.setFabricType(lvFabricType);

        return vldModel;
    }

    private void vldPrice(BigDecimal lvRetailPrice, BigDecimal lvRetailPriceDiscount,
                          BigDecimal lvWholesalePrice, BigDecimal lvWholesalePriceDiscount,
                          BigDecimal lvPurchasePrice, BigDecimal lvCostPrice)
    {
        if (lvRetailPrice                   == null || lvRetailPrice.doubleValue()            < 0
                || lvRetailPriceDiscount    == null || lvRetailPriceDiscount.doubleValue()    < 0
                || lvWholesalePrice         == null || lvWholesalePrice.doubleValue()         < 0
                || lvWholesalePriceDiscount == null || lvWholesalePriceDiscount.doubleValue() < 0
                || lvPurchasePrice          == null || lvPurchasePrice.doubleValue()          < 0
                || lvCostPrice              == null || lvCostPrice.doubleValue()              < 0)
        {
            throw new BadRequestException("Price must greater than zero!");
        }
        SystemConfig lvConfig = Core.mvSystemConfigList.get(ConfigCode.allowSellPriceLessThanCostPrice);
        if (isConfigAvailable(lvConfig) && !lvConfig.isYesOption())
        {
            double sellingPrice = Math.min(lvRetailPriceDiscount.doubleValue(), lvWholesalePriceDiscount.doubleValue());
            double costPrice = Math.min(lvPurchasePrice.doubleValue(), lvCostPrice.doubleValue());
            if (sellingPrice < costPrice)
            {
                throw new BadRequestException("Selling price must greater than cost price!");
            }
        }
    }

    private void savePrice(ProductDetail productVariant, ProductPriceDTO pPriceDTO) {
        BigDecimal lvRetailPrice = CoreUtils.coalesce(pPriceDTO.getRetailPrice());
        BigDecimal lvRetailPriceDiscount = CoreUtils.coalesce(pPriceDTO.getRetailPriceDiscount(), pPriceDTO.getRetailPrice());
        BigDecimal lvWholesalePrice = CoreUtils.coalesce(pPriceDTO.getWholesalePrice());
        BigDecimal lvWholesalePriceDiscount = CoreUtils.coalesce(pPriceDTO.getWholesalePriceDiscount(), pPriceDTO.getWholesalePrice());
        BigDecimal lvPurchasePrice = CoreUtils.coalesce(pPriceDTO.getPurchasePrice());
        BigDecimal lvCostPrice = CoreUtils.coalesce(pPriceDTO.getCostPrice());

        vldPrice(lvRetailPrice, lvRetailPriceDiscount, lvWholesalePrice, lvWholesalePriceDiscount, lvPurchasePrice, lvCostPrice);

        mvProductPriceRepository.save(ProductPrice.builder()
                .productVariant(productVariant)
                .retailPrice(lvRetailPrice)
                .retailPriceDiscount(lvRetailPriceDiscount)
                .wholesalePrice(lvWholesalePrice)
                .wholesalePriceDiscount(lvWholesalePriceDiscount)
                .purchasePrice(lvPurchasePrice)
                .costPrice(lvCostPrice)
                .state(ProductPrice.STATE_ACTIVE)
                .build());
    }

    private String generateSKUCode() {
        //Do something pattern
        return UUID.randomUUID().toString();
    }
}