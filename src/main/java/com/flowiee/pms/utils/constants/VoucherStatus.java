package com.flowiee.pms.utils.constants;

import lombok.Getter;

@Getter
public enum VoucherStatus {
    A("Đang áp dụng"),
    I("Đang không áp dụng");
    private final String label;

    VoucherStatus(String label) {
        this.label = label;
    }
}