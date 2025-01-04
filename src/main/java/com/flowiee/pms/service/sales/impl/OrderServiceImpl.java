package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.product.ProductDetail;
import com.flowiee.pms.entity.sales.*;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.entity.system.SystemConfig;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.exception.EntityNotFoundException;
import com.flowiee.pms.repository.sales.CustomerRepository;
import com.flowiee.pms.repository.system.ConfigRepository;
import com.flowiee.pms.service.category.CategoryService;
import com.flowiee.pms.service.system.AccountService;
import com.flowiee.pms.service.system.MailMediaService;
import com.flowiee.pms.utils.ChangeLog;
import com.flowiee.pms.utils.constants.*;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.exception.DataInUseException;
import com.flowiee.pms.model.dto.ProductVariantDTO;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.service.sales.*;
import com.flowiee.pms.utils.*;
import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.repository.sales.OrderRepository;

import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends BaseService implements OrderService {
    private final CartService           mvCartService;
    private final OrderRepository       mvOrderRepository;
    private final ConfigRepository      mvConfigRepository;
    private final MailMediaService      mvMailMediaService;
    private final CartItemsService      mvCartItemsService;
    private final OrderItemsService     mvOrderItemsService;
    private final GenerateQRCodeService mvGenerateQRCodeService;
    private final CustomerRepository    mvCustomerRepository;
    private final OrderHistoryService   mvOrderHistoryService;
    private final TicketImportService   mvTicketImportService;
    private final VoucherTicketService  mvVoucherTicketService;
    private final ProductVariantService mvProductVariantService;
    private final LoyaltyProgramService mvLoyaltyProgramService;
    private final CategoryService       mvCategoryService;
    private final CustomerService       mvCustomerService;
    private final AccountService        mvAccountService;

    private BigDecimal mvDefaultShippingCost = BigDecimal.ZERO;
    private BigDecimal mvDefaultPackagingCost = BigDecimal.ZERO;
    private BigDecimal mvDefaultGiftWrapCost = BigDecimal.ZERO;
    private BigDecimal mvDefaultCodFee = BigDecimal.ZERO;

    @Override
    public List<OrderDTO> findAll() {
        return this.findAll(-1, -1, null, null, null, OrderStatus.ALL, null, null, null, null, null, null, null, null, null).getContent();
    }

    @Override
    public Page<OrderDTO> findAll(int pPageSize, int pPageNum, String pTxtSearch, Long pOrderId, Long pPaymentMethodId,
                                  OrderStatus pOrderStatus, Long pSalesChannelId, Long pSellerId, Long pCustomerId,
                                  Long pBranchId, Long pGroupCustomerId, String pDateFilter, LocalDateTime pOrderTimeFrom, LocalDateTime pOrderTimeTo, String pSortBy) {
        Pageable pageable = getPageable(pPageNum, pPageSize, Sort.by(pSortBy != null ? pSortBy : "orderTime").descending());
        if (pOrderTimeFrom == null) {
            pOrderTimeFrom = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
        }
        if (pOrderTimeTo == null) {
            pOrderTimeTo = LocalDateTime.of(2100, 12, 1, 0, 0, 0);
        }
        if (pDateFilter != null && !"".equals(pDateFilter)) {
            LocalDateTime[] lvFromDateToDate = getFromDateToDate(pOrderTimeFrom, pOrderTimeTo, pDateFilter);
            pOrderTimeFrom = lvFromDateToDate[0];
            pOrderTimeTo = lvFromDateToDate[1];
        }
        Page<Order> orders = mvOrderRepository.findAll(pTxtSearch, pOrderId, pPaymentMethodId, pOrderStatus, pSalesChannelId, pSellerId, pCustomerId, pBranchId, pGroupCustomerId, pOrderTimeFrom, pOrderTimeTo, pageable);
        return new PageImpl<>(OrderDTO.fromOrders(orders.getContent()), pageable, orders.getTotalElements());
    }

    @Override
    public Optional<OrderDTO> findById(Long orderId) {
        Optional<Order> orderOptional = mvOrderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(OrderDTO.fromOrder(orderOptional.get()));
    }

    @Override
    public OrderDTO findById(Long pOrderId, boolean throwException) {
        Optional<OrderDTO> lvOrderOptional = this.findById(pOrderId);
        if (lvOrderOptional.isPresent()) {
            return lvOrderOptional.get();
        }
        if (throwException) {
            throw new EntityNotFoundException(new Object[] {"order"}, null, null);
        }
        return null;
    }

    private VldModel vldBeforeCreateOrder(OrderDTO pOrderInfo, Long pCartId, String pVoucherCode, Long pPaymentMethodId, Long pSaleChannelId, Long pCustomerId, Long pSalesAssistantId) {
        OrderCart lvCart = mvCartService.findById(pCartId, true);
        if (ObjectUtils.isEmpty(lvCart.getListItems()))
            throw new BadRequestException("At least one product in the order!");

        Category lvPaymentMethod = mvCategoryService.findById(pPaymentMethodId, false);
        if (lvPaymentMethod == null || !lvPaymentMethod.getStatus())
            throw new BadRequestException("Payment method invalid!");
        Category lvSalesChannel = mvCategoryService.findById(pSaleChannelId, false);
        if (lvSalesChannel == null || !lvSalesChannel.getStatus())
            throw new BadRequestException("Sales channel invalid!");

        Customer lvCustomer = mvCustomerService.findById(pCustomerId, true);
        if (lvCustomer.getIsBlackList())
            throw new BadRequestException("The customer is on the blacklist!");
        if (!CoreUtils.validateEmail(pOrderInfo.getReceiverEmail()))
            throw new BadRequestException("Email invalid!");
        if (!CoreUtils.validatePhoneNumber(pOrderInfo.getReceiverPhone(), CommonUtils.defaultCountryCode))
            throw new BadRequestException("Phone number invalid!");
        if (CoreUtils.isNullStr(pOrderInfo.getReceiverAddress()))
            throw new BadRequestException("Address must not empty!");

        Account lvSalesAssistant = mvAccountService.findById(pSalesAssistantId, false);
        if (lvSalesAssistant == null || lvSalesAssistant.isClosed())
            throw new BadRequestException("Sales assistant invalid!");

        VoucherTicket lvVoucherTicket = null;
        if (!CoreUtils.isNullStr(pVoucherCode)) {
            lvVoucherTicket = mvVoucherTicketService.findTicketByCode((pVoucherCode));
            if (lvVoucherTicket == null)
                throw new BadRequestException("Voucher invalid!");
            if (lvVoucherTicket.isUsed())
                throw new BadRequestException("Voucher code already used!");
        }

        VldModel vldModel = new VldModel();
        vldModel.setSalesAssistant(lvSalesAssistant);
        vldModel.setPaymentMethod(lvPaymentMethod);
        vldModel.setSalesChannel(lvSalesChannel);
        vldModel.setCustomer(lvCustomer);
        vldModel.setOrderCart(lvCart);
        vldModel.setVoucherTicket(lvVoucherTicket);

        return vldModel;
    }

    @Transactional
    @Override
    public OrderDTO save(OrderDTO request) {
        BigDecimal lvAmountDiscount = request.getAmountDiscount();
        String lvVoucherUsedCode = request.getVoucherUsedCode();
        LocalDateTime lvOrderTime = request.getOrderTime();
        Long lvSaleChannelId = request.getSalesChannelId();
        Long lvPaymentMethodId = request.getPayMethodId();
        Long lvCustomerId = request.getCustomerId();
        Long lvSalesAssistantId = request.getCashierId();
        Long lvCartId = request.getCartId();

        VldModel vldModel = vldBeforeCreateOrder(request, lvCartId, lvVoucherUsedCode, lvPaymentMethodId, lvSaleChannelId, lvCustomerId, lvSalesAssistantId);
        OrderCart lvCart = vldModel.getOrderCart();
        VoucherTicket lvVoucherTicket = vldModel.getVoucherTicket();
        Customer lvCustomer = vldModel.getCustomer();
        Category lvSalesChannel = vldModel.getSalesChannel();
        Account lvSalesAssistant = vldModel.getSalesAssistant();
        Category lvPaymentMethod = vldModel.getPaymentMethod();

        Order order = Order.builder()
                .code(getNextOrderCode())
                .customer(lvCustomer)
                .kenhBanHang(lvSalesChannel)
                .nhanVienBanHang(lvSalesAssistant)
                .note(request.getNote())
                .orderTime(lvOrderTime != null ? lvOrderTime : LocalDateTime.now())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverEmail(request.getReceiverEmail())
                .receiverAddress(request.getReceiverAddress())
                .paymentMethod(lvPaymentMethod)
                .paymentStatus(false)
                .voucherUsedCode(ObjectUtils.isNotEmpty(lvVoucherUsedCode) ? lvVoucherUsedCode : null)
                .amountDiscount(CoreUtils.coalesce(lvAmountDiscount))
                .packagingCost(CoreUtils.coalesce(request.getPackagingCost(), mvDefaultPackagingCost))
                .shippingCost(CoreUtils.coalesce(request.getShippingCost(), mvDefaultShippingCost))
                .giftWrapCost(CoreUtils.coalesce(request.getGiftWrapCost(), mvDefaultGiftWrapCost))
                .codFee(CoreUtils.coalesce(request.getCodFee(), mvDefaultCodFee))
                .isGiftWrapped(false)
                .orderStatus(getDefaultOrderStatus())
                .build();

        if (lvVoucherTicket != null) {
            order.setVoucherUsedCode(lvVoucherTicket.getCode());
            //Update voucher ticket's status to used
            lvVoucherTicket.setCustomer(lvCustomer);
            lvVoucherTicket.setActiveTime(new Date());
            lvVoucherTicket.setUsed(true);
            mvVoucherTicketService.update(lvVoucherTicket, lvVoucherTicket.getId());
        }

        //Create order
        Order orderSaved = mvOrderRepository.save(order);
        //Create QRCode
        try {
            mvGenerateQRCodeService.generateOrderQRCode(orderSaved.getId());
        } catch (IOException | WriterException e ) {
            e.printStackTrace();
            logger.error(String.format("Can't generate QR Code for Order %s", orderSaved.getCode()), e);
        }
        //Create items detail
        List<OrderDetail> itemsList = new ArrayList<>();
        for (Items items : lvCart.getListItems()) {
            Long lvProductVariantId = items.getProductDetail().getId();
            BigDecimal lvExtraDiscount = CoreUtils.coalesce(items.getExtraDiscount());
            ProductVariantDTO productDetail = mvProductVariantService.findById(lvProductVariantId, true);
            String productVariantName = productDetail.getVariantName();
            int lvItemQuantity = mvCartItemsService.findQuantityOfItemProduct(lvCart.getId(), lvProductVariantId);
            if (lvItemQuantity <= 0) {
                throw new BadRequestException(String.format("The quantity of product %s must greater than zero!", productVariantName));
            }
            if (lvItemQuantity > productDetail.getAvailableSalesQty()) {
                throw new AppException(ErrorCode.ProductOutOfStock, new Object[]{productVariantName}, null, getClass(), null);
            }
            itemsList.add(mvOrderItemsService.save(OrderDetail.builder()
                    .order(orderSaved)
                    .productDetail(productDetail)
                    .quantity(lvItemQuantity)
                    .status(true)
                    .note(items.getNote())
                    .price(items.getPrice())
                    .priceOriginal(items.getPriceOriginal())
                    .extraDiscount(lvExtraDiscount)
                    .priceType(items.getPriceType())
                    .build()));
        }
        BigDecimal totalAmountDiscount = OrderUtils.calTotalAmount(itemsList, orderSaved.getAmountDiscount());
        if (totalAmountDiscount.doubleValue() <= 0) {
            throw new BadRequestException("The value of order must greater than zero!");
        }

        //Accumulate bonus points for customer
        if (request.getAccumulateBonusPoints() != null && request.getAccumulateBonusPoints()) {
            int bonusPoints = OrderUtils.calBonusPoints(totalAmountDiscount.subtract(orderSaved.getAmountDiscount()));
            mvCustomerRepository.updateBonusPoint(lvCustomerId, bonusPoints);
        }

        //Sau khi đã lưu đơn hàng thì xóa all items
        mvCartItemsService.deleteAllItems();

        //Log
        systemLogService.writeLogCreate(MODULE.PRODUCT, ACTION.PRO_ORD_C, MasterObject.Order, "Thêm mới đơn hàng", orderSaved.getCode());
        logger.info("Insert new order success! insertBy={}", CommonUtils.getUserPrincipal().getUsername());

        return OrderDTO.fromOrder(orderSaved);
    }

    private VldModel vldBeforeUpdateOrder(OrderDTO pOrderInfo) {
        if (!CoreUtils.validateEmail(pOrderInfo.getReceiverEmail()))
            throw new BadRequestException("Email invalid!");
        if (!CoreUtils.validatePhoneNumber(pOrderInfo.getReceiverPhone(), CommonUtils.defaultCountryCode))
            throw new BadRequestException("Phone number invalid!");
        if (CoreUtils.isNullStr(pOrderInfo.getReceiverAddress()))
            throw new BadRequestException("Address must not empty!");

        return new VldModel();
    }

    private Order beforeUpdateOrder(Order pOrder) {
        OrderStatus lvOrderStatus = pOrder.getOrderStatus();
        LocalDateTime lvSuccessfulDeliveryTime = pOrder.getSuccessfulDeliveryTime();

        switch (lvOrderStatus) {
            case RTND:
                SystemConfig lvReturnPeriodDaysMdl = mvConfigRepository.findByCode(ConfigCode.returnPeriodDays.name());
                if (isConfigAvailable(lvReturnPeriodDaysMdl)) {
                    throw new AppException("System has not configured the time allowed to return the order!");
                }
                int lvReturnPeriodDays = lvReturnPeriodDaysMdl.getIntValue();
                if (!isWithinReturnPeriod(lvSuccessfulDeliveryTime, LocalDateTime.now(), lvReturnPeriodDays)) {
                    throw new AppException("The return period has expired!");
                }
                break;
        }

        return ObjectUtils.clone(pOrder);
    }

    @Transactional
    @Override
    public OrderDTO update(OrderDTO dto, Long pOrderId) {
        Order orderToUpdate = mvOrderRepository.findById(pOrderId)
                .orElseThrow(() -> new AppException("Order not found!"));
        LocalDateTime lvSuccessfulDeliveryTime = dto.getSuccessfulDeliveryTime();
        OrderStatus lvOrderStatus = dto.getOrderStatus();

        //Validate request data
        vldBeforeUpdateOrder(dto);
        //Validate business before update
        Order orderBefore = beforeUpdateOrder(orderToUpdate);

        //Update new info
        orderToUpdate.setReceiverName(dto.getReceiverName());
        orderToUpdate.setReceiverPhone(dto.getReceiverPhone());
        orderToUpdate.setReceiverEmail(dto.getReceiverEmail());
        orderToUpdate.setReceiverAddress(dto.getReceiverAddress());
        orderToUpdate.setNote(dto.getNote());
        orderToUpdate.setOrderStatus(lvOrderStatus);
        if (lvOrderStatus.equals(OrderStatus.DLVD))
            orderToUpdate.setSuccessfulDeliveryTime(lvSuccessfulDeliveryTime != null ? lvSuccessfulDeliveryTime : LocalDateTime.now());
        if (lvOrderStatus.equals(OrderStatus.CNCL)) {
            orderToUpdate.setCancellationReason(dto.getCancellationReason());
            orderToUpdate.setCancellationDate(LocalDateTime.now());
        }
        Order orderUpdated = mvOrderRepository.save(orderToUpdate);

        //Do something after updated
        afterUpdatedOrder(orderUpdated, null);

        //Log
        ChangeLog changeLog = new ChangeLog(orderBefore, orderUpdated);
        mvOrderHistoryService.save(changeLog.getLogChanges(), "Cập nhật đơn hàng", pOrderId, null);
        systemLogService.writeLogUpdate(MODULE.SALES, ACTION.PRO_ORD_U, MasterObject.Order, "Cập nhật đơn hàng", changeLog);
        logger.info("Cập nhật đơn hàng {}", dto.toString());

        return OrderDTO.fromOrder(orderToUpdate);
    }

    private void afterUpdatedOrder(Order pOrderUpdated, Long pLoyaltyProgramId) {
        String lvOrderCode = pOrderUpdated.getCode();
        OrderStatus lvOrderStatus = pOrderUpdated.getOrderStatus();
        Long lvStorageId = pOrderUpdated.getTicketExport().getStorage().getId();
        boolean isNeedRefund = false;

        switch (lvOrderStatus) {
            case CNCL:
                SystemConfig lvSendNotifyConfig = mvConfigRepository.findByCode(ConfigCode.sendNotifyCustomerOnOrderConfirmation.name());
                if (isConfigAvailable(lvSendNotifyConfig) && lvSendNotifyConfig.isYesOption()) {
                    sendNotifyCustomerOnOrderConfirmation(pOrderUpdated);
                }
                break;
            case RTND:
                mvTicketImportService.restockReturnedItems(lvStorageId, lvOrderCode);
                if (isNeedRefund) {
                    //create ledger transaction record for export
                }
                break;
            case DLVD:
                mvLoyaltyProgramService.accumulatePoints(pOrderUpdated, pLoyaltyProgramId);
                break;
        }
    }

    @Override
    public String delete(Long id) {
        OrderDTO lvOrder = this.findById(id, true);
        if (lvOrder.getPaymentStatus()) {
            throw new DataInUseException(ErrorCode.ERROR_DATA_LOCKED.getDescription());
        }
        mvOrderRepository.deleteById(id);
        systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_ORD_D, MasterObject.Order, "Xóa đơn hàng", lvOrder.toString());
        logger.info("Xóa đơn hàng orderId={}", id);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }

    @Override
    public List<Order> findOrdersToday() {
        return mvOrderRepository.findOrdersToday();
    }

    @Override
    public Page<OrderDTO> getOrdersByCustomer(int pageSize, int pageNum, Long pCustomerId) {
        return this.findAll(pageSize, pageNum, null, null, null, null, null, null, pCustomerId, null, null, null, null, null, null);
    }

    private String getNextOrderCode() {
        int orderTodayQty = 0;
        List<Order> ordersToday = mvOrderRepository.findOrdersToday();
        if (ordersToday != null) {
            orderTodayQty = ordersToday.size();
        }
        LocalDate currentDate = LocalDate.now();
        String year = String.valueOf(currentDate.getYear());
        String month = String.format("%02d", currentDate.getMonthValue());
        String day = String.format("%02d", currentDate.getDayOfMonth());
        return year + month + day + String.format("%03d", orderTodayQty + 1);
    }

    private void sendNotifyCustomerOnOrderConfirmation(Order pOrderInfo) {
        String lvDestination = pOrderInfo.getReceiverEmail();
        if (pOrderInfo.getReceiverEmail() == null || pOrderInfo.getReceiverEmail().isBlank())
            return;

        Map<String, Object> lvNotificationParameter = new HashMap<>();
        lvNotificationParameter.put(NotificationType.SendNotifyCustomerOnOrderConfirmation.name(), lvDestination);
        lvNotificationParameter.put("customerName", pOrderInfo.getCustomer().getCustomerName());
        lvNotificationParameter.put("orderTime", pOrderInfo.getOrderTime().format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy")));
        lvNotificationParameter.put("deliveryAddress", pOrderInfo.getReceiverAddress());
        lvNotificationParameter.put("deliveryPhoneNumber", pOrderInfo.getReceiverPhone());
        lvNotificationParameter.put("deliveryEmail", pOrderInfo.getReceiverEmail());
        lvNotificationParameter.put("totalOrderValue", OrderUtils.calTotalAmount(pOrderInfo.getListOrderDetail(), pOrderInfo.getAmountDiscount()));

        StringBuilder lvRowsBuilder = new StringBuilder("");
        StringBuilder lvRowBuilder = new StringBuilder();
        int i = 1;
        for (OrderDetail lvItem : pOrderInfo.getListOrderDetail()) {
            ProductDetail lvProductDetail = lvItem.getProductDetail();
            BigDecimal lvUnitPrice = lvItem.getPrice();
            int lvQuantity = lvItem.getQuantity();
            lvRowBuilder.append("<tr>");
            lvRowBuilder.append("<td>").append(i++).append("</td>");
            lvRowBuilder.append("<td>").append(lvProductDetail.getVariantName()).append("</td>");
            lvRowBuilder.append("<td>").append(lvUnitPrice).append("</td>");
            lvRowBuilder.append("<td>").append(lvQuantity).append("</td>");
            lvRowBuilder.append("<td>").append(lvUnitPrice.multiply(new BigDecimal(lvQuantity))).append("</td>");
            lvRowBuilder.append("<td>").append(lvItem.getNote()).append("</td>");
            lvRowBuilder.append("</tr>");
            lvRowsBuilder.append(lvRowBuilder.toString());
            lvRowBuilder.setLength(0);
        }
        lvNotificationParameter.put("orderDetails", lvRowsBuilder.toString());

        mvMailMediaService.send(NotificationType.SendNotifyCustomerOnOrderConfirmation, lvNotificationParameter);
    }

    private OrderStatus getDefaultOrderStatus() {
        return OrderStatus.PEND;
    }

    public boolean isWithinReturnPeriod(LocalDateTime successfulDeliveryTime, LocalDateTime currentDay, int periodDays) {
        long daysBetween = ChronoUnit.DAYS.between(successfulDeliveryTime, currentDay);
        return daysBetween < periodDays;
    }
}