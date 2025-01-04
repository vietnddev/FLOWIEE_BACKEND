package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.BaseEntity;
import com.flowiee.pms.entity.product.ProductDetail;
import com.flowiee.pms.entity.sales.Order;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.exception.EntityNotFoundException;
import com.flowiee.pms.repository.product.ProductDetailRepository;
import com.flowiee.pms.repository.sales.OrderRepository;
import com.flowiee.pms.service.BaseGenerateService;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.repository.system.FileStorageRepository;
import com.flowiee.pms.service.sales.GenerateQRCodeService;
import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.constants.FileExtension;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class GenerateQRCodeServiceImpl extends BaseGenerateService implements GenerateQRCodeService {
    private final ProductDetailRepository mvProductVariantRepository;
    private final FileStorageRepository mvFileStorageRepository;
    private final OrderRepository mvOrderRepository;

    private void generateQRCode(String pContent, FileExtension pFormat, Path pPath) throws WriterException, IOException {
        generateQRCode(pContent, pFormat, mvQRCodeWidth, mvQRCodeHeight, pPath);
    }

    private void generateQRCode(String pContent, FileExtension pFormat, int pWidth, int pHeight, Path pPath) throws WriterException, IOException {
        QRCodeWriter lvQrCodeWriter = new QRCodeWriter();
        BitMatrix lvBitMatrix = lvQrCodeWriter.encode(pContent, BarcodeFormat.QR_CODE, pWidth, pHeight);
        MatrixToImageWriter.writeToPath(lvBitMatrix, pFormat.name(), pPath);
    }

    @Override
    public void generateOrderQRCode(long orderId) throws IOException, WriterException {
        Order lvOrder = mvOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(new Object[] {"order"}, null, null));

        FileStorage lvQRCodeModel = getFileModel(lvOrder, MODULE.SALES, orderId, null);
        mvFileStorageRepository.save(lvQRCodeModel);

        Path lvGenPath = Paths.get(super.getGenPath(MODULE.SALES) + "/" + lvQRCodeModel.getStorageName());
        generateQRCode(getGenContent(lvOrder), mvQRCodeFormat, lvGenPath);
    }

    @Override
    public void generateProductVariantQRCode(long productVariantId) throws IOException, WriterException {
        ProductDetail lvProduct = mvProductVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new EntityNotFoundException(new Object[] {"product variant"}, null, null));

        FileStorage lvQRCodeModel = getFileModel(lvProduct, MODULE.PRODUCT, null, productVariantId);
        FileStorage lvQRCodeSaved = mvFileStorageRepository.save(lvQRCodeModel);

        Path lvGenPath = Paths.get(super.getGenPath(MODULE.PRODUCT) + "/" + lvQRCodeSaved.getStorageName());
        generateQRCode(getGenContent(lvProduct), mvQRCodeFormat, lvGenPath);
    }

    @Override
    public FileStorage findQRCodeOfOrder(long orderId) {
        return mvFileStorageRepository.findQRCodeOfOrder(orderId);
    }

    @Override
    protected String getCodeType() {
        return FileStorage.QRCODE;
    }

    @Override
    protected String getImageName(BaseEntity baseEntity) {
        return "qrcode_order_" + baseEntity.getId() + "." + mvBarcodeFormat.getKey();
    }

    @Override
    protected String getGenContent(BaseEntity baseEntity) {
        return CommonUtils.getServerURL() + "/product/" + baseEntity.getId();
    }

    @Override
    protected String getImageExtension() {
        return mvQRCodeFormat.getKey();
    }
}