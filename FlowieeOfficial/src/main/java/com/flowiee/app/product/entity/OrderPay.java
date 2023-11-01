package com.flowiee.app.product.entity;

import com.flowiee.app.base.BaseEntity;
import com.flowiee.app.category.entity.HinhThucThanhToan;
import com.flowiee.app.system.entity.Account;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "don_hang_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPay extends BaseEntity implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_hang_id", nullable = false)
    private Order order;

    @Column(name = "ma_phieu", nullable = false)
    private String maPhieu;

    @Column(name = "thoi_gian_thanh_toan", nullable = false)
    private Date thoiGianThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hinh_thuc_thanh_toan", nullable = false)
    private HinhThucThanhToan hinhThucThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier", nullable = false)
    private Account thuNgan;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai_thanh_toan", nullable = false)
    private boolean trangThaiThanhToan;

    @Override
    public String toString() {
        return "DonHangThanhToan{" +
                "donHang=" + order +
                ", maPhieu='" + maPhieu + '\'' +
                ", thoiGianThanhToan=" + thoiGianThanhToan +
                ", hinhThucThanhToan=" + hinhThucThanhToan +
                ", thuNgan=" + thuNgan +
                ", ghiChu='" + ghiChu + '\'' +
                ", trangThaiThanhToan=" + trangThaiThanhToan +
                '}';
    }
}