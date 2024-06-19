package com.flowiee.pms.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopInfo {
    String name;
    String email;
    String phoneNumber;
    String address;
    String description;
    String returnPolicy;
    String refundPolicy;
    String logoUrl;
}