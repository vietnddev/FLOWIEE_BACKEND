package com.flowiee.app.service;

import com.flowiee.app.entity.Customer;
import com.flowiee.app.entity.Order;
import com.flowiee.app.model.product.DoanhThuCacNgayTheoThang;
import com.flowiee.app.model.product.DoanhThuCacThangTheoNam;
import com.flowiee.app.model.product.DoanhThuTheoKenhBanHang;
import com.flowiee.app.model.product.TopBestSeller;

import java.util.List;

public interface DashboardService {
    DoanhThuTheoKenhBanHang getDoanhThuTheoKenhBanHang();

    DoanhThuCacThangTheoNam getDoanhThuCacThangTheoNam();

    DoanhThuCacNgayTheoThang getDoanhThuCacNgayTheoThang();

    TopBestSeller getTopSanPhamBanChay();

    List<Order> getSoLuongDonHangHomNay();

    Float getDoanhThuHomNay();

    Float getDoanhThuThangNay();

    List<Customer> getSoLuongKhachHangMoi();
}