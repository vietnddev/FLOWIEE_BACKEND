package com.flowiee.app.service.impl;

import com.flowiee.app.entity.OrderHistory;
import com.flowiee.app.repository.OrderHistoryRepository;
import com.flowiee.app.service.OrderHistoryService;
import com.flowiee.app.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderHistoryServiceImpl implements OrderHistoryService {
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Override
    public List<OrderHistory> findAll() {
        return orderHistoryRepository.findAll();
    }

    @Override
    public OrderHistory findById(Integer orderHistoryId) {
        return orderHistoryRepository.findById(orderHistoryId).orElse(null);
    }

    @Override
    public String save(OrderHistory orderHistory) {
        orderHistoryRepository.save(orderHistory);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public String update(OrderHistory orderHistory, Integer orderHistoryId) {
        orderHistory.setId(orderHistoryId);
        orderHistoryRepository.save(orderHistory);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public String delete(Integer orderHistoryId) {
        orderHistoryRepository.deleteById(orderHistoryId);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public List<OrderHistory> findByOrderId(Integer orderId) {
        return orderHistoryRepository.findByOrderId(orderId);
    }

    @Override
    public List<OrderHistory> findByOrderDetailId(Integer orderDetailId) {
        return orderHistoryRepository.findByOrderDetailId(orderDetailId);
    }

    @Override
    public List<OrderHistory> findByOrderPayId(Integer orderPayId) {
        return orderHistoryRepository.findByOrderPayId(orderPayId);
    }
}