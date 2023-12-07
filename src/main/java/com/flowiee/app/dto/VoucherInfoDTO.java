package com.flowiee.app.dto;

import com.flowiee.app.entity.Product;
import com.flowiee.app.entity.VoucherInfo;
import com.flowiee.app.entity.VoucherTicket;
import com.flowiee.app.utils.AppConstants;
import com.flowiee.app.utils.CommonUtil;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
public class VoucherInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String title;
    private String description;
    private String doiTuongApDung;
    private String voucherType;
    private Integer quantity;
    private Integer lengthOfKey;
    private Integer discount;
    private Float maxPriceDiscount;
    private String startTime;
    private String endTime;
    private String status;
    private Date createdAt;
    private Integer createdBy;
    private List<VoucherTicket> listVoucherTicket;
    private List<Product> listSanPhamApDung;

    public static VoucherInfoDTO fromVoucherInfo(VoucherInfo voucherInfo) {
        try {
            VoucherInfoDTO voucherInfoDTO = new VoucherInfoDTO();
            voucherInfoDTO.setId(voucherInfo.getId());
            voucherInfoDTO.setTitle(voucherInfo.getTitle());
            voucherInfoDTO.setDescription(voucherInfo.getDescription());
            voucherInfoDTO.setDoiTuongApDung(voucherInfo.getDoiTuongApDung());
            voucherInfoDTO.setVoucherType(voucherInfo.getVoucherType());
            voucherInfoDTO.setQuantity(voucherInfo.getQuantity());
            voucherInfoDTO.setLengthOfKey(voucherInfo.getLengthOfKey());
            voucherInfoDTO.setDiscount(voucherInfo.getDiscount());
            voucherInfoDTO.setMaxPriceDiscount(voucherInfo.getMaxPriceDiscount());
            voucherInfoDTO.setStartTime(String.valueOf(voucherInfo.getStartTime()).substring(0, 10));
            voucherInfoDTO.setEndTime(String.valueOf(voucherInfo.getEndTime()).substring(0, 10));
            //Checking status
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = CommonUtil.convertStringToDate(String.valueOf(voucherInfo.getStartTime()).substring(0, 10), "yyyy-MM-dd");
            Date endDate = CommonUtil.convertStringToDate(String.valueOf(voucherInfo.getEndTime()).substring(0, 10), "yyyy-MM-dd");
            Date currentDate = CommonUtil.convertStringToDate(formatter.format(new Date()), "yyyy-MM-dd");
            if ((startDate.before(currentDate) || startDate.equals(currentDate)) && (endDate.after(currentDate) || endDate.equals(currentDate))) {
                voucherInfoDTO.setStatus(AppConstants.VOUCHER_STATUS.ACTIVE.getLabel());
            } else {
                voucherInfoDTO.setStatus(AppConstants.VOUCHER_STATUS.INACTIVE.getLabel());
            }
            voucherInfoDTO.setListVoucherTicket(voucherInfo.getListVoucherTicket());

            return voucherInfoDTO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}