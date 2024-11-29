<!DOCTYPE html>
<html lang="en" xmlns:th="https://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Flowiee | Thông tin khách hàng</title>
    <div th:replace="header :: stylesheets">
        <!--Nhúng các file css, icon,...-->
    </div>
    <style rel="stylesheet">
        .table td,
        th {
            vertical-align: middle;
        }
    </style>
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">
    <div th:replace="header :: header"></div>

    <div th:replace="sidebar :: sidebar"></div>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper" style="padding-top: 10px; padding-bottom: 1px;">
        <!-- Main content -->
        <section class="content">
            <div class="container-fluid">
                <!-- Small boxes (Stat box) -->
                <div class="row">
                    <div class="col-12">
                        <div class="card card-light" style="min-height: 605px">
                            <div class="card-header">
                                <ul class="nav nav-tabs"
                                    id="custom-tabs-one-tab"
                                    role="tablist">
                                    <li class="nav-item">
                                        <a class="nav-link active"
                                           id="THONG_TIN_TAB" href="#THONG_TIN"
                                           data-toggle="pill" role="tab" aria-selected="true"
                                           style="font-weight: bold">Thông tin chung
                                        </a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-link"
                                           id="LICH_SU_MUA_HANG_TAB" href="#LICH_SU_MUA_HANG"
                                           data-toggle="pill" role="tab" aria-selected="true"
                                           style="font-weight: bold">Lịch sử mua hàng
                                        </a>
                                    </li>
                                </ul>
                            </div>
                            <!--END CARD HEADER-->
                            <!--START CARD BODY-->
                            <div class="card-body">
                                <div class="tab-content" id="tabContent">
                                    <div class="tab-pane fade show active" id="THONG_TIN" role="tabpanel"
                                         aria-labelledby="custom-tabs-one-home-tab">
                                        <div class="card">
                                            <div class="card-body">
                                                <div class="row mb-2">
                                                    <span class="col-sm-2"><b>Họ tên: </b></span>
                                                    <span class="col-sm-4">[[${customerDetail.customerName}]]</span>
                                                </div>
                                                <div class="row mb-2">
                                                    <span class="col-sm-2"><b>Giới tính: </b></span>
                                                    <span class="col-sm-4">[[${customerDetail.sex}]]</span>
                                                </div>
                                                <div class="row justify-content-between mb-2">
                                                    <span class="col-sm-2"><b>Thông tin liên hệ: </b></span>
                                                    <button class="btn btn-sm btn-success" type="button"
                                                            data-toggle="modal"
                                                            data-target="#modelAddNewContact">Thêm mới
                                                    </button>
                                                </div>
                                                <div class="row">
                                                    <table class="table table-bordered">
                                                        <thead>
                                                            <tr>
                                                                <th scope="col">STT</th>
                                                                <th scope="col">Loại</th>
                                                                <th>Nội dung</th>
                                                                <th>Sử dụng mặc định</th>
                                                                <th>Trạng thái</th>
                                                                <th>Ghi chú</th>
                                                                <th>Thao tác</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <tr th:each="c, index : ${listCustomerContact}">
                                                                <td th:text="${index.index + 1}"></td>
                                                                <td th:text="${c.code}"></td>
                                                                <td th:text="${c.value}"></td>
                                                                <td th:text="${c.isDefault}"></td>
                                                                <td th:text="${c.status}"></td>
                                                                <td th:text="${c.note}"></td>
                                                                <td>
                                                                    <button class="btn btn-primary btn-sm"
                                                                            data-toggle="modal"
                                                                            th:data-target="'#update-' + ${c.id}">
                                                                        <i class="fa-solid fa-pencil"></i>
                                                                    </button>
                                                                    <button class="btn btn-danger btn-sm"
                                                                            data-toggle="modal"
                                                                            th:data-target="'#delete-' + ${c.id}">
                                                                        <i class="fa-solid fa-trash"></i>
                                                                    </button>
                                                                    <!-- Modal update contact -->
                                                                    <div th:replace="pages/sales/customer/fragments/customer-fragments :: modalUpdateContact"></div>
                                                                    <!-- Modal delete contact -->
                                                                    <div th:replace="pages/sales/customer/fragments/customer-fragments :: modalDeleteContact"></div>
                                                                </td>
                                                            </tr>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="card">
                                            <div class="card-header">
                                                <h4 class="card-title"><b>Lịch sử giao dịch</b></h4>
                                            </div>
                                            <div class="card-body">
                                                <div class="row mb-2">
                                                    <span class="col-sm-6">Tổng số hóa đơn: [[${customerDetail.listOrder.size()}]]</span>
                                                    <span class="col-sm-6">Ngày bắt đầu mua hàng: 12/12/2022</span>
                                                </div>
                                                <div class="row">
                                                    <span class="col-sm-6">Tổng sản phẩm đã mua: 0</span>
                                                    <span class="col-sm-6">Ngày mua cuối cùng: 12/12/2022</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <!--END TAB THÔNG TIN ĐƠN HÀNG-->

                                    <!--START TAB LỊCH SỬ MUA HÀNG-->
                                    <div class="tab-pane fade" id="LICH_SU_MUA_HANG" role="tabpanel"
                                         aria-labelledby="custom-tabs-one-profile-tab">
                                        <div class="row">
                                            <div class="card-body table-responsive col-sm-12 p-0" style="height: 500px;">
                                                <table class="table table-bordered table-head-fixed text-nowrap">
                                                    <thead>
                                                        <tr>
                                                            <th>STT</th>
                                                            <th>Mã đơn hàng</th>
                                                            <th>Thời gian đặt hàng</th>
                                                            <th>Địa chỉ nhận hàng</th>
                                                            <th>Số tiền</th>
                                                            <th>Kênh</th>
                                                            <th>Trạng thái</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <tr th:each="donHang, index : ${listDonHang}">
                                                            <td th:text="${index.index + 1}"></td>
                                                            <td>
                                                                <a th:href="@{/order/{id}(id=${donHang.id})}"
                                                                   th:text="${donHang.code}"></a>
                                                            </td>
                                                            <td th:text="${donHang.orderTime}"></td>
                                                            <td th:text="${donHang.receiverAddress}"></td>
                                                            <td th:text="${donHang.totalAmountDiscount}"></td>
                                                            <td th:text="${donHang.salesChannelName}"></td>
                                                            <td th:text="${donHang.orderStatusName}"></td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                    <!--END TAB LỊCH SỬ MUA HÀNG-->
                                </div>
                            </div>
                            <!--END CARD BODY-->
                        </div>
                    </div>
                </div>
                <!-- Modal create contact -->
                <div th:replace="pages/sales/customer/fragments/customer-fragments :: modalAddNewContact"></div>
            </div>
        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->

    <div th:replace="footer :: footer"><!-- Nhúng các file JavaScript vào --></div>

    <!-- Control Sidebar -->
    <aside class="control-sidebar control-sidebar-dark"><!-- Control sidebar content goes here --></aside>

    <div th:replace="header :: scripts"><!-- Nhúng các file JavaScript vào --></div>

    <script>
        $(function () {
            //Date and time picker
            $('#reservationdatetime').datetimepicker({icons: {time: 'far fa-clock'}});
            //Timepicker
            $('#timepicker').datetimepicker({
                format: 'LT'
            })

            //Date range picker
            $('#reservation').daterangepicker()
        })

        let lvContactAddressProvinceFieldBefore = $('#contactAddressProvinceField').val();
        $('#contactAddressProvinceField').on("click", function (e) {
            if ($(this).val().split("_")[0] > 0) {
                if ($(this).val() !== lvContactAddressProvinceFieldBefore) {
                    loadDistricts($(this).val().split("_")[1]);
                    lvContactAddressProvinceFieldBefore = $(this).val();
                }
            } else {
                $('#contactAddressDistrictField').empty();
                $('#contactAddressCommuneField').empty();
            }
        });

        let lvContactAddressDistrictFieldBefore = $('#contactAddressDistrictField').val();
        $('#contactAddressDistrictField').on("click", function (e) {
            if ($(this).val().split("_")[0] > 0) {
                if ($(this).val() !== lvContactAddressDistrictFieldBefore) {
                    loadCommunes($(this).val().split("_")[1]);
                    lvContactAddressDistrictFieldBefore = $(this).val();
                }
            } else {
                $('#contactAddressCommuneField').empty();
            }
        });

        $('#contactAddressBlock').hide();
        $('#contactTypeField').on("click", function (e) {
            if ($(this).val() === "A") {
                $('#contactPhoneAndEmailBlock').hide();
                $('#contactAddressBlock').show();
                loadProvinces();
            } else {
                $('#contactPhoneAndEmailBlock').show();
                $('#contactAddressBlock').hide();
            }
        });

        function loadProvinces() {
            let lvSelectElement = $('#contactAddressProvinceField');
            lvSelectElement.empty();
            $.get(mvHostURLCallApi + '/category/province', function (response) {
                lvSelectElement.append('<option value="0">Chọn tỉnh thành</option>');
                if (response.status === "OK") {
                    $.each(response.data, function (index, d) {
                        lvSelectElement.append('<option value=' + d.id + '_' + d.code + '>' + d.name + '</option>');
                    });
                }
            }).fail(function () {
                showErrorModal("Could not connect to the server");
            });
        }

        function loadDistricts(provinceId) {
            let lvSelectElement = $('#contactAddressDistrictField');
            lvSelectElement.empty();
            $.get(mvHostURLCallApi + '/category/district', {parentId: provinceId}, function (response) {
                lvSelectElement.append('<option>Chọn quận huyện</option>');
                if (response.status === "OK") {
                    $.each(response.data, function (index, d) {
                        let optionElement = $('<option>', {
                            value: d.id + "_" + d.code,
                            text: d.name,
                            code: d.code
                        });
                        lvSelectElement.append(optionElement);
                    });
                }
            }).fail(function () {
                showErrorModal("Could not connect to the server");
            });
        }

        function loadCommunes(districtId) {
            let lvSelectElement = $('#contactAddressCommuneField');
            lvSelectElement.empty();
            $.get(mvHostURLCallApi + '/category/commune', {parentId: districtId}, function (response) {
                lvSelectElement.append('<option>Chọn phường xã</option>');
                if (response.status === "OK") {
                    $.each(response.data, function (index, d) {
                        let optionElement = $('<option>', {
                            value: d.id,
                            text: d.name,
                            code: d.code  // Thêm thuộc tính 'code' với giá trị từ d.code
                        });
                        lvSelectElement.append(optionElement);
                    });
                }
            }).fail(function () {
                showErrorModal("Could not connect to the server");
            });
        }
    </script>
</div>

</body>

</html>
