package com.flowiee.pms.service.category.impl;

import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.category.CategoryHistory;
import com.flowiee.pms.entity.product.Material;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.entity.system.FileStorage;
import com.flowiee.pms.entity.system.FlowieeImport;
import com.flowiee.pms.entity.system.Notification;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.exception.DataInUseException;
import com.flowiee.pms.model.MODULE;
import com.flowiee.pms.model.dto.ProductDTO;
import com.flowiee.pms.repository.category.CategoryHistoryRepository;
import com.flowiee.pms.repository.category.CategoryRepository;
import com.flowiee.pms.repository.system.AppImportRepository;
import com.flowiee.pms.repository.system.FileStorageRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.category.CategoryService;
import com.flowiee.pms.service.product.ProductInfoService;
import com.flowiee.pms.service.product.MaterialService;
import com.flowiee.pms.service.system.FileStorageService;
import com.flowiee.pms.service.system.ImportService;
import com.flowiee.pms.service.system.NotificationService;
import com.flowiee.pms.utils.*;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl extends BaseService implements CategoryService {
    private static final String mvModule = MODULE.CATEGORY.name();

    private final CategoryRepository categoryRepo;
    private final CategoryHistoryRepository categoryHistoryRepo;
    private final ProductInfoService productInfoService;
    private final NotificationService notificationService;
    private final ImportService importService;
    private final AppImportRepository appImportRepo;
    private final FileStorageService fileStorageService;
    private final FileStorageRepository fileStorageRepo;
    private final MaterialService materialService;

    @Autowired
    public CategoryServiceImpl(ImportService importService, CategoryRepository categoryRepo, ProductInfoService productInfoService,
                               MaterialService materialService, NotificationService notificationService, AppImportRepository appImportRepo,
                               FileStorageService fileStorageService, FileStorageRepository fileStorageRepo, CategoryHistoryRepository categoryHistoryRepo) {
        this.importService = importService;
        this.categoryRepo = categoryRepo;
        this.productInfoService = productInfoService;
        this.materialService = materialService;
        this.notificationService = notificationService;
        this.appImportRepo = appImportRepo;
        this.fileStorageService = fileStorageService;
        this.fileStorageRepo = fileStorageRepo;
        this.categoryHistoryRepo = categoryHistoryRepo;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepo.findAll();
    }

    @Override
    public Optional<Category> findById(Integer entityId) {
        return categoryRepo.findById(entityId);
    }

    @Override
    public Category save(Category entity) {
        if (entity == null) {
            throw new BadRequestException();
        }
        return categoryRepo.save(entity);
    }

    @Transactional
    @Override
    public Category update(Category entity, Integer entityId) {
        Optional<Category> categoryBefore = this.findById(entityId);
        if (categoryBefore.isEmpty()) {
            throw new BadRequestException();
        }
        categoryBefore.get().compareTo(entity).forEach((key, value) -> {
            CategoryHistory categoryHistory = new CategoryHistory();
            categoryHistory.setTitle("Cập nhật danh mục " + categoryBefore.get().getType());
            categoryHistory.setCategory(new Category(categoryBefore.get().getId(), null));
            categoryHistory.setField(key);
            categoryHistory.setOldValue(value.substring(0, value.indexOf("#")));
            categoryHistory.setNewValue(value.substring(value.indexOf("#") + 1));
            categoryHistoryRepo.save(categoryHistory);
        });
        entity.setId(entityId);
        return categoryRepo.save(entity);
    }

    @Transactional
    @Override
    public String delete(Integer entityId) {
        if (entityId == null || this.findById(entityId).isEmpty()) {
            throw new BadRequestException();
        }
        if (categoryInUse(entityId)) {
            throw new DataInUseException(MessageUtils.ERROR_DATA_LOCKED);
        }
        categoryHistoryRepo.deleteAllByCategory(entityId);
        categoryRepo.deleteById(entityId);
        return MessageUtils.DELETE_SUCCESS;
    }

    @Override
    public List<Category> findRootCategory() {
        List<Category> roots = categoryRepo.findRootCategory();
        List<Object[]> recordsOfEachType = categoryRepo.totalRecordsOfEachType();
        for (Category c : roots) {
            for (Object[] o : recordsOfEachType) {
                if (c.getType().equals(o[0])) {
                    c.setTotalSubRecords(Integer.parseInt(String.valueOf(o[1])));
                    break;
                }
            }
        }
        return roots;
    }

    @Override
    public List<Category> findSubCategory(String categoryType, Integer parentId) {
        return categoryRepo.findSubCategory(categoryType, parentId, Pageable.unpaged()).getContent();
    }

    @Override
    public Page<Category> findSubCategory(String categoryType, Integer parentId, int pageSize, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        return categoryRepo.findSubCategory(categoryType, parentId, pageable);
    }

    @Override
    public List<Category> findUnits() {
        return categoryRepo.findSubCategory(AppConstants.CATEGORY.UNIT.name(), null, Pageable.unpaged()).getContent();
    }

    @Override
    public List<Category> findColors() {
        return categoryRepo.findSubCategory(AppConstants.CATEGORY.COLOR.name(), null, Pageable.unpaged()).getContent();
    }

    @Override
    public List<Category> findSizes() {
        return categoryRepo.findSubCategory(AppConstants.CATEGORY.SIZE.name(), null, Pageable.unpaged()).getContent();
    }

    @Override
    public List<Category> findSalesChannels() {
        return categoryRepo.findSubCategory(AppConstants.CATEGORY.SALES_CHANNEL.name(), null, Pageable.unpaged()).getContent();
    }

    @Override
    public List<Category> findPaymentMethods() {
        return categoryRepo.findSubCategory(AppConstants.CATEGORY.PAYMENT_METHOD.name(), null, Pageable.unpaged()).getContent();
    }

    @Override
    public List<Category> findOrderStatus() {
        return categoryRepo.findSubCategory(AppConstants.CATEGORY.ORDER_STATUS.name(), null, Pageable.unpaged()).getContent();
    }

    @Override
    public Boolean categoryInUse(Integer categoryId) {
        Optional<Category> category = this.findById(categoryId);
        if (category.isEmpty()) {
            throw new AppException();
        }
        switch (category.get().getType()) {
            case "UNIT":
                List<ProductDTO> productByUnits = productInfoService.findAll(-1, -1, null, null, null, null, null, categoryId, null).getContent();
                List<Material> materials = materialService.findAll(-1, -1, null, categoryId, null, null, null, null).getContent();
                if (!productByUnits.isEmpty() || !materials.isEmpty()) {
                    return true;
                }
                break;
            case "FABRIC_TYPE":
//                if (!productDetailRepo.findAll(null, null, null, null, categoryId).isEmpty()) {
//                    return true;
//                }
                break;
            case "PAYMENT_METHOD":
//                if (!orderService.findOrdersByPaymentMethodId(categoryId).isEmpty()) {
//                    return true;
//                }
                break;
            case "SALES_CHANNEL":
//                if (!orderService.findOrdersBySalesChannelId(categoryId).isEmpty()) {
//                    return true;
//                }
                break;
            case "SIZE":
//                if (!productDetailRepo.findAll(null, null, null, categoryId, null).isEmpty()) {
//                    return true;
//                }
                break;
            case "COLOR":
//                if (!productDetailRepo.findAll(null, null, categoryId, null, null).isEmpty()) {
//                    return true;
//                }
                break;
            case "PRODUCT_TYPE":
                List<ProductDTO> productByTypes = productInfoService.findAll(-1, -1, null, null, null, null, null, categoryId, null).getContent();
                if (!productByTypes.isEmpty()) {
                    return true;
                }
                break;
            case "ORDER_STATUS":
//                if (!orderService.findOrdersByStatus(categoryId).isEmpty()) {
//                    return true;
//                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + category.get().getType());
        }
        return false;
    }

    @Transactional
    @Override
    public String importData(MultipartFile fileImport, String categoryType) {
        Date startTimeImport = new Date();
        String resultOfFlowieeImport = "";
        String detailOfFlowieeImport = "";
        int importSuccess = 0;
        int totalRecord = 0;
        boolean isImportSuccess = true;
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileImport.getInputStream());
            XSSFSheet sheet = workbook.getSheetAt(0);
            for (int i = 3; i < sheet.getPhysicalNumberOfRows(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row != null) {
                    String categoryCode = row.getCell(1).getStringCellValue();
                    String categoryName = row.getCell(2).getStringCellValue();
                    String categoryNote = row.getCell(3).getStringCellValue();
                    //Nếu name null -> không ínsert data null vào database
                    if (categoryName == null || categoryName.isEmpty()) {
                        XSSFCellStyle cellStyle = workbook.createCellStyle();
                        XSSFFont fontStyle = workbook.createFont();
                        row.getCell(1).setCellStyle(CommonUtils.highlightDataImportError(cellStyle, fontStyle));
                        row.getCell(2).setCellStyle(CommonUtils.highlightDataImportError(cellStyle, fontStyle));
                        row.getCell(3).setCellStyle(CommonUtils.highlightDataImportError(cellStyle, fontStyle));
                        continue;
                    }

                    Category category = new Category();
                    category.setType(categoryType);
                    category.setCode(!categoryCode.isEmpty() ? categoryCode : CommonUtils.genCategoryCodeByName(categoryName));
                    category.setName(categoryName);
                    category.setNote(categoryNote);

                    if (this.save(category) == null) {
                        isImportSuccess = false;
                        XSSFCellStyle cellStyle = workbook.createCellStyle();
                        XSSFFont fontStyle = workbook.createFont();
                        row.getCell(1).setCellStyle(CommonUtils.highlightDataImportError(cellStyle, fontStyle));
                        row.getCell(2).setCellStyle(CommonUtils.highlightDataImportError(cellStyle, fontStyle));
                        row.getCell(3).setCellStyle(CommonUtils.highlightDataImportError(cellStyle, fontStyle));
                    } else {
                        importSuccess++;
                    }
                    totalRecord++;
                }
            }
            workbook.close();

            if (isImportSuccess) {
                //resultOfFlowieeImport = MessagesUtil.IMPORT_DM_DONVITINH_SUCCESS;
                detailOfFlowieeImport = importSuccess + " / " + totalRecord;
            } else {
                //resultOfFlowieeImport = MessagesUtil.IMPORT_DM_DONVITINH_FAIL;
                detailOfFlowieeImport = importSuccess + " / " + totalRecord;
            }
            //Save file attach to storage
            FileStorage fileStorage = new FileStorage(fileImport, MODULE.CATEGORY.name(), null);
            fileStorage.setNote("IMPORT");
            fileStorage.setStatus(false);
            fileStorage.setActive(false);
            fileStorage.setAccount(new Account(CommonUtils.getUserPrincipal().getId()));
            fileStorage.setStorageName(Instant.now(Clock.systemUTC()).toEpochMilli() + "_" + fileImport.getOriginalFilename());
            fileStorageService.saveFileOfImport(fileImport, fileStorage);

            //Save import
            FlowieeImport flowieeImport = new FlowieeImport();
            flowieeImport.setModule(mvModule);
            flowieeImport.setEntity(Category.class.getName());
            flowieeImport.setAccount(new Account(CommonUtils.getUserPrincipal().getId()));
            flowieeImport.setStartTime(startTimeImport);
            flowieeImport.setEndTime(new Date());
            flowieeImport.setResult(resultOfFlowieeImport);
            flowieeImport.setDetail(detailOfFlowieeImport);
            flowieeImport.setSuccessRecord(importSuccess);
            flowieeImport.setTotalRecord(totalRecord);
            flowieeImport.setFileId(fileStorageRepo.findByCreatedTime(fileStorage.getCreatedAt()).getId());
            importService.save(flowieeImport);

            Notification notification = new Notification();
            notification.setTitle(resultOfFlowieeImport);
            notification.setSend(0);
            notification.setReceive(CommonUtils.getUserPrincipal().getId());
            //notification.setType(MessagesUtil.NOTI_TYPE_IMPORT);
            notification.setContent(resultOfFlowieeImport);
            notification.setReaded(false);
            notification.setImportId(appImportRepo.findByStartTime(flowieeImport.getStartTime()).getId());
            notificationService.save(notification);

            return MessageUtils.CREATE_SUCCESS;
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    @Override
    public byte[] exportTemplate(String categoryType) {
        return CommonUtils.exportTemplate(AppConstants.TEMPLATE_IE_DM_CATEGORY);
    }

    @Override
    public byte[] exportData(String categoryType) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String filePathOriginal = FileUtils.excelTemplatePath + "/" + AppConstants.TEMPLATE_IE_DM_CATEGORY + ".xlsx";
        String filePathTemp = FileUtils.excelTemplatePath + "/" + AppConstants.TEMPLATE_IE_DM_CATEGORY + "_" + Instant.now(Clock.systemUTC()).toEpochMilli() + ".xlsx";
        File fileDeleteAfterExport = new File(Path.of(filePathTemp).toUri());
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(Files.copy(Path.of(filePathOriginal), Path.of(filePathTemp), StandardCopyOption.REPLACE_EXISTING).toFile());
            XSSFSheet sheet = workbook.getSheetAt(0);
            List<Category> listData = this.findAll();
            for (int i = 0; i < listData.size(); i++) {
                XSSFRow row = sheet.createRow(i + 3);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(listData.get(i).getCode());
                row.createCell(2).setCellValue(listData.get(i).getName());
                row.createCell(3).setCellValue(listData.get(i).getNote());
                for (int j = 0; j <= 3; j++) {
                    row.getCell(j).setCellStyle(FileUtils.setBorder(workbook.createCellStyle()));
                }
            }
            workbook.write(stream);
            workbook.close();
        } catch (Exception e) {
            throw new AppException(e);
        } finally {
            if (fileDeleteAfterExport.exists()) {
                fileDeleteAfterExport.delete();
            }
        }
        return stream.toByteArray();
    }
}