<!DOCTYPE html>
<html lang="en" xmlns:th="https://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tạo đơn</title>
    <th:block th:replace="header :: stylesheets"></th:block>
    <style rel="stylesheet">
        .table td, th {
            vertical-align: middle;
        }
        .modal-dialog.modal-xl {
            max-width: 1400px; /* Tăng chiều rộng của modal */
        }
        .modal-content {
            margin: auto; /* Canh giữa modal */
        }
        .modal-dialog {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh; /* Đảm bảo modal luôn căn giữa */
        }
    </style>
</head>
<body class="hold-transition sidebar-mini layout-fixed">
    <div class="wrapper">
        <div th:replace="header :: header"></div>

        <div th:replace="sidebar :: sidebar"></div>

        <!-- Content Wrapper. Contains page content -->
        <div class="content-wrapper" style="padding-top: 10px; padding-bottom: 1px">
            <!-- Main content -->
            <section class="content">
                <div class="container-fluid">
                    <!-- Small boxes (Stat box) -->
                    <div class="card" style="min-height: 605px">
                        <div class="card-body p-0" th:each="cart, cartIndex : ${listCart}">
                            <div class="row pl-1 pr-1">
                                <div class="col-9 p-0">
                                    <table class="table table-responsive table-head-fixed text-nowrap" style="height: 500px;" id="itemsTable">
                                        <thead>
                                            <tr>
                                                <th>#</th>
                                                <th class="text-left">Tên sản phẩm</th>
                                                <th>Giá bán</th>
                                                <th>Giảm thêm</th>
                                                <th>SL</th>
                                                <th>Thành tiền</th>
                                                <th>
                                                    <button class="btn btn-sm btn-primary w-100" id="btnAddItems">Chọn sản phẩm</button>
                                                </th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr th:each="item, itemIndex : ${cart.listItems}">
                                                <td th:text="${itemIndex.index + 1}"></td>
                                                <td class="text-left">
                                                    <input type="hidden" id="productVariantIdField" th:value="${item.productDetail.Id}"/>
                                                    <a th:text="${item.productDetail.variantName}"
                                                       th:href="@{/san-pham/variant/{id}(id=${item.productDetail.id})}"></a>
                                                    <p class="font-italic" th:text="${item.note}"></p>
                                                </td>
                                                <td th:text="${item.price != null}
                                                                ? ${#numbers.formatDecimal (item.price, 0, 'COMMA', 0, 'NONE')} + ' đ (' + ${item.priceType == 'L' ? 'Lẻ' : 'Sỉ'} + ')'
                                                                : '-'"></td>
                                                <td th:text="${item.extraDiscount != null}
                                                                ? ${#numbers.formatDecimal(item.extraDiscount, 0, 'COMMA', 0, 'NONE')} + ' đ'
                                                                : '-'"></td>
                                                <td th:text="${item.quantity}"></td>
                                                <td th:text="${item.price != null}
                                                                ? ${#numbers.formatDecimal(item.price * item.quantity - item.extraDiscount, 0, 'COMMA', 0, 'NONE')} + ' đ'
                                                                : '-'"></td>
                                                <td>
                                                    <button type="button" class="btn btn-sm btn-primary" data-toggle="modal" th:data-target="'#modalUpdateItems_' + ${item.id}">Cập nhật</button>
                                                    <button type="button" class="btn btn-sm btn-danger" data-toggle="modal" th:data-target="'#modalDeleteItems_' + ${item.id}">Xóa</button>
                                                    <!--Modal update item-->
                                                    <div th:replace="pages/sales/order/fragments/create-order-fragments :: modalUpdateItem"></div>
                                                    <!--Modal delete item-->
                                                    <div th:replace="pages/sales/order/fragments/create-order-fragments :: modalDeleteItem"></div>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="col-3 pl-3 pr-3">
                                    <div class="form-group row mt-3" style="padding-right: 8px">
                                        <label style="display: flex; align-items: center">Khách hàng</label>
                                        <select class="custom-select" id="customerField" required>
                                            <!--<option th:each="list : ${listCustomer}" th:value="${list.id}" th:text="${list.customerName}"></option>-->
                                        </select>
                                    </div>
                                    <div class="form-group row" style="padding-right: 8px">
                                        <label style="display: flex; align-items: center">Nhân viên bán hàng</label>
                                        <select class="custom-select" id="accountField" required>
                                            <option th:each="list : ${listAccount}" th:value="${list.id}" th:text="${list.fullName}"></option>
                                        </select>
                                    </div>
                                    <div class="form-group row" style="padding-right: 8px">
                                        <label style="display: flex; align-items: center">Thời gian đặt hàng</label>
                                        <div class="input-group date" id="reservationdatetime"
                                             data-target-input="nearest">
                                            <input type="text" class="form-control datetimepicker-input"
                                                   data-target="#reservationdatetime"
                                                   id="orderTimeField"
                                                   required/>
                                            <div class="input-group-append"
                                                 data-target="#reservationdatetime"
                                                 data-toggle="datetimepicker">
                                                <div class="input-group-text"><i class="fa fa-calendar"></i></div>
                                            </div>
                                        </div>
                                    </div>
                                    <!--KÊNH BÁN HÀNG-->
                                    <div class="form-group row" style="padding-right: 8px">
                                        <label style="display: flex; align-items: center">Kênh bán hàng</label>
                                        <select class="custom-select" id="salesChannelField" required>
                                            <option th:each="list : ${listSalesChannel}" th:value="${list.id}" th:text="${list.name}"></option>
                                        </select>
                                    </div>
                                    <!--KÊNH BÁN HÀNG-->
                                    <!--HÌNH THỨC THANH TOÁN-->
                                    <div class="form-group row" style="padding-right: 8px">
                                        <label style="display: flex; align-items: center">Hình thức thanh toán</label>
                                        <select class="custom-select" id="paymentMethodField" required>
                                            <option th:each="list : ${listPaymentMethod}" th:value="${list.id}" th:text="${list.name}"></option>
                                        </select>
                                    </div>
                                    <!--HÌNH THỨC THANH TOÁN-->
                                    <!--TRẠNG THÁI ĐƠN HÀNG-->
                                    <div class="form-group row" style="padding-right: 8px">
                                        <label style="display: flex; align-items: center">Trạng thái đơn hàng</label>
                                        <!--<select class="custom-select col-sm-6" id="orderStatusField" required>
                                            <option th:each="list : ${listOrderStatus}" th:value="${list.id}" th:text="${list.name}"></option>
                                        </select>-->
                                        <select class="custom-select" id="orderStatusField">
                                            <option th:each="d : ${orderStatusMap}" th:value="${d.key}" th:text="${d.value}"></option>
                                        </select>
                                    </div>
                                    <div class="form-group row">
                                        <label>
                                            Tổng tiền hàng
                                            <span class="badge badge-info" id="totalAmountWithoutDiscountField"
                                                  th:if="${cart.listItems.size() > 0}"
                                                  th:text="${cart.listItems.size()}"></span>
                                        </label>

                                        <span class="text-right"
                                              th:text="${#numbers.formatDecimal (totalAmountWithoutDiscount, 0, 'COMMA', 0, 'NONE')} + ' đ'"></span>
                                    </div>
                                    <hr class="w-75">
                                    <div class="form-group row">
                                        <label class="col-sm-6">Phí vận chuyển</label>
                                        <span class="col-sm-6 text-right">0</span>
                                    </div>
                                    <hr class="w-75">
                                    <div class="form-group row">
                                        <label class="col-sm-6">Khuyến mãi</label>
                                        <span class="col-sm-6 text-right" id="amountDiscountField" th:text="0"></span>
                                    </div>
                                    <hr class="w-75">
                                    <div class="form-group row">
                                        <label>Phải thu</label>
                                        <label class="text-right" id="totalAmountDiscountField"
                                               th:text="${#numbers.formatDecimal (totalAmountDiscount, 0, 'COMMA', 0, 'NONE')} + ' đ'"></label>
                                    </div>
                                    <hr class="w-75">
                                    <div class="form-group">
                                        <label>Ghi chú</label>
                                        <textarea class="form-control" id="noteFieldCart"></textarea>
                                    </div>
                                    <hr class="mt-0">
                                    <div class="form-group row">
                                        <!--Button TẠO ĐƠN HÀNG-->
                                        <div class="col-sm">
                                            <button type="button" class="btn btn-primary w-100 link-confirm" style="padding-right: 3px" th:cartId="${cart.id}" th:actionType="'create'">Tạo đơn</button>
                                        </div>
                                        <!--Button XÓA GIỎ HÀNG (Clear)-->
                                        <div class="col-sm">
                                            <button type="button" class="btn btn-danger w-100" style="padding-left: 3px;" data-toggle="modal" id="preDeleteDonHang" th:data-target="'#modalDeleteCart_' + ${cart.id}">Clear</button>
                                        </div>
                                        <!--Modal clear cart-->
                                        <div th:replace="pages/sales/order/fragments/create-order-fragments :: modalClearCart"></div>
                                    </div>
                                </div>
                            </div>


                                <div class="col-sm-9 row">


                                    <hr class="w-50 bg-info">

                                    <div class="row" style="margin-top: -15px">
                                        <div class="col-5 mt-3">
                                            <div class="row col-12">
                                                <label for="voucherCodeField">Voucher giảm giá</label>
                                                <div class="input-group" style="width: 80%">
                                                    <input type="text" class="form-control" id="voucherCodeField">
                                                    <span class="input-group-append"><button type="button" class="btn btn-info btn-flat" id="btnCheckVoucherIsAvailable">Kiểm tra</button></span>
                                                </div>
                                            </div>
                                            <div id="ticketInfoBlock">
                                                <span class="row col-12 mt-2" id="voucherTitleField"></span>
                                                <span class="row col-12 mt-2" id="voucherStatusField"></span>
                                                <span class="row col-12 mt-2" id="voucherPercentField"></span>
                                                <span class="row col-12 mt-2" id="voucherMaxPriceField"></span>
                                                <span class="row col-12 mt-2" id="voucherDoiTuongApDungField"></span>
                                                <div class="row col-12 mt-2 form-group" id="isUseVoucherBlock">
                                                    <div class="custom-control custom-checkbox">
                                                        <input class="custom-control-input" type="checkbox" id="isUseVoucherField">
                                                        <label for="isUseVoucherField" class="custom-control-label">Sử dụng</label>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-7 mt-3 p-0">
                                            <div class="row col-12 mt-2 text-center">
                                                <label class="col-sm-12">Thông tin người nhận</label>
                                            </div>
                                            <div class="row col-12 mt-2">
                                                <label class="col-sm-3" for="receiveNameField">Họ tên</label>
                                                <input class="col-sm-9 form-control" type="text" id="receiveNameField">
                                            </div>
                                            <div class="row col-12 mt-2">
                                                <label class="col-sm-3" for="receivePhoneNumberField">Số điện thoại</label>
                                                <input class="col-sm-9 form-control" type="text" id="receivePhoneNumberField">
                                            </div>
                                            <div class="row col-12 mt-2">
                                                <label class="col-sm-3" for="receiveEmailField">Email</label>
                                                <input class="col-sm-9 form-control" type="text" id="receiveEmailField">
                                            </div>
                                            <div class="row col-12 mt-2">
                                                <label class="col-sm-3" for="receiveAddressField">Địa chỉ</label>
                                                <textarea class="col-sm-9 form-control" type="text" id="receiveAddressField" rows="3"></textarea>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-sm-3 border">

                                </div>
                            </div>

                    </div>
                </div>
            </section>
        </div>
        <div th:replace="modal_fragments :: confirm_modal"></div>
        <div th:replace="pages/product/fragments/product-fragments :: searchProductModal"></div>

        <div th:replace="footer :: footer"><!-- Nhúng các file JavaScript vào --></div>

        <aside class="control-sidebar control-sidebar-dark"><!-- Control sidebar content goes here --></aside>

        <div th:replace="header :: scripts"><!-- Nhúng các file JavaScript vào --></div>

        <script th:src="@{/js/order/CreateNewOrder.js}"></script>
        <script th:src="@{/js/product/SearchProductModal.js}"></script>
    </div>

    <script type="text/javascript">
        setupSelectMultiple();

        let mvCartId = [[${listCart.get(0).id}]];
        let mvCustomers = {};
        let mvVoucherTicketDetail= {};
        let mvVoucherStatus = "NOK";
        let mvVoucherCode = "";
        let mvTotalAmountWithoutDiscount = [[${totalAmountWithoutDiscount}]];//$("#totalAmountWithoutDiscountField");
        let mvAmountDiscount = 0;// $("#amountDiscountField");
        let mvTotalAmountDiscount = [[${totalAmountWithoutDiscount}]];//$("#totalAmountDiscountField");
        $('#isUseVoucherBlock').hide();

        $(document).ready(function () {
            createListener();
            loadCustomers();
            loadReceiveInformationToForm();
            createOrder();
            checkVoucherIsAvailable();
            useVoucher();
        });

        function createListener() {
            $("#btnAddItems").on("click", function () {
                $("#searchProductModal").modal();
                setupSearchModalInCreateOrderPage();
            });

            $("#btnSubmitProductOnSearchModal").on("click", function () {
                submitProductOnSearchModal("createOrder");
                //$("#searchProductModal").modal("hide");
            });
        }

        async function loadCustomers() {
            let selectElement = $('#customerField');
            let apiURL = mvHostURLCallApi + '/customer/all'
            let response = await fetch(apiURL)
            if (response.ok) {
                let data = (await response.json()).data;
                selectElement.append('<option>Chọn khách hàng</option>');
                $.each(data, function (index, d) {
                    selectElement.append('<option value=' + d.id + '>' + d.customerName + '</option>');
                    mvCustomers[d.id] = d; //Tương tự map trong Java, d.id là key, d là value
                });
            } else {
                alert('Call API fail!')
            }
        }

        function loadReceiveInformationToForm() {
            $('#customerField').on('click', function () {
                $('#receiveNameField').val(mvCustomers[$(this).val()].customerName);
                $('#receivePhoneNumberField').val(mvCustomers[$(this).val()].phoneDefault);
                $('#receiveEmailField').val(mvCustomers[$(this).val()].emailDefault);
                $('#receiveAddressField').val(mvCustomers[$(this).val()].addressDefault);
            });
        }

        function checkVoucherIsAvailable() {
            $('#btnCheckVoucherIsAvailable').on('click', function () {
                $('#ticketInfoBlock').hide();
                let codeInput = $('#voucherCodeField').val();
                let apiURL = mvHostURLCallApi + '/voucher/check/' + codeInput;
                $.get(apiURL, function (response) {
                    if (response.status === "OK" && response.message === "OK") {
                        $('#ticketInfoBlock').show();
                        mvVoucherTicketDetail = response.data;
                        let isAvailable = mvVoucherTicketDetail.available;
                        let title = isAvailable === "Y" ? mvVoucherTicketDetail.voucherInfo.title : "";
                        let discountPercent = isAvailable === "Y" ? mvVoucherTicketDetail.voucherInfo.discount : "";
                        let maxPrice = isAvailable === "Y" ? formatCurrency(mvVoucherTicketDetail.voucherInfo.discountPriceMax) : "";
                        let applicableObjects = isAvailable === "Y" ? mvVoucherTicketDetail.voucherInfo.applicableObjects : "";
                        $('#voucherTitleField').text("Tên đợt khuyến mãi: " + title);
                        $('#voucherPercentField').text("Phần trăm giảm: " + discountPercent + " %");
                        $('#voucherMaxPriceField').text("Tối đa giảm được: " + maxPrice);
                        $('#voucherDoiTuongApDungField').text("Đối tượng áp dụng: " + applicableObjects);
                        if (isAvailable === "Y") {
                            mvVoucherStatus = "OK";
                            $('#voucherStatusField').text("Trạng thái: Khả dụng");
                            $('#isUseVoucherBlock').show();
                        } else {
                            mvVoucherStatus = "NOK";
                            $('#voucherStatusField').text("Trạng thái: Không khả dụng");
                            $('#isUseVoucherBlock').hide();
                        }
                        mvVoucherCode = codeInput;
                    }
                }).fail(function () {
                    showErrorModal("Could not connect to the server");//nếu ko gọi xuống được controller thì báo lỗi
                });
            });
        }

        function useVoucher() {
            $("#isUseVoucherField").on("change", function () {
                if($(this).is(':checked')) {
                    if (mvVoucherStatus === "OK") {
                        mvAmountDiscount = Math.round(mvTotalAmountWithoutDiscount * mvVoucherTicketDetail.voucherInfo.discount / 100);
                        if (mvAmountDiscount > mvVoucherTicketDetail.voucherInfo.discountPriceMax) {
                            mvAmountDiscount = mvVoucherTicketDetail.voucherInfo.discountPriceMax;
                        }
                        $("#amountDiscountField").text(formatCurrency(mvAmountDiscount));
                        mvTotalAmountDiscount = mvTotalAmountWithoutDiscount - mvAmountDiscount;
                        $("#totalAmountDiscountField").text(formatCurrency(mvTotalAmountDiscount));
                    }
                } else {
                    $("#amountDiscountField").text("0 đ");
                    $("#totalAmountDiscountField").text(formatCurrency(mvTotalAmountWithoutDiscount));
                }
            })
        }
    </script>
</body>
</html>