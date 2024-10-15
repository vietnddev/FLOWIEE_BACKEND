package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.product.ProductDetail;
import com.flowiee.pms.entity.product.ProductPrice;
import com.flowiee.pms.entity.sales.Items;
import com.flowiee.pms.entity.sales.OrderCart;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.repository.product.ProductPriceRepository;
import com.flowiee.pms.utils.constants.*;
import com.flowiee.pms.model.dto.ProductVariantDTO;
import com.flowiee.pms.repository.sales.CartItemsRepository;
import com.flowiee.pms.repository.sales.OrderCartRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.service.sales.CartItemsService;
import com.flowiee.pms.service.sales.CartService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CartServiceImpl extends BaseService implements CartService {
    CartItemsService mvCartItemsService;
    OrderCartRepository mvCartRepository;
    CartItemsRepository mvCartItemsRepository;
    ProductVariantService mvProductVariantService;
    ProductPriceRepository mvProductPriceRepository;

    @Override
    public List<OrderCart> findCartByAccountId(Integer accountId) {
        List<OrderCart> listCart = mvCartRepository.findByAccountId(accountId);
        for (OrderCart cart : listCart) {
            for (Items item : cart.getListItems()) {
                ProductPrice itemPrice = mvProductPriceRepository.findPricePresent(null, item.getProductDetail().getId());
                if (itemPrice != null) {
                    PriceType priceType = PriceType.valueOf(item.getPriceType());
                    if (priceType.equals(PriceType.L)) {
                        item.setPriceOriginal(itemPrice.getRetailPrice());
                        item.setPrice(itemPrice.getRetailPriceDiscount());
                    }
                    if (priceType.equals(PriceType.S)) {
                        item.setPriceOriginal(itemPrice.getWholesalePrice());
                        item.setPrice(itemPrice.getWholesalePriceDiscount());
                    }
                }
                item.getProductDetail().setAvailableSalesQty(item.getProductDetail().getStorageQty() - item.getProductDetail().getDefectiveQty());
            }
        }
        return listCart;
    }

    @Override
    public List<OrderCart> findAll() {
        return mvCartRepository.findAll();
    }

    @Override
    public Optional<OrderCart> findById(Integer id) {
        return mvCartRepository.findById(id);
    }

    @Override
    public OrderCart save(OrderCart orderCart) {
        if (orderCart == null) {
            throw new BadRequestException();
        }
        return mvCartRepository.save(orderCart);
    }

    @Override
    public OrderCart update(OrderCart cart, Integer cartId) {
        if (this.findById(cartId).isEmpty()) {
            throw new BadRequestException();
        }
        cart.setId(cartId);
        return mvCartRepository.save(cart);
    }

    @Override
    public String delete(Integer cartId) {
        if (this.findById(cartId).isEmpty()) {
            throw new BadRequestException();
        }
        mvCartRepository.deleteById(cartId);
        systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_CART_C, MasterObject.Cart, "Xóa/Reset giỏ hàng", "cartId = " + cartId);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }

    @Override
    public Double calTotalAmountWithoutDiscount(int cartId) {
        return mvCartItemsRepository.calTotalAmountWithoutDiscount(cartId);
    }

    @Override
    public boolean isItemExistsInCart(Integer cartId, Integer productVariantId) {
        Items item = mvCartItemsRepository.findByCartAndProductVariant(cartId, productVariantId);
        return item != null;
    }

    @Transactional
    @Override
    public void resetCart(Integer cartId) {
        Optional<OrderCart> cartOptional = this.findById(cartId);
        cartOptional.ifPresent(orderCart -> mvCartItemsRepository.deleteAllItems(orderCart.getId()));
    }

    @Override
    public void addItemsToCart(Integer cartId, String[] productVariantIds) {
        List<String> listProductVariantId = Arrays.stream(productVariantIds).toList();
        for (String productVariantId : listProductVariantId) {
            Optional<ProductVariantDTO> productVariant = mvProductVariantService.findById(Integer.parseInt(productVariantId));
            if (productVariant.isEmpty()) {
                continue;
            }
            if (this.isItemExistsInCart(cartId, productVariant.get().getId())) {
                Items items = mvCartItemsService.findItemByCartAndProductVariant(cartId, productVariant.get().getId());
                mvCartItemsService.increaseItemQtyInCart(items.getId(), items.getQuantity() + 1);
            } else {
                ProductPrice productVariantPrice = mvProductPriceRepository.findPricePresent(null, Integer.parseInt(productVariantId));
                if (productVariantPrice == null) {
                    throw new AppException(String.format("Sản phẩm %s chưa được thiết lập giá bán!", productVariant.get().getVariantName()));
                }
                Items items = Items.builder()
                    .orderCart(new OrderCart(cartId))
                    .productDetail(new ProductDetail(Integer.parseInt(productVariantId)))
                    .priceType(PriceType.L.name())
                    .price(productVariantPrice.getRetailPriceDiscount() != null ? productVariantPrice.getRetailPriceDiscount() : productVariantPrice.getRetailPrice())
                    .priceOriginal(productVariantPrice.getRetailPrice())
                    .extraDiscount(BigDecimal.ZERO)
                    .quantity(1)
                    .note("")
                    .build();
                mvCartItemsService.save(items);
            }
        }
    }

    @Override
    public void updateItemsOfCart(Items itemToUpdate, Integer itemId) {
        Optional<Items> itemOptional = mvCartItemsService.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new BadRequestException("Item not found!");
        }
        Items item = itemOptional.get();
        if (itemToUpdate.getQuantity() <= 0) {
            mvCartItemsService.delete(item.getId());
        } else {
            ProductDetail productVariant = item.getProductDetail();
            if (itemToUpdate.getQuantity() > productVariant.getAvailableSalesQty()) {
                throw new BadRequestException("This item's quantity is over available quantity!");
            }
            ProductPrice productVariantPrice = mvProductPriceRepository.findPricePresent(null, productVariant.getId());
            item.setNote(itemToUpdate.getNote());
            item.setQuantity(itemToUpdate.getQuantity());
            if (itemToUpdate.getPriceType() != null && (!item.getPriceType().equals(itemToUpdate.getPriceType()))) {
                if (itemToUpdate.getPriceType().equals(PriceType.L.name())) {
                    item.setPrice(productVariantPrice.getRetailPriceDiscount());
                    item.setPriceOriginal(productVariantPrice.getRetailPrice());
                    item.setPriceType(PriceType.L.name());
                }
                if (itemToUpdate.getPriceType().equals(PriceType.S.name())) {
                    item.setPrice(productVariantPrice.getWholesalePriceDiscount());
                    item.setPriceOriginal(productVariantPrice.getWholesalePrice());
                    item.setPriceType(PriceType.S.name());
                }
            }
            if (itemToUpdate.getExtraDiscount() != null) {
                item.setExtraDiscount(itemToUpdate.getExtraDiscount());
            }
            mvCartItemsService.update(item, item.getId());
        }
    }
}