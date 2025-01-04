package com.flowiee.pms.service.sales;

import com.flowiee.pms.service.BaseCurdService;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.entity.sales.Order;

import com.flowiee.pms.utils.constants.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService extends BaseCurdService<OrderDTO> {
    Page<OrderDTO> findAll(int pageSize, int pageNum, String pTxtSearch, Long pOrderId, Long pPaymentMethodId,
                           OrderStatus pOrderStatus, Long pSalesChannelId, Long pSellerId, Long pCustomerId,
                           Long pBranchId, Long pGroupCustomerId, String pDateFilter, LocalDateTime pOrderTimeFrom, LocalDateTime pOrderTimeTo, String pSortBy);

    Optional<OrderDTO> findById(Long orderId);

    OrderDTO findById(Long orderId, boolean throwException);

    List<Order> findOrdersToday();

    Page<OrderDTO> getOrdersByCustomer(int pageSize, int pageNum, Long pCustomerId);
}