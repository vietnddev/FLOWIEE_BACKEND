package com.flowiee.app.controller.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flowiee.app.dto.OrderDTO;
import com.flowiee.app.entity.*;
import com.flowiee.app.exception.BadRequestException;
import com.flowiee.app.utils.*;
import com.flowiee.app.base.BaseController;
import com.flowiee.app.service.CategoryService;
import com.flowiee.app.exception.NotFoundException;
import com.flowiee.app.service.*;
import com.flowiee.app.security.ValidateModuleProduct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(EndPointUtil.PRO_ORDER)
public class OrderUIController extends BaseController {
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final CustomerService customerService;
    private final CartService cartService;
    private final VoucherService voucherService;
    private final ValidateModuleProduct validateModuleProduct;
    private final TicketExportService ticketExportService;

    @Autowired
    public OrderUIController(OrderService orderService, CategoryService categoryService, CustomerService customerService, CartService cartService, VoucherService voucherService, ValidateModuleProduct validateModuleProduct, TicketExportService ticketExportService) {
        this.orderService = orderService;
        this.categoryService = categoryService;
        this.customerService = customerService;
        this.cartService = cartService;
        this.voucherService = voucherService;
        this.validateModuleProduct = validateModuleProduct;
        this.ticketExportService = ticketExportService;
    }

    @GetMapping
    public ModelAndView viewAllOrders() {
        validateModuleProduct.readOrder(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_ORDER);
        modelAndView.addObject("listKenhBanHang", categoryService.findSubCategory(AppConstants.CATEGORY.SALES_CHANNEL.getName(), null));
        modelAndView.addObject("listHinhThucThanhToan", categoryService.findSubCategory(AppConstants.CATEGORY.PAYMENT_METHOD.getName(), null));
        modelAndView.addObject("listKhachHang", customerService.findAllCustomer());
        modelAndView.addObject("listNhanVienBanHang", accountService.findAll());
        modelAndView.addObject("listTrangThaiDonHang", categoryService.findSubCategory(AppConstants.CATEGORY.ORDER_STATUS.getName(), null));
        return baseView(modelAndView);
    }

    @GetMapping("/{id}")
    public ModelAndView findDonHangDetail(@PathVariable("id") Integer orderId) throws JsonProcessingException {
        validateModuleProduct.readOrder(true);
        if (orderId <= 0 || orderService.findOrderById(orderId) == null) {
            throw new NotFoundException("Order not found!");
        }
        OrderDTO orderDetail = orderService.findOrderById(orderId);
        TicketExport ticketExportDetail = new TicketExport();
        if (orderDetail.getTicketExportId() != null) {
            ticketExportDetail = ticketExportService.findById(orderDetail.getTicketExportId());
        }
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_ORDER_DETAIL);
        modelAndView.addObject("orderDetailId", orderId);
        modelAndView.addObject("orderDetail", orderDetail);
        modelAndView.addObject("listOrderDetail", orderDetail.getListOrderDetailDTO());
        modelAndView.addObject("ticketExportDetail", ticketExportDetail);
        modelAndView.addObject("listHinhThucThanhToan", categoryService.findSubCategory(AppConstants.CATEGORY.PAYMENT_METHOD.getName(), null));
        modelAndView.addObject("listNhanVienBanHang", accountService.findAll());
        modelAndView.addObject("donHang", new Order());
        //modelAndView.addObject("donHangThanhToan", new OrderPay());
        return baseView(modelAndView);
    }

    @GetMapping("/ban-hang")
    public ModelAndView showPageBanHang() {
        validateModuleProduct.insertOrder(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_ORDER_SELL);
        List<OrderCart> orderCartCurrent = cartService.findCartByAccountId(CommonUtils.getCurrentAccountId());
        if (orderCartCurrent.isEmpty()) {
            OrderCart orderCart = new OrderCart();
            orderCart.setCreatedBy(CommonUtils.getCurrentAccountId());
            cartService.saveCart(orderCart);
        }

        List<OrderCart> listOrderCart = cartService.findCartByAccountId(CommonUtils.getCurrentAccountId());
        modelAndView.addObject("listCart", listOrderCart);

        double totalAmountWithoutDiscount = cartService.calTotalAmountWithoutDiscount(listOrderCart.get(0).getId());
        double amountDiscount = 0;
        double totalAmountDiscount = totalAmountWithoutDiscount - amountDiscount;
        modelAndView.addObject("totalAmountWithoutDiscount", totalAmountWithoutDiscount);
        //modelAndView.addObject("amountDiscount", amountDiscount);
        modelAndView.addObject("totalAmountDiscount", totalAmountDiscount);
        return baseView(modelAndView);
    }

    @PostMapping("/ban-hang/cart/item/add")
    public ModelAndView addItemsToCart(@RequestParam("cartId") Integer cartId,
                                       @RequestParam("bienTheSanPhamId") String[] bienTheSanPhamId) {
        validateModuleProduct.insertOrder(true);
        if (cartId <= 0 || cartService.findCartById(cartId) == null) {
            throw new NotFoundException("Cart not found!");
        }
        List<String> listProductVariantId = Arrays.stream(bienTheSanPhamId).toList();
        for (String productVariantId : listProductVariantId) {
            if (cartService.isItemExistsInCart(cartId, Integer.parseInt(productVariantId))) {
                Items items = cartService.findItemByCartAndProductVariant(cartId, Integer.parseInt(productVariantId));
                cartService.increaseItemQtyInCart(items.getId(), items.getSoLuong() + 1);
            } else {
                Items items = new Items();
                items.setOrderCart(new OrderCart(cartId));
                items.setProductVariant(new ProductVariant(Integer.parseInt(productVariantId)));
                items.setSoLuong(1);
                items.setGhiChu("");
                cartService.saveItem(items);
            }
        }
        return new ModelAndView("redirect:/don-hang/ban-hang");
    }

    @PostMapping("/ban-hang/cart/item/update/{itemId}")
    public ModelAndView updateItemsOfCart(@RequestParam("cartId") Integer cartId,
                                          @ModelAttribute("items") Items items,
                                          @PathVariable("itemId") Integer itemId) {
        validateModuleProduct.insertOrder(true);
        if (cartId <= 0 || cartService.findCartById(cartId) == null) {
            throw new NotFoundException("Cart not found!");
        }
        items.setId(itemId);
        items.setOrderCart(cartService.findCartById(cartId));
        if (items.getSoLuong() > 0) {
            cartService.saveItem(items);
        } else {
            cartService.deleteItem(items.getId());
        }
        return new ModelAndView("redirect:/don-hang/ban-hang");
    }

    @PostMapping("/ban-hang/cart/item/delete/{itemId}")
    public ModelAndView deleteItemsOfCart(@RequestParam("cartId") Integer cartId, @PathVariable("itemId") Integer itemId) {
        validateModuleProduct.insertOrder(true);
        if (cartService.findCartById(cartId) == null || cartService.findItemById(itemId) == null) {
            throw new BadRequestException("Sản phẩm cần xóa trong giỏ hàng không tồn tại! cartId=" + cartId + ", itemId=" + itemId);
        }
        cartService.deleteItem(itemId);
        return new ModelAndView("redirect:/don-hang/ban-hang");
    }
    
    @PostMapping("/ban-hang/cart/add-voucher/{code}")
    public ModelAndView checkToUse(@PathVariable("code") String code) {
        validateModuleProduct.readVoucher(true);
        ModelAndView modelAndView = new ModelAndView("redirect:/don-hang/ban-hang");
        modelAndView.addObject("ticket_code", code);
        modelAndView.addObject("ticket_status", voucherService.checkTicketToUse(code));
        modelAndView.addObject("ticket_info", voucherService.findTicketByCode(code));
        return modelAndView;
    }

    @PostMapping("/update/{id}")
    public ModelAndView update(@ModelAttribute("donHang") Order order, @PathVariable("id") Integer orderId) {
        validateModuleProduct.updateOrder(true);
        orderService.updateOrder(order, orderId);
        return new ModelAndView("redirect:/don-hang");
    }

    @PostMapping("/delete/{id}")
    public ModelAndView delete(@PathVariable("id") Integer orderId) {
        validateModuleProduct.deleteOrder(true);
        orderService.deleteOrder(orderId);
        return new ModelAndView("redirect:/don-hang");
    }

    @GetMapping("/abc")
    public ModelAndView getOrderInfoByScanQRCode() {
        return null;
    }
}