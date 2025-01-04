package com.flowiee.pms.service.sales;

import com.flowiee.pms.entity.system.FileStorage;
import com.google.zxing.WriterException;

import java.io.IOException;

public interface GenerateQRCodeService {
    void generateOrderQRCode(long orderId) throws IOException, WriterException;

    void generateProductVariantQRCode(long productVariantId) throws IOException, WriterException;

    FileStorage findQRCodeOfOrder(long orderId);
}