package com.flowiee.pms.service.statistics.impl;

import com.flowiee.pms.entity.sales.Order;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.entity.system.GroupAccount;
import com.flowiee.pms.model.statistics.SalesPerformanceModel;
import com.flowiee.pms.repository.sales.OrderRepository;
import com.flowiee.pms.repository.system.AccountRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.sales.OrderService;
import com.flowiee.pms.service.statistics.SalesPerformanceStatisticsService;
import com.flowiee.pms.utils.OrderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesPerformanceStatisticsServiceImpl extends BaseService implements SalesPerformanceStatisticsService {
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<SalesPerformanceModel> getPerformanceEmployee() {
        List<SalesPerformanceModel> returnList = new ArrayList<>();
        List<Account> employeeList = accountRepository.findAll();
        for (Account employee : employeeList) {
            List<Order> orderList = orderRepository.findAll(null, null, null, null, null,
                    employee.getId(), null, null, null,
                    LocalDateTime.of(1900, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2100, 12, 1, 0, 0, 0), Pageable.unpaged()).getContent();

            String lvEmployeeName = employee.getFullName();
            GroupAccount lvGroupEmployee = employee.getGroupAccount();
            String lvEmployeePosition = lvGroupEmployee != null ? lvGroupEmployee.getGroupName() : "-";
            BigDecimal lvTotalRevenue = OrderUtils.calTotalAmount(orderList);
            Integer lvTotalTransactions = orderList.size();
            Float lvTargetAchievementRate = 0f;
            String lvEffectiveSalesTime= "";
            Integer lvNumberOfProductsSold = OrderUtils.countItemsListOrder(orderList);

            returnList.add(SalesPerformanceModel.builder()
                    .employeeName(lvEmployeeName)
                    .employeePosition(lvEmployeePosition)
                    .totalRevenue(lvTotalRevenue)
                    .totalTransactions(lvTotalTransactions)
                    .targetAchievementRate(lvTargetAchievementRate)
                    .effectiveSalesTime(lvEffectiveSalesTime)
                    .numberOfProductsSold(lvNumberOfProductsSold)
                    .build());
        }
        return returnList;
    }
}