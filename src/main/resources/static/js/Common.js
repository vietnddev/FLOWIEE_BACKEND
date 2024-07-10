//Host
const mvHostURLCallApi = 'http://localhost:8085/api/v1';
const mvHostURL = 'http://localhost:8085';

//Language to use
let mvLang = "vi";

//User login info
const mvCurrentAccountId = '';
const mvCurrentAccountUsername = '';

//Config product status
const mvProductStatus = {};
mvProductStatus["A"] = "Đang kinh doanh";
mvProductStatus["I"] = "Ngừng kinh doanh";

//Config ticket import status
const mvTicketImportStatus = {};
mvTicketImportStatus["DRAFT"] = "Nháp";
mvTicketImportStatus["COMPLETED"] = "Hoàn thành";
mvTicketImportStatus["CANCEL"] = "Hủy";

//Config ticket export status
const mvTicketExportStatus = {};
mvTicketExportStatus["DRAFT"] = "Nháp";
mvTicketExportStatus["COMPLETED"] = "Hoàn thành";
mvTicketExportStatus["CANCEL"] = "Hủy";

//Pagination
const mvPageSizeDefault = 10;
function updatePaginationUI(pageNum, pageSize, totalPage, totalElements) {
    $('#paginationInfo').attr("pageNum", pageNum);
    $('#paginationInfo').attr("pageSize", pageSize);
    $('#paginationInfo').attr("totalPage", totalPage);
    $('#paginationInfo').attr("totalElements", totalElements);
    $('#pageNum').text(pageNum);

    let startCount = (pageNum - 1) * pageSize + 1;
    let endCount = startCount + pageSize - 1;
    if (endCount > totalElements) {
        endCount = totalElements;
    }
    $('#paginationInfo').text("Showing " + startCount + " to " + endCount + " of " + totalElements + " entries");

    $('#totalPages').text("Total pages " + totalPage);

    if(pageNum === 1) {
        $('#firstPage').attr("disable", "");
        $('#previousPage').attr("disable", "");
    }
}

function updatePaginationUI2(pageNum, pageSize, totalPage, totalElements) {
    $('#paginationInfo2').attr("pageNum", pageNum);
    $('#paginationInfo2').attr("pageSize", pageSize);
    $('#paginationInfo2').attr("totalPage", totalPage);
    $('#paginationInfo2').attr("totalElements", totalElements);
    $('#pageNum2').text(pageNum);

    let startCount = (pageNum - 1) * pageSize + 1;
    let endCount = startCount + pageSize - 1;
    if (endCount > totalElements) {
        endCount = totalElements;
    }
    $('#paginationInfo2').text("Showing " + startCount + " to " + endCount + " of " + totalElements + " entries");

    $('#totalPages2').text("Total pages " + totalPage);

    if(pageNum === 1) {
        $('#firstPage2').attr("disable", "");
        $('#previousPage2').attr("disable", "");
    }
}

function updatePaginationUI3(pageNum, pageSize, totalPage, totalElements) {
    $('#paginationInfo3').attr("pageNum", pageNum);
    $('#paginationInfo3').attr("pageSize", pageSize);
    $('#paginationInfo3').attr("totalPage", totalPage);
    $('#paginationInfo3').attr("totalElements", totalElements);
    $('#pageNum3').text(pageNum);

    let startCount = (pageNum - 1) * pageSize + 1;
    let endCount = startCount + pageSize - 1;
    if (endCount > totalElements) {
        endCount = totalElements;
    }
    $('#paginationInfo3').text("Showing " + startCount + " to " + endCount + " of " + totalElements + " entries");

    $('#totalPages3').text("Total pages " + totalPage);

    if(pageNum === 1) {
        $('#firstPage3').attr("disable", "");
        $('#previousPage3').attr("disable", "");
    }
}

function updateTableContentWhenOnClickPagination(loadNewDataMethod) {
    let lvPageSize = $('#selectPageSize').val();
    $('#selectPageSize').on('click', function() {
        console.log($(this).val())
        if (lvPageSize === $(this).val()) {
            return;
        }
        lvPageSize = $(this).val();
        loadNewDataMethod($(this).val(), 1);
    });

    $('#firstPage').on('click', function() {
        if (parseInt($('#paginationInfo').attr("pageNum")) === 1) {
            return;
        }
        loadNewDataMethod(lvPageSize, 1);
    });

    $('#previousPage').on('click', function() {
        if (parseInt($('#paginationInfo').attr("pageNum")) === 1) {
            return;
        }
        loadNewDataMethod(lvPageSize, $('#paginationInfo').attr("pageNum") - 1);
    });

    $('#nextPage').on('click', function() {
        if ($('#paginationInfo').attr("pageNum") === $('#paginationInfo').attr("totalPage")) {
            return;
        }
        loadNewDataMethod(lvPageSize, parseInt($('#paginationInfo').attr("pageNum")) + 1);
    });

    $('#lastPage').on('click', function() {
        if ($('#paginationInfo').attr("pageNum") === $('#paginationInfo').attr("totalPage")) {
            return;
        }
        loadNewDataMethod(lvPageSize, $('#paginationInfo').attr("totalPage"));
    });
}

function updateTableContentWhenOnClickPagination2(loadNewDataMethod) {
    let lvPageSize = $('#selectPageSize2').val();
    $('#selectPageSize2').on('click', function() {
        console.log($(this).val())
        if (lvPageSize === $(this).val()) {
            return;
        }
        lvPageSize = $(this).val();
        loadNewDataMethod($(this).val(), 1);
    });

    $('#firstPage2').on('click', function() {
        if (parseInt($('#paginationInfo2').attr("pageNum")) === 1) {
            return;
        }
        loadNewDataMethod(lvPageSize, 1);
    });

    $('#previousPage2').on('click', function() {
        if (parseInt($('#paginationInfo2').attr("pageNum")) === 1) {
            return;
        }
        loadNewDataMethod(lvPageSize, $('#paginationInfo2').attr("pageNum") - 1);
    });

    $('#nextPage2').on('click', function() {
        if ($('#paginationInfo2').attr("pageNum") === $('#paginationInfo2').attr("totalPage")) {
            return;
        }
        loadNewDataMethod(lvPageSize, parseInt($('#paginationInfo2').attr("pageNum")) + 1);
    });

    $('#lastPage2').on('click', function() {
        if ($('#paginationInfo2').attr("pageNum") === $('#paginationInfo2').attr("totalPage")) {
            return;
        }
        loadNewDataMethod(lvPageSize, $('#paginationInfo2').attr("totalPage"));
    });
}

function updateTableContentWhenOnClickPagination3(loadNewDataMethod) {
    let lvPageSize = $('#selectPageSize3').val();
    $('#selectPageSize3').on('click', function() {
        console.log($(this).val())
        if (lvPageSize === $(this).val()) {
            return;
        }
        lvPageSize = $(this).val();
        loadNewDataMethod($(this).val(), 1);
    });

    $('#firstPage3').on('click', function() {
        if (parseInt($('#paginationInfo3').attr("pageNum")) === 1) {
            return;
        }
        loadNewDataMethod(lvPageSize, 1);
    });

    $('#previousPage3').on('click', function() {
        if (parseInt($('#paginationInfo3').attr("pageNum")) === 1) {
            return;
        }
        loadNewDataMethod(lvPageSize, $('#paginationInfo3').attr("pageNum") - 1);
    });

    $('#nextPage3').on('click', function() {
        if ($('#paginationInfo3').attr("pageNum") === $('#paginationInfo3').attr("totalPage")) {
            return;
        }
        loadNewDataMethod(lvPageSize, parseInt($('#paginationInfo3').attr("pageNum")) + 1);
    });

    $('#lastPage3').on('click', function() {
        if ($('#paginationInfo3').attr("pageNum") === $('#paginationInfo3').attr("totalPage")) {
            return;
        }
        loadNewDataMethod(lvPageSize, $('#paginationInfo3').attr("totalPage"));
    });
}

//Search tool
function setupSearchTool(keySearch) {
    let brandFilter = $('#brandFilter');
    let productTypeFilter = $('#productTypeFilter');
    let colorFilter = $('#colorFilter');
    let sizeFilter = $('#sizeFilter');
    let unitFilter = $('#unitFilter');
    let discountFilter = $('#discountFilter');
    let productStatusFilter = $('#productStatusFilter');

    $("#btnOpenSearchAdvance").on("click", function () {
        brandFilter.empty();
        brandFilter.append("<option>Chọn nhãn hiệu</option>");
        productTypeFilter.empty();
        productTypeFilter.append("<option>Chọn loại sản phẩm</option>");
        colorFilter.empty();
        colorFilter.append("<option>Chọn màu sắc</option>");
        sizeFilter.empty();
        sizeFilter.append("<option>Chọn kích cỡ</option>");
        unitFilter.empty();
        unitFilter.append("<option>Chọn đơn vị tính</option>");
        discountFilter.empty();
        discountFilter.append("<option>Chọn khuyến mãi</option>");
        productStatusFilter.empty();
        productStatusFilter.append("<option>Chọn trạng thái sản phẩm</option>");

        $.each(keySearch, function (index, key) {
            if (key === "BRAND") {
                $.get(mvHostURLCallApi + '/category/brand', function (response) {
                    if (response.status === "OK") {
                        $.each(response.data, function (index, d) {
                            brandFilter.append('<option value=' + d.id + '>' + d.name + '</option>');
                            console.log(d)
                        });
                    }
                }).fail(function () {
                    showErrorModal("Could not connect to the server");
                });
            }
            if (key === "PRODUCT_TYPE") {
                $.get(mvHostURLCallApi + '/category/product-type', function (response) {
                    if (response.status === "OK") {
                        $.each(response.data, function (index, d) {
                            productTypeFilter.append('<option value=' + d.id + '>' + d.name + '</option>');
                            console.log(d)
                        });
                    }
                }).fail(function () {
                    showErrorModal("Could not connect to the server");
                });
            }
            if (key === "COLOR") {
                $.get(mvHostURLCallApi + '/category/color', function (response) {
                    if (response.status === "OK") {
                        $.each(response.data, function (index, d) {
                            colorFilter.append('<option value=' + d.id + '>' + d.name + '</option>');
                            console.log(d)
                        });
                    }
                }).fail(function () {
                    showErrorModal("Could not connect to the server");
                });
            }
            if (key === "SIZE") {
                $.get(mvHostURLCallApi + '/category/size', function (response) {
                    if (response.status === "OK") {
                        $.each(response.data, function (index, d) {
                            sizeFilter.append('<option value=' + d.id + '>' + d.name + '</option>');
                            console.log(d)
                        });
                    }
                }).fail(function () {
                    showErrorModal("Could not connect to the server");
                });
            }
            if (key === "UNIT") {
                $.get(mvHostURLCallApi + '/category/unit', function (response) {
                    if (response.status === "OK") {
                        $.each(response.data, function (index, d) {
                            unitFilter.append('<option value=' + d.id + '>' + d.name + '</option>');
                            console.log(d)
                        });
                    }
                }).fail(function () {
                    showErrorModal("Could not connect to the server");
                });
            }
            if (key === "DISCOUNT") {
                discountFilter.append(`<option value="Y">Đang áp dụng</option><option value="N">Không áp dụng</option>`);
            }
            if (key === "PRODUCT_STATUS") {
                productStatusFilter.append(`<option value="ACTIVE">Đang kinh doanh</option><option value="INACTIVE">Không kinh doanh</option>`);
            }
        })
    })
}

function loadFolderTree() {
    // Bắt sự kiện click cho các button có class bắt đầu bằng "nav-link folder-"
    $(document).on('click', 'a[class^="nav-link folder-"]', function() {
        if ($(this).attr("hasSubFolder") === "N") {
            return;
        }
        if ($(this).attr("collapse") === "Y") {
            return;
        }
        let aClass = $(this).attr('class');
        let folderId = aClass.split('-')[2];

        let subFolders = $('#sub-folders-' + folderId);
        subFolders.empty();

        $.get(mvHostURLCallApi + '/stg/doc/folders', {parentId: folderId}, function (response) {
            let subFoldersData = response.data;
            $.each(subFoldersData, function (index, d) {
                let iconDropdownList = ``;
                if (d.hasSubFolder === "Y") {
                    iconDropdownList = `<i class="fas fa-angle-left right"></i>`;
                }
                subFolders.append(`
                    <li class="nav-item">
                        <a href="#" class="nav-link folder-${d.id}" hasSubFolder="${d.hasSubFolder}" collapse="N">
                            <p>${d.name} ${iconDropdownList}</p>
                        </a>
                        <ul class="nav nav-treeview" id="sub-folders-${d.id}" style="margin-left: 15px"></ul>
                    </li>
                `);
            })
        }).fail(function () {
            showErrorModal("Could not connect to the server");
        });
        $(this).attr("collapse", "Y");
    });
}

let convertDateT1 = (dateInput) => {
    let dateObject = new Date(dateInput);
    let year = dateObject.getFullYear();
    let month = dateObject.getMonth() + 1; // Tháng (đánh số từ 0)
    let day = dateObject.getDate();
    // Format lại chuỗi ngày tháng thành 'dd/MM/yyyy'
    return formattedDate = (day < 10 ? '0' : '') + day + '/' + (month < 10 ? '0' : '') + month + '/' + year;
}

function setupSelectMultiple() {
    $(function () {
        //Initialize Select2 Elements
        $('.select2').select2()

        //Initialize Select2 Elements
        $('.select2bs4').select2({
            theme: 'bootstrap4'
        })

        //Bootstrap Duallistbox
        $('.duallistbox').bootstrapDualListbox()

        $("input[data-bootstrap-switch]").each(function () {
            $(this).bootstrapSwitch('state', $(this).prop('checked'));
        })

        //Date and time picker
        $('#reservationdatetime').datetimepicker({icons: {time: 'far fa-clock'}});
        //Timepicker
        $('#timepicker').datetimepicker({
            format: 'LT'
        })

        //Date range picker
        $('#reservation').daterangepicker()
    })
}