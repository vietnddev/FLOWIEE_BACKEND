package com.flowiee.app.service.impl;

import com.flowiee.app.entity.Items;
import com.flowiee.app.entity.OrderCart;
import com.flowiee.app.entity.Price;
import com.flowiee.app.entity.SystemLog;
import com.flowiee.app.exception.BadRequestException;
import com.flowiee.app.repository.ItemsRepository;
import com.flowiee.app.repository.OrderCartRepository;
import com.flowiee.app.service.CartService;
import com.flowiee.app.service.PriceService;
import com.flowiee.app.service.SystemLogService;

import com.flowiee.app.utils.AppConstants;
import com.flowiee.app.utils.CommonUtils;
import com.flowiee.app.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private OrderCartRepository cartRepository;
    @Autowired
    private ItemsRepository itemsRepository;
    @Autowired
    private SystemLogService systemLogService;
    @Autowired
    private PriceService priceService;

    @Override
    public List<OrderCart> findAllCarts() {
        return cartRepository.findAll();
    }

    @Override
    public List<OrderCart> findCartByAccountId(Integer accountId) {
        List<OrderCart> listCart = cartRepository.findByAccountId(accountId);
        for (OrderCart cart : listCart) {
            for (Items item : cart.getListItems()) {
                BigDecimal price = new BigDecimal("0");
                Price priceEntity = priceService.findGiaHienTai(item.getProductVariant().getId());
                if (priceEntity != null) {
                    price = priceEntity.getGiaBan();
                }
                item.setPrice(price);
            }
        }
        return listCart;
    }

    @Override
    public List<Items> findItemsByCartId(Integer cartId) {
        return itemsRepository.findByCartId(cartId);
    }

    @Override
    public OrderCart findCartById(Integer id) {
        return cartRepository.findById(id).orElse(null);
    }

    @Override
    public Items findItemById(Integer itemId) {
        return itemsRepository.findById(itemId).orElse(null);
    }

    @Override
    public Items findItemByCartAndProductVariant(Integer cartId, Integer productVariantId) {
        return itemsRepository.findByCartAndProductVariant(cartId, productVariantId);
    }

    @Override
    public OrderCart saveCart(OrderCart orderCart) {
        if (orderCart == null) {
            throw new BadRequestException();
        }
        return cartRepository.save(orderCart);
    }

    @Override
    public OrderCart updateCart(OrderCart cart, Integer cartId) {
        cart.setId(cartId);
        return cartRepository.save(cart);
    }

    @Override
    public String deleteCart(Integer id) {
        cartRepository.deleteById(id);
        SystemLog systemLog = new SystemLog();
        systemLog.setModule(AppConstants.SYSTEM_MODULE.PRODUCT.name());
        systemLog.setAction("DELETE_CART");
        systemLog.setCreatedBy(CommonUtils.getCurrentAccountId());
        systemLog.setIp(CommonUtils.getCurrentAccountIp());
        systemLog.setNoiDung("DELETE CART");
        systemLogService.writeLog(systemLog);
        return "OK";
    }

    @Override
    public Items saveItem(Items items) {
        if (items == null) {
            throw new BadRequestException();
        }
        return itemsRepository.save(items);
    }

    @Override
    public Items updateItem(Items entity, Integer entityId) {
        if (entity == null || entityId == null || entityId <= 0) {
            throw new BadRequestException();
        }
        entity.setId(entityId);
        return itemsRepository.save(entity);
    }

    @Override
    public String deleteItem(Integer itemId) {
        if (itemId == null || itemId <= 0) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        Items items = this.findItemById(itemId);
        if (items == null) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        itemsRepository.deleteById(itemId);
        return MessageUtils.DELETE_SUCCESS;
    }

    @Override
    public Integer findSoLuongByBienTheSanPhamId(Integer cartId, Integer productVariantId) {
        return itemsRepository.findSoLuongByBienTheSanPhamId(cartId, productVariantId);
    }

    @Override
    public Double calTotalAmountWithoutDiscount(int cartId) {
        return itemsRepository.calTotalAmountWithoutDiscount(cartId);
    }

    @Override
    public boolean isItemExistsInCart(Integer cartId, Integer productVariantId) {
        Items item = itemsRepository.findByCartAndProductVariant(cartId, productVariantId);
        return item != null;
    }

    @Transactional
    @Override
    public void increaseItemQtyInCart(Integer itemId, int quantity) {
        itemsRepository.updateItemQty(itemId, quantity);
    }

    @Transactional
    @Override
    public void deleteAllItems() {
        itemsRepository.deleteAllItems();
    }
}