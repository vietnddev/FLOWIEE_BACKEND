package com.flowiee.app.model.storage;

import lombok.Data;

import java.util.List;

@Data
public class ReportLoaiTaiLieuPieChart {
    private List<String> tenLoaiTaiLieu;
    private List<Float> soLuongDangSuDung;
}