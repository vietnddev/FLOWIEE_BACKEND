package com.flowiee.pms.controller.sales;

import com.flowiee.pms.controller.BaseController;
import com.flowiee.pms.exception.ResourceNotFoundException;
import com.flowiee.pms.model.AppResponse;
import com.flowiee.pms.model.EximModel;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.model.payload.CreateOrderReq;
import com.flowiee.pms.service.ExportService;
import com.flowiee.pms.service.sales.OrderPayService;
import com.flowiee.pms.service.sales.OrderService;
import com.flowiee.pms.utils.constants.ErrorCode;
import com.flowiee.pms.utils.constants.OrderStatus;
import com.flowiee.pms.utils.constants.TemplateExport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app.api.prefix}/order")
@Tag(name = "Order API", description = "Quản lý đơn hàng")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderController extends BaseController {
    OrderService  mvOrderService;
    @Autowired
    @NonFinal
    @Qualifier("orderExportServiceImpl")
    ExportService mvExportService;
    OrderPayService mvOrderPayService;

    @Operation(summary = "Find all orders")
    @GetMapping("/all")
    @PreAuthorize("@vldModuleSales.readOrder(true)")
    public AppResponse<List<OrderDTO>> findAllOrders(@RequestParam(value = "orderId", required = false) Long pOrderId,
                                                     @RequestParam(value = "paymentMethodId", required = false) Long pPaymentMethodId,
                                                     @RequestParam(value = "orderStatusId", required = false) OrderStatus pOrderStatus,
                                                     @RequestParam(value = "salesChannelId", required = false) Long pSalesChannelId,
                                                     @RequestParam(value = "groupCustomerId", required = false) Long pGroupCustomerId,
                                                     @RequestParam(value = "sellerId", required = false) Long pSellerId,
                                                     @RequestParam(value = "customerId", required = false) Long pCustomerId,
                                                     @RequestParam(value = "branchId", required = false) Long pBranchId,
                                                     @RequestParam(value = "dateFilter", required = false) String pDateFilter,
                                                     @RequestParam(value = "txtSearch", required = false) String pTxtSearch,
                                                     @RequestParam("pageSize") int pageSize,
                                                     @RequestParam("pageNum") int pageNum) {
        try {
            Page<OrderDTO> orderPage = mvOrderService.findAll(pageSize, pageNum - 1, pTxtSearch, pOrderId, pPaymentMethodId, pOrderStatus, pSalesChannelId, pSellerId, pCustomerId, pBranchId, pGroupCustomerId, pDateFilter, null, null, null);
            return success(orderPage.getContent(), pageNum, pageSize, orderPage.getTotalPages(), orderPage.getTotalElements());
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR_OCCURRED.getDescription(), "order"), ex);
        }
    }

    @Operation(summary = "Find detail order")
    @GetMapping("/{orderId}")
    @PreAuthorize("@vldModuleSales.readOrder(true)")
    public AppResponse<OrderDTO> findOrderDetail(@PathVariable("orderId") Long orderId) {
        Optional<OrderDTO> orderDTO = mvOrderService.findById(orderId);
        if (orderDTO.isEmpty()) {
            throw new ResourceNotFoundException("Order not found!");
        }
        return success(orderDTO.get());
    }

    @Operation(summary = "Create new order")
    @PostMapping("/insert")
    public AppResponse<OrderDTO> createOrder(@RequestBody CreateOrderReq request) {
        try {
            return success(mvOrderService.save(request.toDTO()));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.CREATE_ERROR_OCCURRED.getDescription(), "order") + "\n" + ex.getMessage(), ex);
        }
    }

    @Operation(summary = "Update order")
    @PutMapping("/update/{orderId}")
    @PreAuthorize("@vldModuleSales.updateOrder(true)")
    public AppResponse<OrderDTO> update(@RequestBody OrderDTO order, @PathVariable("orderId") Long orderId) {
        return success(mvOrderService.update(order, orderId));
    }

    @DeleteMapping("/delete/{orderId}")
    @PreAuthorize("@vldModuleSales.deleteOrder(true)")
    public AppResponse<String> deleteOrder(@PathVariable("orderId") Long orderId) {
        //Check them trang thai
        return success(mvOrderService.delete(orderId));
    }

    @PutMapping("/do-pay/{orderId}")
    @PreAuthorize("@vldModuleSales.updateOrder(true)")
    public AppResponse<String> doPayOrder(@PathVariable("orderId") Long orderId,
                                          @RequestParam(value = "paymentTime", required = false) String paymentTime,
                                          @RequestParam("paymentMethod") Long paymentMethod,
                                          @RequestParam("paymentAmount") BigDecimal paymentAmount,
                                          @RequestParam(value = "paymentNote", required = false) String paymentNote) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");
        return success(mvOrderPayService.doPay(orderId, LocalDateTime.parse(paymentTime, formatter), paymentMethod, paymentAmount, paymentNote));
    }

    @GetMapping("/export")
    @PreAuthorize("@vldModuleSales.readOrder(true)")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        EximModel model = mvExportService.exportToExcel(TemplateExport.EX_LIST_OF_ORDERS, null, false);
        return ResponseEntity.ok().headers(model.getHttpHeaders()).body(model.getContent());
    }

    @GetMapping("/scan/QR-Code/{code}")
    public ModelAndView findOrderInfoByQRCode(@PathVariable("code") String code) {
        try {
            //Xử lý code thành id
            return new ModelAndView().addObject("orderInfo", mvOrderService.findById(null));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR_OCCURRED.getDescription(), "scan order"), ex);
        }
    }
}