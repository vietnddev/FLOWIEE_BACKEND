package com.flowiee.pms.service.statistics;

import com.flowiee.pms.model.statistics.SalesPerformanceModel;

import java.util.List;

public interface SalesPerformanceStatisticsService {
    List<SalesPerformanceModel> getPerformanceEmployee();
}