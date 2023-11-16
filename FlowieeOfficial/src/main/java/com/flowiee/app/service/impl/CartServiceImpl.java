package com.flowiee.app.service.impl;

import com.flowiee.app.common.utils.FlowieeUtil;
import com.flowiee.app.common.utils.TagName;
import com.flowiee.app.entity.OrderCart;
import com.flowiee.app.entity.SystemLog;
import com.flowiee.app.common.module.SystemModule;
import com.flowiee.app.repository.product.OrderCartRepository;
import com.flowiee.app.service.product.CartService;
import com.flowiee.app.service.system.SystemLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private OrderCartRepository orderCartRepository;
    @Autowired
    private SystemLogService systemLogService;

    @Override
    public List<OrderCart> findAll() {
        return orderCartRepository.findAll();
    }

    @Override
    public List<OrderCart> findByAccountId(Integer accountId) {
        return orderCartRepository.findByAccountId(accountId);
    }

    @Override
    public OrderCart findById(int id) {
        return orderCartRepository.findById(id).orElse(null);
    }

    @Override
    public String save(OrderCart orderCart) {
        if (orderCart == null) {
            return TagName.SERVICE_RESPONSE_FAIL;
        }
        orderCartRepository.save(orderCart);
        return "OK";
    }

    @Override
    public String delete(int id) {
        orderCartRepository.deleteById(id);
        SystemLog systemLog = new SystemLog();
        systemLog.setModule(SystemModule.SAN_PHAM.name());
        systemLog.setAction("DELETE_CART");
        systemLog.setCreatedBy(FlowieeUtil.ACCOUNT_ID);
        systemLog.setIp(FlowieeUtil.ACCOUNT_IP);
        systemLog.setNoiDung("DELETE CART");
        systemLogService.writeLog(systemLog);
        return "OK";
    }
}
