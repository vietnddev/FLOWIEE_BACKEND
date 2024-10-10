package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.product.ProductPrice;
import com.flowiee.pms.entity.sales.Order;
import com.flowiee.pms.entity.sales.OrderDetail;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.model.dto.ProductVariantDTO;
import com.flowiee.pms.repository.product.ProductPriceRepository;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.utils.ChangeLog;
import com.flowiee.pms.utils.constants.ACTION;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.repository.sales.OrderDetailRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.sales.OrderHistoryService;
import com.flowiee.pms.service.sales.OrderItemsService;
import com.flowiee.pms.service.system.SystemLogService;
import com.flowiee.pms.utils.constants.MasterObject;
import com.flowiee.pms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderItemsServiceImpl extends BaseService implements OrderItemsService {
    SystemLogService      systemLogService;
    OrderHistoryService   orderHistoryService;
    OrderDetailRepository orderDetailRepository;
    @Autowired
    @NonFinal
    @Lazy
    private ProductVariantService productVariantService;
    private ProductPriceRepository productPriceRepository;

    @Override
    public List<OrderDetail> findAll() {
        return orderDetailRepository.findAll();
    }

    @Override
    public Optional<OrderDetail> findById(Integer orderDetailId) {
        return orderDetailRepository.findById(orderDetailId);
    }

    @Override
    public List<OrderDetail> findByOrderId(Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    @Override
    public List<OrderDetail> save(OrderDTO pOrderDto, List<String> productVariantIds) {
        List<OrderDetail> itemAdded = new ArrayList<>();
        for (String productVariantId : productVariantIds) {
            Optional<ProductVariantDTO> productDetail = productVariantService.findById(Integer.parseInt(productVariantId));
            if (productDetail.isPresent()) {
                OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndProductVariantId(pOrderDto.getId(), productDetail.get().getId());
                if (orderDetail != null) {
                    orderDetail.setQuantity(orderDetail.getQuantity() + 1);
                    itemAdded.add(orderDetailRepository.save(orderDetail));
                } else {
                    ProductPrice itemPrice = productPriceRepository.findPricePresent(null, productDetail.get().getId());
                    if (itemPrice == null) {
                        throw new AppException(String.format("Sản phẩm %s chưa được thiết lập giá bán!", productDetail.get().getVariantName()));
                    }
                    itemAdded.add(this.save(OrderDetail.builder()
                            .order(new Order(pOrderDto.getId()))
                            .productDetail(productDetail.get())
                            .quantity(1)
                            .status(true)
                            .price(itemPrice.getRetailPriceDiscount())
                            .priceOriginal(itemPrice.getRetailPrice())
                            .extraDiscount(BigDecimal.ZERO)
                            .priceType("L")
                            .build()));
                }
            }
        }
        return itemAdded;
    }

    @Override
    public OrderDetail save(OrderDetail orderDetail) {
        try {
            if (orderDetail.getExtraDiscount() == null) {
                orderDetail.setExtraDiscount(BigDecimal.ZERO);
            }
            OrderDetail orderDetailSaved = orderDetailRepository.save(orderDetail);
            systemLogService.writeLogCreate(MODULE.PRODUCT, ACTION.PRO_ORD_C, MasterObject.OrderDetail, "Thêm mới item vào đơn hàng", orderDetail.toString());
            logger.info("{}: Thêm mới item vào đơn hàng {}", OrderServiceImpl.class.getName(), orderDetail.toString());
            return orderDetailSaved;
        } catch (RuntimeException ex) {
            throw new AppException(ex);
        }
    }

    @Override
    public OrderDetail update(OrderDetail orderDetail, Integer orderDetailId) {
        try {
            Optional<OrderDetail> orderDetailOpt = this.findById(orderDetailId);
            if (orderDetailOpt.isEmpty()) {
                throw new BadRequestException();
            }
            OrderDetail orderItemBefore = ObjectUtils.clone(orderDetailOpt.get());

            orderDetailOpt.get().setQuantity(orderDetail.getQuantity());
            orderDetailOpt.get().setExtraDiscount(orderDetail.getExtraDiscount());
            orderDetailOpt.get().setNote(orderDetail.getNote());
            OrderDetail orderItemUpdated = orderDetailRepository.save(orderDetailOpt.get());

            String logTitle = "Cập nhật đơn hàng " + orderItemUpdated.getOrder().getCode();
            ChangeLog changeLog = new ChangeLog(orderItemBefore, orderItemUpdated);
            orderHistoryService.save(changeLog.getLogChanges(), logTitle, orderItemBefore.getOrder().getId(), orderItemBefore.getId());
            systemLogService.writeLogUpdate(MODULE.PRODUCT, ACTION.PRO_ORD_U, MasterObject.OrderDetail, logTitle, changeLog);
            logger.info("{}: Cập nhật item of đơn hàng {}", OrderServiceImpl.class.getName(), orderItemUpdated.toString());

            return orderItemUpdated;
        } catch (RuntimeException ex) {
            throw new AppException(ex);
        }
    }

    @Override
    public String delete(Integer orderDetailId) {
        Optional<OrderDetail> orderDetail = this.findById(orderDetailId);
        if (orderDetail.isEmpty()) {
            throw new BadRequestException();
        }
        try {
            orderDetailRepository.deleteById(orderDetailId);
            systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_ORD_D, MasterObject.OrderDetail, "Xóa item of đơn hàng", orderDetail.toString());
            logger.info("{}: Xóa item of đơn hàng {}", OrderServiceImpl.class.getName(), orderDetail.toString());
            return MessageCode.DELETE_SUCCESS.getDescription();
        } catch (RuntimeException ex) {
            throw new AppException(ex);
        }
    }
}