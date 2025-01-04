package com.flowiee.pms.service.statistics.impl;

import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.statistics.RevenueStatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class RevenueStatisticsServiceImpl extends BaseService implements RevenueStatisticsService {
    @Override
    public BigDecimal getDayRevenue(LocalDate pFromDate, LocalDate pToDate) {
        return null;
    }

    @Override
    public BigDecimal getWeekRevenue() {
        return null;
    }

    @Override
    public BigDecimal getMonthRevenue(LocalDate pFromMonth, LocalDate pToMonth) {
        return null;
    }
}