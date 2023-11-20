package com.flowiee.app.service.impl;

import com.flowiee.app.common.utils.TagName;
import com.flowiee.app.entity.OrderDetail;
import com.flowiee.app.entity.TicketExportGoods;
import com.flowiee.app.repository.product.TicketExportGoodsRepository;
import com.flowiee.app.service.product.OrderService;
import com.flowiee.app.service.product.ProductVariantService;
import com.flowiee.app.service.storage.TicketExportGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketExportGoodsServiceImpl implements TicketExportGoodsService {
    @Autowired
    private TicketExportGoodsRepository ticketExportGoodsRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductVariantService productVariantService;

    @Override
    public List<TicketExportGoods> findAll() {
        return ticketExportGoodsRepository.findAll();
    }

    @Override
    public TicketExportGoods findById(Integer entityId) {
        return ticketExportGoodsRepository.findById(entityId).get();
    }

    @Override
    public String save(TicketExportGoods ticket) {
        ticketExportGoodsRepository.save(ticket);
        //Code for order status
        //PR -> Preparing
        //WS -> Waiting shipper
        //DE -> Delivering
        //DO -> Done
        //Update lại số lượng của sản phẩm còn trong kho
        List<OrderDetail> listOrderDetail = orderService.findById(ticket.getOrderId()).getListOrderDetail();
        for (OrderDetail d : listOrderDetail) {
            productVariantService.updateSoLuong(productVariantService.findById(d.getProductVariant().getId()).getSoLuongKho() - d.getSoLuong(), d.getProductVariant().getId());
        }
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public String update(TicketExportGoods ticket, Integer entityId) {
        ticket.setId(entityId);
        ticketExportGoodsRepository.save(ticket);
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public String delete(Integer entityId) {
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }
}