package com.flowiee.pms.utils.constants;

import lombok.Getter;

@Getter
public enum VoucherType {
    TEXT("Chữ"),
    NUMBER("Số"),
    BOTH("Chữ và số");
    private final String label;

    VoucherType(String label) {
        this.label = label;
    }
}