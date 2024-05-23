package com.flowiee.pms.config;

import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.sales.Customer;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.entity.system.Branch;
import com.flowiee.pms.entity.system.GroupAccount;
import com.flowiee.pms.entity.system.SystemConfig;
import com.flowiee.pms.model.ShopInfo;
import com.flowiee.pms.repository.category.CategoryRepository;
import com.flowiee.pms.repository.sales.CustomerRepository;
import com.flowiee.pms.repository.system.AccountRepository;
import com.flowiee.pms.repository.system.BranchRepository;
import com.flowiee.pms.repository.system.ConfigRepository;
import com.flowiee.pms.repository.system.GroupAccountRepository;
import com.flowiee.pms.service.system.*;
import com.flowiee.pms.utils.AppConstants;
import com.flowiee.pms.utils.CommonUtils;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class StartUp {
	private final ConfigRepository configRepository;
	private final BranchRepository branchRepository;
	private final AccountRepository accountRepository;
	private final LanguageService languageService;
	private final CustomerRepository customerRepository;
	private final CategoryRepository categoryRepository;
	private final GroupAccountRepository groupAccountRepository;

	public StartUp(LanguageService languageService, ConfigRepository configRepository, @Lazy CategoryRepository categoryRepository,
				   @Lazy BranchRepository branchRepository, @Lazy AccountRepository accountRepository, @Lazy CustomerRepository customerRepository,
				   @Lazy GroupAccountRepository groupAccountRepository) {
		this.configRepository = configRepository;
		this.branchRepository = branchRepository;
		this.accountRepository = accountRepository;
		this.languageService = languageService;
		this.customerRepository = customerRepository;
		this.categoryRepository = categoryRepository;
		this.groupAccountRepository = groupAccountRepository;
	}

    @Bean
    CommandLineRunner init() {
    	return args -> {
			initData();
			loadShopInfo();
			initReportConfig();
            initEndPointConfig();
			loadLanguageMessages("en");
			loadLanguageMessages("vi");
			CommonUtils.START_APP_TIME = LocalDateTime.now();
        };
    }

    private void loadLanguageMessages(String langCode) {
        languageService.reloadMessage(langCode);
    }

	private void loadShopInfo() {
		ShopInfo shopInfo = new ShopInfo();
		List<String> listCode = List.of("shopName", "shopEmail", "shopPhoneNumber", "shopAddress", "shopLogoUrl");
		List<SystemConfig> systemConfigList = configRepository.findByCode(listCode);
		for (SystemConfig sysConfig : systemConfigList) {
			switch (sysConfig.getCode()) {
				case "shopName":
					shopInfo.setName(sysConfig.getName());
					break;
				case "shopEmail":
					shopInfo.setEmail(sysConfig.getName());
					break;
				case "shopPhoneNumber":
					shopInfo.setPhoneNumber(sysConfig.getName());
					break;
				case "shopAddress":
					shopInfo.setAddress(sysConfig.getName());
					break;
				case "shopLogoUrl":
					shopInfo.setLogoUrl(sysConfig.getName());
					break;
            }
		}
		CommonUtils.mvShopInfo = shopInfo;
	}

	private void initEndPointConfig() {
		CommonUtils.mvEndPointHeaderConfig.clear();
		CommonUtils.mvEndPointSideBarConfig.clear();
		for (AppConstants.ENDPOINT e : AppConstants.ENDPOINT.values()) {
			if (e.getType().equals("HEADER") && e.isStatus()) {
				CommonUtils.mvEndPointHeaderConfig.put(e.name(), e.getValue());
			}
			 if (e.getType().equals("SIDEBAR") && e.isStatus()) {
				CommonUtils.mvEndPointSideBarConfig.put(e.name(), e.getValue());
			}
		}
    }

	private void initReportConfig() {
		String templateExportTempStr = CommonUtils.excelTemplatePath + "/temp";
		Path templateExportTempPath = Paths.get(templateExportTempStr);
		if (!Files.exists(templateExportTempPath)) {
            try {
                Files.createDirectory(templateExportTempPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
	}

	private void initData() throws Exception {
		String flagConfigCode = "initData";
		SystemConfig flagConfigObj = configRepository.findByCode(flagConfigCode);
		if (flagConfigObj == null) {
			CommonUtils.mvInitData = false;
			configRepository.save(new SystemConfig(flagConfigCode, "Initialize initial data for the system", "N"));
			configRepository.save(new SystemConfig("shopName", "Tên cửa hàng", "Flowiee"));
			configRepository.save(new SystemConfig("shopEmail", "Email", "nguyenducviet0684@gmail.com"));
			configRepository.save(new SystemConfig("shopPhoneNumber", "Số điện thoại", "(+84) 706 820 684"));
			configRepository.save(new SystemConfig("shopAddress", "Địa chỉ", "Phường 7, Quận 8, Thành phố Hồ Chí Minh"));
			configRepository.save(new SystemConfig("shopLogoUrl", "Logo", null));
			configRepository.save(new SystemConfig("emailHost", "Email host", "smtp"));
			configRepository.save(new SystemConfig("emailPort", "Email port", "587"));
			configRepository.save(new SystemConfig("emailUser", "Email username", null));
			configRepository.save(new SystemConfig("emailPass", "Email password", null));
			configRepository.save(new SystemConfig("sysTimeOut", "Thời gian timeout", "3600"));
			configRepository.save(new SystemConfig("pathFileUpload", "Thư mục lưu file upload", null));
			configRepository.save(new SystemConfig("maxSizeFileUpload", "Dung lượng file tối đa cho phép upload", null));
			configRepository.save(new SystemConfig("extensionAllowedFileUpload", "Định dạng file được phép upload", null));
			configRepository.save(new SystemConfig("sendEmailReportDaily", "Gửi mail báo cáo hoạt động kinh doanh hàng ngày", "N"));
		}
		SystemConfig systemConfigInitData = configRepository.findByCode(flagConfigCode);
		if ("Y".equals(systemConfigInitData.getValue())) {
			return;
		}
		//Init category
		String fileNameCsvCategory = CommonUtils.initCsvDataPath + "/Category.csv";
		CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
		FileReader fileReader = new FileReader(fileNameCsvCategory);
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(parser).build();
		List<Category> listCategory = new ArrayList<>();
		for (String[] row : csvReader.readAll()) {
			//System.out.println(String.join(", ", row));
			Category category = new Category();
			category.setType(row[0]);
			category.setCode(row[1]);
			category.setName(row[2]);
			category.setStatus(Boolean.parseBoolean(row[3]));
			category.setIsDefault(row[4]);
			category.setEndpoint(row[5]);
			category.setCreatedBy(-1);
			category.setLastUpdatedBy("-1");
			listCategory.add(category);
		}
		listCategory.remove(0);//header
		categoryRepository.saveAll(listCategory);
		fileReader.close();
		csvReader.close();
		//Init branch
		Branch branch = new Branch(null, "MAIN", "Trụ sở");
		branch.setCreatedBy(-1);
		branch.setLastUpdatedBy("-1");
		Branch branchSaved = branchRepository.save(branch);
		//Init group account
		GroupAccount groupAccountManager = new GroupAccount(null, "MANAGER", "Quản lý cửa hàng");
		groupAccountManager.setCreatedBy(-1);
		groupAccountManager.setLastUpdatedBy("-1");
		GroupAccount groupManagerSaved = groupAccountRepository.save(groupAccountManager);

		GroupAccount groupAccountStaff = new GroupAccount(null, "STAFF", "Nhân viên bán hàng");
		groupAccountStaff.setCreatedBy(-1);
		groupAccountStaff.setLastUpdatedBy("-1");
		GroupAccount groupStaffSaved = groupAccountRepository.save(groupAccountStaff);
		//Init admin account
		Account adminAccount = new Account();
		adminAccount.setUsername(CommonUtils.ADMINISTRATOR);
		adminAccount.setPassword(CommonUtils.encodePassword("123456"));
		adminAccount.setFullName("Administrator");
		adminAccount.setSex(true);
		adminAccount.setRole("ADMIN");
		adminAccount.setBranch(branchSaved);
		adminAccount.setGroupAccount(groupManagerSaved);
		adminAccount.setStatus(true);
		adminAccount.setCreatedBy(-1);
		adminAccount.setLastUpdatedBy("-1");
		Account adminAccountSaved = accountRepository.save(adminAccount);

		Account staffAccount = new Account();
		staffAccount.setUsername("staff");
		staffAccount.setPassword(CommonUtils.encodePassword("123456"));
		staffAccount.setFullName("Staff");
		staffAccount.setSex(true);
		staffAccount.setRole("USER");
		staffAccount.setBranch(branchSaved);
		staffAccount.setGroupAccount(groupStaffSaved);
		staffAccount.setStatus(true);
		staffAccount.setCreatedBy(-1);
		staffAccount.setLastUpdatedBy("-1");
		Account staffAccountSaved = accountRepository.save(staffAccount);
		//Init customer
		Customer customer = new Customer();
		customer.setCustomerName("Khách vãng lai");
		customer.setDateOfBirth(LocalDate.of(2000, 1, 8));
		customer.setSex(true);
		customer.setCreatedBy(-1);
		customer.setLastUpdatedBy("-1");
		Customer customerSaved = customerRepository.save(customer);

		systemConfigInitData.setValue("Y");
		configRepository.save(systemConfigInitData);
		CommonUtils.mvInitData = true;
	}
}