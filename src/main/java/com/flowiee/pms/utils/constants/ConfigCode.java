package com.flowiee.pms.utils.constants;

import lombok.Getter;

@Getter
public enum ConfigCode {
    allowSellPriceLessThanCostPrice(MODULE.SALES, "Cho phép giá bán ra thấp hơn giá nhập", "Y"),
    allowDuplicateCustomerPhoneNumber(MODULE.SALES, "Cho phép trùng số điện thoại của khách hàng", "N"),
    daySendNotifyBeforeProductExpiry(MODULE.PRODUCT, "Thời gian gửi thông báo trước khi sản phẩm hết hạn sử dụng", "10"),
    dayDeleteSystemLog(MODULE.SYSTEM, "Thời gian xóa nhật ký hệ thống, các nhật ký có thời gian tạo từ >= ? ngày sẽ được xóa tự động", "100"),
    deleteSystemLog(MODULE.SYSTEM, "Xóa nhật ký hệ thống tự động", "N"),
    emailHost(MODULE.SYSTEM, "Email host", "smtp"),
    emailPort(MODULE.SYSTEM, "Email port", "587"),
    emailUser(MODULE.SYSTEM, "Email username", "vietnd.stg008@gmail.com"),
    emailPass(MODULE.SYSTEM, "Email password", "khpsawxcfkntzfbe"),
    extensionAllowedFileUpload(MODULE.SYSTEM, "Định dạng file được phép upload", null),
    failLogonCount(MODULE.SYSTEM, "Khóa tài khoản khi đăng nhập sai số lần", "5"),
    initData(MODULE.SYSTEM, "Initialize initial data for the system", "Y"),
    lowStockAlert(MODULE.PRODUCT, "Thông báo cảnh báo hàng tồn kho thấp", "N"),
    maxSizeFileUpload(MODULE.SYSTEM, "Dung lượng file tối đa cho phép upload", null),
    resourceUploadPath(MODULE.SYSTEM, "Thư mực chứa tệp upload", null),
    returnPeriodDays(MODULE.SALES, "Thời gian cho phép đổi trả hàng", "7"),
    sendEmailReportDaily(MODULE.SALES, "Gửi mail báo cáo hoạt động kinh doanh hàng ngày", "N"),
    sendNotifyCustomerOnOrderConfirmation(MODULE.SALES, "Gửi email thông báo đến khách hàng khi đơn hàng đã được xác nhận", "N"),
    sendNotifyAdminExceptionRuntime(MODULE.SYSTEM, "Gửi email thông báo đến Admin khi có sự cố hệ thống", "N"),
    shopAddress(MODULE.SYSTEM, "Địa chỉ", "Phường 7, Quận 8, Thành phố Hồ Chí Minh"),
    shopEmail(MODULE.SYSTEM, "Email", "nguyenducviet0684@gmail.com"),
    shopLogoUrl(MODULE.SYSTEM, "Logo", "src/main/resources/static/dist/img/FlowieeLogo.png"),
    shopName(MODULE.SYSTEM, "Tên cửa hàng", "Flowiee"),
    shopPhoneNumber(MODULE.SYSTEM, "Số điện thoại", "(+84) 706 820 684"),
    sysTimeOut(MODULE.SYSTEM, "Thời gian timeout", "3600"),
    tokenResetPasswordValidityPeriod(MODULE.SYSTEM, "Thông gian hiệu lực của token đổi mật khẩu (phút)", "60"),
    generateNewPasswordDefault(MODULE.SYSTEM, "Mật khẩu mặc định", "123456"),
    passwordLength(MODULE.SYSTEM, "Độ dài mật khẩu", "8"),
    passwordValidityPeriod(MODULE.SYSTEM, "Thời gian hiệu lực của mật khẩu (ngày)", "30");

    private MODULE module;
    private String description;
    private String defaultValue;

    ConfigCode(MODULE module, String description, String defaultValue) {
        this.module = module;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public static ConfigCode get(String pConfigCode) {
        try {
            return valueOf(pConfigCode);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}