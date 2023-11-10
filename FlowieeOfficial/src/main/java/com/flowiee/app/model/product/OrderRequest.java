package com.flowiee.app.model.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class OrderRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<Integer> listBienTheSanPham;
    private Integer khachHang;
    private Integer kenhBanHang;
    private Integer hinhThucThanhToan;
    private String diaChiGiaoHang;
    private Integer nhanVienBanHang;
    private Date thoiGianDatHang;
    private Integer trangThaiDonHang;
    private Boolean trangThaiThanhToan;
    private String ghiChu;
    private String thoiGianDatHangSearch;
    private String searchTxt;
    private Integer cartId;
}