package com.flowiee.pms.utils;

import com.flowiee.pms.entity.sales.Order;
import com.flowiee.pms.entity.sales.OrderDetail;

import java.math.BigDecimal;
import java.util.List;

public class OrderUtils {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static BigDecimal calTotalAmount(List<Order> orderList) {
        BigDecimal lvAmount = BigDecimal.ZERO;
        if (orderList == null) {
            return lvAmount;
        }
        for (Order lvOrder : orderList) {
            BigDecimal lvOrderValue = calTotalAmount(lvOrder.getListOrderDetail(), lvOrder.getAmountDiscount());
            lvAmount = lvAmount.add(lvOrderValue);
        }
        return lvAmount;
    }

    public static BigDecimal calTotalAmount(List<OrderDetail> orderDetails, BigDecimal amountDiscount) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return ZERO;
        }
        BigDecimal totalAmount = ZERO;
        for (OrderDetail d : orderDetails) {
            totalAmount = totalAmount.add((d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity()))).subtract(d.getExtraDiscount()));
        }
        return totalAmount.subtract(amountDiscount);
    }

    public static int countItemsListOrder(List<Order> pOrders) {
        if (pOrders == null || pOrders.isEmpty()) {
            return 0;
        }
        int totalItems = 0;
        for (Order lvOrder : pOrders) {
            totalItems += countItemsEachOrder(lvOrder.getListOrderDetail());
        }
        return totalItems;
    }

    public static int countItemsEachOrder(List<OrderDetail> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0;
        }
        int totalItems = 0;
        for (OrderDetail d : orderItems) {
            totalItems += d.getQuantity();
        }
        return totalItems;
    }

    public static int calBonusPoints(BigDecimal totalAmount) {
        BigDecimal bonusPoints = totalAmount.divide(ONE_HUNDRED).setScale(0, BigDecimal.ROUND_DOWN);
        return bonusPoints.intValue();
    }
}