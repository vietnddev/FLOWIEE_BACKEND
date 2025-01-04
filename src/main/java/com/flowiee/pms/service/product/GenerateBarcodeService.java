package com.flowiee.pms.service.product;

import com.flowiee.pms.entity.system.FileStorage;
import com.google.zxing.WriterException;

import java.io.IOException;

public interface GenerateBarcodeService {
    FileStorage generateBarcode(Long pProductVariantId) throws WriterException, IOException;

    FileStorage generateBarcode(Long pProductVariantId, int width, int height) throws WriterException, IOException;
}