package com.flowiee.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class VoucherApplyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer voucherApplyId;
    private Integer voucherInfoId;
    private String voucherInfoTitle;
    private Integer productId;
    private String productName;
    private String appliedAt;
    private Integer appliedBy;
}