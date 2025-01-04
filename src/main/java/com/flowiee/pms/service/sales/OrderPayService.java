package com.flowiee.pms.service.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderPayService {
    String doPay(Long orderId, LocalDateTime paymentTime, Long paymentMethod, BigDecimal paymentAmount, String paymentNote);
}