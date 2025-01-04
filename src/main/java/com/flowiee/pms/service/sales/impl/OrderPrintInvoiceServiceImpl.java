package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.config.Core;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.model.OrderDetailRpt;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.model.dto.OrderDetailDTO;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.sales.OrderPrintInvoiceService;
import com.flowiee.pms.service.sales.GenerateQRCodeService;
import com.flowiee.pms.service.sales.OrderService;
import com.flowiee.pms.utils.FileUtils;
import com.flowiee.pms.utils.ReportUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderPrintInvoiceServiceImpl extends BaseService implements OrderPrintInvoiceService {
    OrderService       mvOrderService;
    GenerateQRCodeService mvGenerateQRCodeService;

    @Override
    public void printInvoicePDF(Long pOrderId, List<Integer> pOrderIds, boolean isExportAll, HttpServletResponse response) {
        Optional<OrderDTO> dtoOptional = mvOrderService.findById(pOrderId);
        if (dtoOptional.isEmpty()) {
            throw new BadRequestException();
        }
        OrderDTO dto = dtoOptional.get();

        boolean checkBatch = false;

        //ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        //PDFMergerUtility mergePdf = new PDFMergerUtility();
        //Barcode_Image.createImage(order.getId().toString() + ".png", order.getId().toString());
        HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("customerName", dto.getReceiverName());
        parameterMap.put("customerAddress", dto.getReceiverAddress());
        parameterMap.put("customerPhone", dto.getReceiverPhone());
        parameterMap.put("customerEmail", dto.getReceiverEmail());
        parameterMap.put("totalSubtotal","$ 0");
        parameterMap.put("totalShippingCost","$ 0");
        parameterMap.put("discount","$ " + dto.getAmountDiscount());
        parameterMap.put("totalPayment", dto.getTotalAmountDiscount());
        parameterMap.put("paymentMethod", dto.getPayMethodName());
        parameterMap.put("invoiceNumber", dto.getCode());
        parameterMap.put("orderDate", dto.getOrderTime());
        parameterMap.put("nowDate", new Date());
        FileStorage f = mvGenerateQRCodeService.findQRCodeOfOrder(dto.getId());
        if (f != null) {
            Path barcodePath = Path.of(Core.getResourceUploadPath() + FileUtils.getImageUrl(f, true));
            if (barcodePath.toFile().exists()) {
                parameterMap.put("barcode", barcodePath);
            }
        } else {
            parameterMap.put("barcode", null);
        }
        parameterMap.put("logoPath", FileUtils.logoPath);

        // orderDetails
        List<OrderDetailRpt> listDetail = new ArrayList<>();
        for (OrderDetailDTO detailDTO : dto.getListOrderDetailDTO()) {
            BigDecimal lvUnitPrice = detailDTO.getPrice();
            int lvQuantity = detailDTO.getQuantity();
            listDetail.add(OrderDetailRpt.builder()
                    .productName(detailDTO.getProductVariantDTO().getVariantName())
                    .unitPrice(lvUnitPrice)
                    .quantity(lvQuantity)
                    .subTotal(lvUnitPrice.multiply(BigDecimal.valueOf(lvQuantity)))
                    .note(detailDTO.getNote())
                    .build());
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ServletOutputStream servletOutputStream = response.getOutputStream();
            try {
//				JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.default.pdf.font.name", "false");
//				JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.default.pdf.encoding", "UTF-8");
//				JRPropertiesUtil.getInstance(context).setProperty("net.sf.jasperreports.default.pdf.embedded", "true");
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", " inline; filename=deliveryNote" + dto.getId() + ".pdf");
                InputStream reportStream = new FileInputStream(ReportUtils.getReportTemplate("Invoice"));
                JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameterMap, new JRBeanCollectionDataSource(listDetail));
                JasperExportManager.exportReportToPdfStream(jasperPrint, (checkBatch) ? byteArrayOutputStream : servletOutputStream);
                if (checkBatch) {
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                } else {
                    servletOutputStream.flush();
                    servletOutputStream.close();
                }
            } catch (Exception e) {
                // display stack trace in the browser
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                response.setContentType("text/plain");
                response.getOutputStream().print(stringWriter.toString());
                throw new AppException(e);
            }
        } catch (Exception e) {
            throw new AppException("Exception print PDF: " + e.getMessage());
        }
    }
}