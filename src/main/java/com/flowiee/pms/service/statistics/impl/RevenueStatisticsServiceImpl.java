package com.flowiee.pms.service.statistics.impl;

import com.flowiee.pms.entity.sales.Order;
import com.flowiee.pms.repository.sales.OrderRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.statistics.RevenueStatisticsService;
import com.flowiee.pms.utils.OrderUtils;
import com.flowiee.pms.utils.constants.FilterDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueStatisticsServiceImpl extends BaseService implements RevenueStatisticsService {
    private final OrderRepository orderRepository;

    @Override
    public BigDecimal getDayRevenue(LocalDate pFromDate, LocalDate pToDate) {
        LocalDateTime[] lvDate;
        if (pFromDate == null || pFromDate == null) {
             lvDate = getFromDateToDate(FilterDate.getByCode("T0"));//Today
        } else {
            lvDate = new LocalDateTime[] {pFromDate.atTime(LocalTime.MIN), pToDate.atTime(LocalTime.MAX)};
        }
        List<Order> lvOrderList = orderRepository.findBySuccessfulDeliveryTime(lvDate[0], lvDate[1]);
        BigDecimal lvRevenue = OrderUtils.calTotalAmount(lvOrderList);
        return lvRevenue;
    }

    @Override
    public BigDecimal getWeekRevenue() {
        LocalDateTime[] lvDate = getFromDateToDate(FilterDate.getByCode("T-7"));
        List<Order> lvOrderList = orderRepository.findBySuccessfulDeliveryTime(lvDate[0], lvDate[1]);
        BigDecimal lvRevenue = OrderUtils.calTotalAmount(lvOrderList);
        return lvRevenue;
    }

    @Override
    public BigDecimal getMonthRevenue(Integer pFMonth, Integer pFYear, Integer pTMonth, Integer pTYear) {
        LocalDateTime[] lvDate;
        if (pFMonth == null || pFYear == null || pTMonth == null || pTYear == null) {
            lvDate = getFromDateToDate(FilterDate.getByCode("M0"));//This month
        } else {
            lvDate = new LocalDateTime[] {
                    YearMonth.of(pFYear, pFMonth).atDay(1).atStartOfDay(),
                    YearMonth.of(pTYear, pTMonth).atEndOfMonth().atTime(23, 59, 59)
            };
        }
        List<Order> lvOrderList = orderRepository.findBySuccessfulDeliveryTime(lvDate[0], lvDate[1]);
        BigDecimal lvRevenue = OrderUtils.calTotalAmount(lvOrderList);
        return lvRevenue;
    }
}