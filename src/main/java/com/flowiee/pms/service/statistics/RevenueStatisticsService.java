package com.flowiee.pms.service.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RevenueStatisticsService {
    BigDecimal getDayRevenue(LocalDate pFromDate, LocalDate pToDate);

    BigDecimal getWeekRevenue();

    BigDecimal getMonthRevenue(LocalDate pFromMonth, LocalDate pToMonth);
}