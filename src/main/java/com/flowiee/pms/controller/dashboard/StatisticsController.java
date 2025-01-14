package com.flowiee.pms.controller.dashboard;

import com.flowiee.pms.controller.BaseController;
import com.flowiee.pms.model.AppResponse;
import com.flowiee.pms.model.statistics.SalesPerformanceModel;
import com.flowiee.pms.service.statistics.RevenueStatisticsService;
import com.flowiee.pms.service.statistics.SalesPerformanceStatisticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${app.api.prefix}/statistics")
@Tag(name = "Statistics API", description = "Statistics")
@RequiredArgsConstructor
public class StatisticsController extends BaseController {
    private final SalesPerformanceStatisticsService salesPerformanceStatisticsService;
    private final RevenueStatisticsService revenueStatisticsService;

    @GetMapping("/revenue-day")
    public AppResponse<BigDecimal> getRevenueDay(@RequestParam(value = "date", required = false) LocalDate date) {
        return success(revenueStatisticsService.getDayRevenue(date, date));
    }

    @GetMapping("/revenue-week")
    public AppResponse<BigDecimal> getRevenueWeek() {
        return success(revenueStatisticsService.getWeekRevenue());
    }

    @GetMapping("/revenue-month")
    public AppResponse<BigDecimal> getRevenueMonth(@RequestParam(value = "fmonth", required = false) Integer pFMonth,
                                                   @RequestParam(value = "fyear", required = false) Integer pFYear,
                                                   @RequestParam(value = "tmonth", required = false) Integer pTMonth,
                                                   @RequestParam(value = "tyear", required = false) Integer pTYear) {
        return success(revenueStatisticsService.getMonthRevenue(pFMonth, pFYear, pTMonth, pTYear));
    }

    @GetMapping("/sales-performance")
    public AppResponse<List<SalesPerformanceModel>> getPerformanceEmployee() {
        return success(salesPerformanceStatisticsService.getPerformanceEmployee());
    }
}