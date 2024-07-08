<!DOCTYPE html>
<html lang="en" xmlns:th="https://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh sách đơn hàng</title>
    <div th:replace="header :: stylesheets"></div>
    <style rel="stylesheet">
        .table td, th {
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
                        <div class="col-sm-12">
                            <!--Search tool-->
                            <div th:replace="fragments :: searchTool(${configSearchTool})" id="searchTool"></div>

                            <div class="card">
                                <div class="card-body align-items-center p-0">
                                    <table class="table table-bordered table-striped align-items-center">
                                        <thead class="align-self-center">
                                            <tr class="align-self-center">
                                                <th>STT</th>
                                                <th>Mã đơn</th>
                                                <th>Thời gian mua</th>
                                                <th>Địa chỉ giao</th>
                                                <th>Tên khách</th>
                                                <th>SĐT nhận hàng</th>
                                                <th>Số tiền</th>
                                                <th>Kênh bán hàng</th>
                                                <th>Thanh toán</th>
                                                <th>Trạng thái</th>
                                                <th>Thao tác</th>
                                            </tr>
                                        </thead>
                                        <tbody id="contentTable"></tbody>
                                    </table>
                                </div>
                                <div class="card-footer">
                                    <div th:replace="fragments :: pagination"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- /.card-body -->
            </section>
            <!-- /.content -->
        </div>
        <!-- /.content-wrapper -->

        <div th:replace="footer :: footer"></div>

        <aside class="control-sidebar control-sidebar-dark"></aside>

        <div th:replace="header :: scripts"></div>

        <script th:src="@{/js/order/LoadOrders.js}"></script>

        <script>
            $(document).ready(function () {
                setupSearchTool();

                searchOrders();
                loadOrders(mvPageSizeDefault, 1);
                updateTableContentWhenOnClickPagination(loadOrders);
                printReport();
            });

            function searchOrders() {
                $("#btnSearch").on("click", function () {
                    loadOrders(mvPageSizeDefault, 1);
                })
            }

            function printReport() {
                $(document).on("click", ".btn-print-invoice", function (e) {
                    e.preventDefault();
                    window.open(mvHostURL + "/order/print-invoice/" + parseInt($(this).attr("orderId")), "_blank");
                })
            }

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
        </script>
    </div>
</body>
</html>
