package com.flowiee.pms.model.payload;

import com.flowiee.pms.entity.sales.Items;
import lombok.*;

import java.util.List;

@Data
@ToString
public class CartItemsReq {
    private Long cartId;
    private List<Items> items;

//    @Data
//    @ToString
//    public class Item {
//        private Long productVariantId;
//        private Integer quantity;
//    }
}