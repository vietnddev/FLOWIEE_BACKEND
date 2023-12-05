package com.flowiee.app.service;

import com.flowiee.app.entity.VoucherInfo;
import com.flowiee.app.dto.VoucherInfoDTO;

import java.util.List;

public interface VoucherService {
    List<VoucherInfoDTO> findAll();

    VoucherInfo findById(Integer voucherId);

    String save(VoucherInfo voucherInfo, List<Integer> listSanPhamApDung);

    String update(VoucherInfo voucherInfo, Integer voucherId);

    String detele(Integer voucherId);
}