package com.flowiee.pms.utils.constants;

import lombok.Getter;

@Getter
public enum ConfigCode {
    allowSellPriceLessThanCostPrice("Cho phép giá bán ra thấp hơn giá nhập", "Y"),
    allowDuplicateCustomerPhoneNumber( "Cho phép trùng số điện thoại của khách hàng", "N"),
    daySendNotifyBeforeProductExpiry("Thời gian gửi thông báo trước khi sản phẩm hết hạn sử dụng", "10"),
    dayDeleteSystemLog("Thời gian xóa nhật ký hệ thống, các nhật ký có thời gian tạo từ >= ? ngày sẽ được xóa tự động", "100"),
    deleteSystemLog("Xóa nhật ký hệ thống tự động", "N"),
    emailHost("Email host", "smtp"),
    emailPort("Email port", "587"),
    emailUser("Email username", "vietnd.stg008@gmail.com"),
    emailPass("Email password", "khpsawxcfkntzfbe"),
    extensionAllowedFileUpload("Định dạng file được phép upload", null),
    failLogonCount("Khóa tài khoản khi đăng nhập sai số lần", "5"),
    initData("Initialize initial data for the system", "Y"),
    lowStockAlert("Thông báo cảnh báo hàng tồn kho thấp", "N"),
    maxSizeFileUpload("Dung lượng file tối đa cho phép upload", null),
    //pathFileUpload(null, null),
    resourceUploadPath("Thư mực chứa tệp upload", null),
    returnPeriodDays("Thời gian cho phép đổi trả hàng", "7"),
    sendEmailReportDaily("Gửi mail báo cáo hoạt động kinh doanh hàng ngày", "N"),
    sendNotifyCustomerOnOrderConfirmation("Gửi email thông báo đến khách hàng khi đơn hàng đã được xác nhận", "N"),
    sendNotifyAdminExceptionRuntime("Gửi email thông báo đến Admin khi có sự cố hệ thống", "N"),
    shopAddress("Địa chỉ", "Phường 7, Quận 8, Thành phố Hồ Chí Minh"),
    shopEmail("Email", "nguyenducviet0684@gmail.com"),
    shopLogoUrl("Logo", "src/main/resources/static/dist/img/FlowieeLogo.png"),
    shopName("Tên cửa hàng", "Flowiee"),
    shopPhoneNumber("Số điện thoại", "(+84) 706 820 684"),
    sysTimeOut("Thời gian timeout", "3600"),
    tokenResetPasswordValidityPeriod("Thông gian hiệu lực của token đổi mật khẩu (phút)", "60"),
    generateNewPasswordDefault("Mật khẩu mặc định", "123456"),
    passwordLength("Độ dài mật khẩu", "8"),
    passwordValidityPeriod("Thời gian hiệu lực của mật khẩu (ngày)", "30");

    private String description;
    private String defaultValue;

    ConfigCode(String description, String defaultValue) {
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