package com.flowiee.pms.service;

import com.flowiee.pms.entity.BaseEntity;
import com.flowiee.pms.entity.product.ProductDetail;
import com.flowiee.pms.entity.sales.Order;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.FileUtils;
import com.flowiee.pms.utils.constants.FileExtension;
import com.flowiee.pms.utils.constants.MODULE;
import org.springframework.stereotype.Component;

@Component
public class BaseGenerateService extends BaseService {
    protected FileExtension mvQRCodeFormat = FileExtension.PNG;
    protected int mvQRCodeWidth = 200;
    protected int mvQRCodeHeight = 200;
    protected FileExtension mvBarcodeFormat = FileExtension.PNG;
    protected int mvBarcodeWidth = 300;
    protected int mvBarcodeHeight = 100;

    protected String getStorageName(long pCurrentTime, String pQRCodeName) {
        return pCurrentTime + "_" + pQRCodeName;
    }

    protected FileStorage getFileModel(BaseEntity baseEntity, MODULE pModule, Long pOrderId, Long pProductVariantId) {
        return FileStorage.builder()
                .module(pModule.name())
                .originalName(getImageName(baseEntity))
                .customizeName(getImageName(baseEntity))
                .storageName(getImageStorageName())
                .extension(getImageExtension())
                .directoryPath(CommonUtils.getPathDirectory(pModule).substring(CommonUtils.getPathDirectory(pModule).indexOf("uploads")))
                .account(new Account(CommonUtils.getUserPrincipal().getId()))
                .isActive(false)
                .order(pOrderId != null ? new Order(pOrderId) : null)
                .productDetail(pProductVariantId != null ? new ProductDetail(pProductVariantId) : null)
                .fileType(getCodeType())
                .build();
    }

    protected String getCodeType() {
        return null;
    }

    protected String getImageName(BaseEntity baseEntity) {
        return null;
    }

    protected String getImageStorageName() {
        return FileUtils.genRandomFileName() + "." + getImageExtension();
    }

    protected String getGenContent(BaseEntity baseEntity) {
        return null;
    }

    protected String getGenPath(MODULE pModule) {
        return CommonUtils.getPathDirectory(pModule);
    }

    protected String getImageExtension() {
        return null;
    }
}