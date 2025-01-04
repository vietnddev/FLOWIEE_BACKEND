package com.flowiee.pms.utils;

import com.flowiee.pms.config.Core;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.utils.constants.ErrorCode;
import com.flowiee.pms.utils.constants.FileExtension;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FileUtils {
    public static String resourceStaticPath = "src/main/resources/static";
    public static String fileUploadPath = Core.getResourceUploadPath() + "/uploads/";
    public static String initCsvDataPath = Core.getResourceUploadPath() + "/data/csv";
    public static String reportTemplatePath = resourceStaticPath + "/report";
    public static String excelTemplatePath = resourceStaticPath + "/templates/excel";
    public static Path logoPath = Paths.get(resourceStaticPath + "/dist/img/FlowieeLogo.png");

    public static void createCellCombobox(XSSFWorkbook workbook, XSSFSheet sheet, XSSFSheet hsheet, List<String> listValue, int row, int column, String nameName) {
        //Put các tên danh mục vào column trong sheet danh mục ẩn
        for (int i = 0; i < listValue.size(); i++) {
            XSSFRow hideRow = hsheet.getRow(i);
            if (hideRow == null) {
                hideRow = hsheet.createRow(i);
            }
            hideRow.createCell(column).setCellValue(listValue.get(i));
        }

        // Khởi tạo name cho mỗi loại danh mục
        Name namedRange = workbook.createName();
        namedRange.setNameName(nameName);
        String colName = CellReference.convertNumToColString(column);
        namedRange.setRefersToFormula(hsheet.getSheetName() + "!$" + colName + "$1:$" + colName + "$" + listValue.size());

        sheet.autoSizeColumn(column); //Auto điều chỉnh độ rộng cột

        DataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
        CellRangeAddressList addressList = new CellRangeAddressList(row, row, column, column); //Tạo dropdownlist cho một cell
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(nameName);
        DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);

        dataValidation.setSuppressDropDownArrow(true); //Hiển thị mũi tên xổ xuống để chọn giá trị
        dataValidation.setShowErrorBox(true); //Hiển thị hộp thoại lỗi khi chọn giá trị không hợp lệ
        dataValidation.createErrorBox("Error", "Giá trị đã chọn không hợp lệ!");
        dataValidation.setEmptyCellAllowed(false); //Không cho phép ô trống trong dropdownlist
        dataValidation.setShowPromptBox(true); //Hiển thị hộp nhắc nhở khi người dùng chọn ô
        dataValidation.createPromptBox("Danh mục hệ thống", "Vui lòng chọn giá trị!"); //Tạo hộp thoại nhắc nhở khi click chuột vào cell

        sheet.addValidationData(dataValidation);
    }

    private List<String> getOnlineFile(String pQueryURL, int pConnectTimeOut, int pReadTimeOut, String pProxyIP, int pProxyPort) throws Exception
    {
        Proxy lvProxy = null;
        if(pProxyIP != null && pProxyPort > 0)
            lvProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(pProxyIP, pProxyPort));

        List<String> lvOutput = new LinkedList<>();
        URL lvURL = new URL(pQueryURL);
        URLConnection lvURLConnection = lvProxy == null? lvURL.openConnection() : lvURL.openConnection(lvProxy);
        lvURLConnection.setConnectTimeout(pConnectTimeOut);
        lvURLConnection.setReadTimeout(pReadTimeOut);
        BufferedReader lvReader = new BufferedReader(new InputStreamReader(lvURLConnection.getInputStream()));
        String lvReadLine;
        while ((lvReadLine = lvReader.readLine()) != null)
        {
            lvOutput.add(lvReadLine);
        }

        return lvOutput;
    }

    public static File getFileDataCategoryInit() {
        return Paths.get(initCsvDataPath + "/Category.csv").toFile();
    }

    public static String getFileExtension(String fileName) {
        String extension = "";
        if (ObjectUtils.isNotEmpty(fileName)) {
            int lastIndex = fileName.lastIndexOf('.');
            if (lastIndex > 0 && lastIndex < fileName.length() - 1) {
                extension = fileName.substring(lastIndex + 1);
            }
        }
        return extension;
    }

    public static String genRandomFileName() {
        return UUID.randomUUID().toString();
    }

    public static String getFileUploadPath() {
        if (Core.getResourceUploadPath() == null) {
            throw new AppException("The uploaded file saving directory is not configured, please try again later!");
        }
        return Core.getResourceUploadPath() + "/uploads/";
    }

    public static String getImageTempPath() {
        if (Core.getResourceUploadPath() == null) {
            throw new AppException("The uploaded file saving directory is not configured, please try again later!");
        }
        return Core.getResourceUploadPath() + "/data-temp/";
    }

    public static boolean isAllowUpload(String fileExtension, boolean throwException, String message) {
        if (ObjectUtils.isNotEmpty(fileExtension)) {
            for (FileExtension ext : FileExtension.values()) {
                if (ext.key().equalsIgnoreCase(fileExtension) && ext.isAllowUpload()) {
                    return true;
                }
            }
        }
        if (throwException) {
            throw new AppException(ErrorCode.FileDoesNotAllowUpload, new Object[]{fileExtension}, message, null, null);
        }
        return false;
    }

    public static String getImageUrl(FileStorage imageModel, boolean addLeadingSlash) {
        if (imageModel == null) {
            return null;
        }
        String imageUrl = imageModel.getDirectoryPath() + "/" + imageModel.getStorageName();
        return addLeadingSlash ? "/" + imageUrl : imageUrl;
    }
}