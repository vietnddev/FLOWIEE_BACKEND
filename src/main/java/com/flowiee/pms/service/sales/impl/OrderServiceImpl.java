package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.sales.*;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.utils.constants.ACTION;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.model.dto.OrderDetailDTO;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.exception.DataInUseException;
import com.flowiee.pms.model.dto.ProductVariantDTO;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.service.sales.*;
import com.flowiee.pms.utils.*;
import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.repository.sales.OrderRepository;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl extends BaseService implements OrderService {
    private static final String mainObjectName = "Order";

    private final OrderRepository orderRepository;
    private final ProductVariantService productVariantService;
    private final CartService cartService;
    private final CartItemsService cartItemsService;
    private final OrderHistoryService orderHistoryService;
    private final OrderItemsService orderItemsService;
    private final OrderQRCodeService orderQRCodeService;
    private final VoucherTicketService voucherTicketService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, CartService cartService, CartItemsService cartItemsService,
                            OrderHistoryService orderHistoryService, ProductVariantService productVariantService,
                            OrderQRCodeService orderQRCodeService, VoucherTicketService voucherTicketService,
                            OrderItemsService orderItemsService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.cartItemsService = cartItemsService;
        this.orderHistoryService = orderHistoryService;
        this.orderItemsService = orderItemsService;
        this.productVariantService = productVariantService;
        this.orderQRCodeService = orderQRCodeService;
        this.voucherTicketService = voucherTicketService;
    }

    @Override
    public List<OrderDTO> findAll() {
        return this.findAll(-1, -1, null, null, null, null, null, null, null, null, null).getContent();
    }

    @Override
    public Page<OrderDTO> findAll(int pPageSize, int pPageNum, Integer pOrderId, Integer pPaymentMethodId,
                                  Integer pOrderStatusId, Integer pSalesChannelId, Integer pSellerId, Integer pCustomerId,
                                  LocalDateTime pOrderTimeFrom, LocalDateTime pOrderTimeTo, String pSortBy) {
        Pageable pageable = Pageable.unpaged();
        if (pPageSize >= 0 && pPageNum >= 0) {
            pageable = PageRequest.of(pPageNum, pPageSize, Sort.by(pSortBy != null ? pSortBy : "orderTime").descending());
        }
        if (pOrderTimeFrom == null) {
            pOrderTimeFrom = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
        }
        if (pOrderTimeTo == null) {
            pOrderTimeTo = LocalDateTime.of(2100, 12, 1, 0, 0, 0);
        }
        Page<Order> orders = orderRepository.findAll(pOrderId, pPaymentMethodId, pOrderStatusId, pSalesChannelId, pSellerId, pCustomerId, pOrderTimeFrom, pOrderTimeTo, pageable);
        return new PageImpl<>(OrderDTO.fromOrders(orders.getContent()), pageable, orders.getTotalElements());
    }

    @Override
    public Optional<OrderDTO> findById(Integer orderId) {
        OrderDTO order = OrderDTO.fromOrder(Objects.requireNonNull(orderRepository.findById(orderId).orElse(null)));
        List<OrderDetail> listOrderDetail = orderItemsService.findByOrderId(orderId);
        int totalProduct = 0;
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderDetail d : listOrderDetail) {
            totalProduct += d.getQuantity();
            totalAmount = totalAmount.add(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
        }
        List<OrderDetailDTO> orderDetailDTOs = OrderDetailDTO.fromOrderDetails(listOrderDetail);;
        order.setListOrderDetailDTO(orderDetailDTOs);
        order.setTotalProduct(totalProduct);
        order.setTotalAmount(totalAmount);
        order.setTotalAmountDiscount(order.getTotalAmount().subtract(order.getAmountDiscount()));
        return Optional.of(order);
    }

    @Transactional
    @Override
    public OrderDTO save(OrderDTO request) {
        try {
            //Insert order
            Order order = new Order();
            order.setCode(request.getCode() != null ? request.getCode() : CommonUtils.genOrderCode());
            order.setCustomer(new Customer(request.getCustomerId()));
            order.setKenhBanHang(new Category(request.getSalesChannelId(), null));
            order.setNhanVienBanHang(new Account(request.getCashierId()));
            order.setNote(request.getNote());
            if (request.getOrderTime() != null) {
                order.setOrderTime(request.getOrderTime());
            } else {
                order.setOrderTime(LocalDateTime.now());
            }
            order.setTrangThaiDonHang(new Category(request.getOrderStatusId(), null));
            order.setReceiverName(request.getReceiverName());
            order.setReceiverPhone(request.getReceiverPhone());
            order.setReceiverEmail(request.getReceiverEmail());
            order.setReceiverAddress(request.getReceiverAddress());
            if (request.getPayMethodId() != null) {
                order.setPaymentMethod(new Category(request.getPayMethodId(), null));
            }
            order.setPaymentStatus(false);

            if (ObjectUtils.isNotEmpty(request.getVoucherUsedCode())) {
                order.setVoucherUsedCode(request.getVoucherUsedCode());
            }
            if (ObjectUtils.isNotEmpty(request.getAmountDiscount())) {
                order.setAmountDiscount(request.getAmountDiscount());
            }

            Order orderSaved = orderRepository.save(order);

            //QRCode
            orderQRCodeService.saveQRCodeOfOrder(orderSaved.getId());

            //Insert items detail
            Optional<OrderCart> cart = cartService.findById(request.getCartId());
            if (cart.isPresent()) {
                for (Items items : cart.get().getListItems()) {
                    Optional<ProductVariantDTO> productDetail = productVariantService.findById(items.getProductDetail().getId());
                    if (productDetail.isPresent()) {
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setOrder(orderSaved);
                        orderDetail.setProductDetail(productDetail.get());
                        orderDetail.setQuantity(cartItemsService.findQuantityOfItem(items.getOrderCart().getId() , productDetail.get().getId()));
                        orderDetail.setStatus(true);
                        orderDetail.setNote(items.getNote());
                        orderDetail.setPrice(items.getPrice());
                        orderDetail.setPriceOriginal(items.getPriceOriginal());
                        orderDetail.setExtraDiscount(items.getExtraDiscount() != null ? items.getExtraDiscount() : BigDecimal.ZERO);
                        orderDetail.setPriceType(items.getPriceType());
                        orderItemsService.save(orderDetail);
                    }
                }
            }

            //Update voucher ticket status
            VoucherTicket voucherTicket = voucherTicketService.findTicketByCode((request.getVoucherUsedCode()));
            if (voucherTicket != null) {
//                String statusCode = voucherService.checkTicketToUse(request.getVoucherUsedCode());
//                if (AppConstants.VOUCHER_STATUS.ACTIVE.name().equals(statusCode)) {
//                    orderSaved.setVoucherUsedCode(request.getVoucherUsedCode());
//                    orderSaved.setAmountDiscount(request.getAmountDiscount());
//                }
                voucherTicket.setCustomer(orderSaved.getCustomer());
                voucherTicket.setActiveTime(new Date());
                voucherTicket.setUsed(true);
                voucherTicketService.update(voucherTicket, voucherTicket.getId());
            }
            //orderRepo.save(orderSaved);

            //Sau khi đã lưu đơn hàng thì xóa all items
            cartItemsService.deleteAllItems();

            //Log
            systemLogService.writeLogCreate(MODULE.PRODUCT.name(), ACTION.PRO_ORD_C.name(), mainObjectName, "Thêm mới đơn hàng", order.toString());
            logger.info("Insert new order success! insertBy={}", CommonUtils.getUserPrincipal().getUsername());

            return OrderDTO.fromOrder(orderSaved);
        } catch (RuntimeException ex) {
            throw new AppException("Insert new order fail! order=" + request, ex);
        }
    }

    @Override
    public OrderDTO update(OrderDTO dto, Integer id) {
        Order orderToUpdate = orderRepository.findById(id).orElse(null);
        if (orderToUpdate == null) {
            throw new BadRequestException();
        }
        Order orderBefore = ObjectUtils.clone(orderToUpdate);
        orderToUpdate.setNote(dto.getNote());
        orderToUpdate.setTrangThaiDonHang(new Category(dto.getOrderStatusId(), null));
        Order orderUpdated = orderRepository.save(orderToUpdate);

        String logTitle = "Cập nhật đơn hàng";
        Map<String, Object[]> logChanges = LogUtils.logChanges(orderBefore, orderUpdated);
        orderHistoryService.save(logChanges, logTitle, id, null);
        systemLogService.writeLogUpdate(MODULE.PRODUCT.name(), ACTION.PRO_ORD_U.name(), mainObjectName, logTitle, logChanges);
        logger.info("Cập nhật đơn hàng {}", dto.toString());

        return OrderDTO.fromOrder(orderToUpdate);
    }

    @Override
    public String delete(Integer id) {
        Optional<OrderDTO> order = this.findById(id);
        if (order.isEmpty() || order.get().getPaymentStatus()) {
            throw new DataInUseException(MessageUtils.ERROR_DATA_LOCKED);
        }
        orderRepository.deleteById(id);
        systemLogService.writeLogDelete(MODULE.PRODUCT.name(), ACTION.PRO_ORD_D.name(), mainObjectName, "Xóa đơn hàng", order.toString());
        logger.info("Xóa đơn hàng orderId={}", id);
        return MessageUtils.DELETE_SUCCESS;
    }

    @Transactional
    @Override
    public String doPay(Integer orderId, LocalDateTime paymentTime, Integer paymentMethod, Float paymentAmount, String paymentNote) {
        orderRepository.updatePaymentStatus(orderId, paymentTime, paymentMethod, paymentAmount, paymentNote);
        return "Thanh toán thành công!";
    }

    @Override
    public List<Order> findOrdersToday() {
        return orderRepository.findOrdersToday();
    }

    private String getNextOrderCode() {
        return "";
    }
}