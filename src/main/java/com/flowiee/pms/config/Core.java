package com.flowiee.pms.config;

import com.flowiee.pms.entity.BaseEntity;
import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.sales.Customer;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.entity.system.Branch;
import com.flowiee.pms.entity.system.GroupAccount;
import com.flowiee.pms.entity.system.SystemConfig;
import com.flowiee.pms.model.ServerInfo;
import com.flowiee.pms.repository.category.CategoryRepository;
import com.flowiee.pms.repository.sales.CustomerRepository;
import com.flowiee.pms.repository.system.AccountRepository;
import com.flowiee.pms.repository.system.BranchRepository;
import com.flowiee.pms.repository.system.ConfigRepository;
import com.flowiee.pms.repository.system.GroupAccountRepository;
import com.flowiee.pms.service.system.*;
import com.flowiee.pms.utils.AppConstants;
import com.flowiee.pms.utils.CommonUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flowiee.pms.utils.FileUtils;
import com.flowiee.pms.utils.constants.AccountStatus;
import com.flowiee.pms.utils.constants.ConfigCode;
import com.flowiee.pms.utils.constants.EndPoint;
import com.flowiee.pms.utils.constants.NotificationType;
import com.opencsv.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class Core {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	ConfigRepository       mvConfigRepository;
	BranchRepository       mvBranchRepository;
	AccountRepository      mvAccountRepository;
	LanguageService        mvLanguageService;
	CustomerRepository     mvCustomerRepository;
	CategoryRepository     mvCategoryRepository;
	GroupAccountRepository mvGroupAccountRepository;
	ConfigService          mvConfigService;
	TemplateSendEmail      mvTemplateSendEmail;

	public static LocalDateTime                                     START_APP_TIME;
	public static String                                            mvResourceUploadPath      = null;
	public static Map<NotificationType, TemplateSendEmail.Template> mvGeneralEmailTemplateMap = new HashMap<>();
	public static Map<ConfigCode, SystemConfig>                     mvSystemConfigList        = new HashMap();
	public static final ConfigCode                                  mvConfigInitData          = ConfigCode.initData;

    @Bean
    CommandLineRunner init() {
    	return args -> {
    		initConfig();
			initData();
			configReport();
            configEndPoint();
            mvConfigService.refreshApp();
			logger.info("Finish loads system configs");

			loadLanguageMessages("en");
			logger.info("Finish downloading vi messages");

			loadLanguageMessages("vi");
			logger.info("Finish downloading en message");

			List<TemplateSendEmail.Template> lvGeneralMailTemplates = mvTemplateSendEmail.getGeneralMailTemplates();
			lvGeneralMailTemplates.forEach(lvTemplate -> {
				NotificationType lvNotificationType = NotificationType.valueOf(lvTemplate.getType());
				String lvEncoding = lvTemplate.getEncoding();
				String lvTemplatePath = lvTemplate.getPath();
				StringBuilder lvTemplateContent = new StringBuilder("");
				if (Files.exists(Path.of(lvTemplatePath))) {
					byte[] lvBuf = new byte[1024];
					try (BufferedInputStream lvIs = new BufferedInputStream(new FileInputStream(new File(lvTemplatePath)));
						 ByteArrayOutputStream lvOs = new ByteArrayOutputStream()) {
						int lvBytesRead = -1;
						while ((lvBytesRead = lvIs.read(lvBuf)) != -1) {
							lvOs.write(lvBuf, 0, lvBytesRead);
						}
						lvTemplateContent.append(new String(lvOs.toByteArray(), lvEncoding));
					} catch (IOException e) {
						logger.warn(e.getMessage(), e);
					}
				}
				lvTemplate.setTemplateContent(lvTemplateContent.toString());
				mvGeneralEmailTemplateMap.put(lvNotificationType, lvTemplate);
			});
			logger.info("Finish loads template email");

			START_APP_TIME = LocalDateTime.now();
        };
    }

	@EventListener
	private void loadServerInfo(WebServerInitializedEvent event) {
		int serverPort = event.getWebServer().getPort();
		String ipAddress = "localhost";
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			Log.info("Can't get local host address");
		}
		System.out.println("Server is running on IP: " + ipAddress + ", Port: " + serverPort);
		CommonUtils.mvServerInfo = new ServerInfo(ipAddress, serverPort);
	}

    private void loadLanguageMessages(String langCode) {
        mvLanguageService.reloadMessage(langCode);
    }

	private void configReport() {
		String templateExportTempStr = FileUtils.excelTemplatePath + "/temp";
		Path templateExportTempPath = Paths.get(templateExportTempStr);
		if (!Files.exists(templateExportTempPath)) {
            try {
                Files.createDirectories(templateExportTempPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
	}

	private void configEndPoint() {
		CommonUtils.mvEndPointHeaderConfig.clear();
		CommonUtils.mvEndPointSideBarConfig.clear();
		for (EndPoint e : EndPoint.values()) {

			if (e.getType().equals("HEADER") && e.isStatus()) {
				CommonUtils.mvEndPointHeaderConfig.put(e.name(), e.getValue());
			}
			if (e.getType().equals("SIDEBAR") && e.isStatus()) {
				CommonUtils.mvEndPointSideBarConfig.put(e.name(), e.getValue());
			}
		}
	}

	private void initConfig() {
		List<SystemConfig> cnfList = initConfigModels(ConfigCode.values());

		SystemConfig flagConfigObj = mvConfigRepository.findByCode(mvConfigInitData.name());
		if (flagConfigObj == null) {
			mvConfigRepository.saveAll(cnfList);
		}

		initNewConfigIfDatabaseNotDefined(cnfList);

		for (SystemConfig systemConfig : mvConfigRepository.findAll()) {
			ConfigCode lvConfigCode = ConfigCode.get(systemConfig.getCode());
			if (lvConfigCode == null)
				continue;
			mvSystemConfigList.put(lvConfigCode, systemConfig);
		}

		mvResourceUploadPath = mvSystemConfigList.get(ConfigCode.resourceUploadPath).getValue();
		CommonUtils.defaultNewPassword = mvSystemConfigList.get(ConfigCode.generateNewPasswordDefault).getValue();
	}

	private void initData() throws Exception {
		SystemConfig systemConfigInitData = mvConfigRepository.findByCode(mvConfigInitData.name());
		if ("Y".equals(systemConfigInitData.getValue())) {
			return;
		}
		//Init category
		CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
		FileReader fileReader = new FileReader(FileUtils.getFileDataCategoryInit());
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(parser).build();
		List<Category> listCategory = new ArrayList<>();
		for (String[] row : csvReader.readAll()) {
			//System.out.println(String.join(", ", row));
			Category category = Category.builder()
				.type(row[0])
				.code(row[1])
				.name(row[2])
				.status(Boolean.parseBoolean(row[3]))
				.isDefault(row[4])
				.endpoint(row[5]).build();
				initAudit(category);
			listCategory.add(category);
		}
		listCategory.remove(0);//header
		mvCategoryRepository.saveAll(listCategory);
		fileReader.close();
		csvReader.close();
		//Init branch
		Branch branch = Branch.builder().branchCode("MAIN").branchName("Trụ sở").build();
		initAudit(branch);
		Branch branchSaved = mvBranchRepository.save(branch);
		//Init group account
		GroupAccount groupAccountManager = GroupAccount.builder().groupCode("MANAGER").groupName("Quản lý cửa hàng").build();
		initAudit(groupAccountManager);
		GroupAccount groupManagerSaved = mvGroupAccountRepository.save(groupAccountManager);

		GroupAccount groupAccountStaff = GroupAccount.builder().groupCode("STAFF").groupName("Nhân viên bán hàng").build();
		initAudit(groupAccountStaff);
		GroupAccount groupStaffSaved = mvGroupAccountRepository.save(groupAccountStaff);
		//Init admin account
		Account adminAccount = Account.builder()
			.username(AppConstants.ADMINISTRATOR)
			.password(CommonUtils.encodePassword(CommonUtils.defaultNewPassword))
			.fullName("Administrator").sex(true)
			.role("ADMIN")
			.branch(branchSaved).groupAccount(groupManagerSaved)
			.status(AccountStatus.N.name())
			.build();
			initAudit(adminAccount);
		mvAccountRepository.save(adminAccount);

		Account staffAccount = Account.builder()
			.username("staff")
			.password(CommonUtils.encodePassword(CommonUtils.defaultNewPassword))
			.fullName("Staff").sex(true)
			.role("USER")
			.branch(branchSaved).groupAccount(groupStaffSaved)
			.status(AccountStatus.N.name())
			.build();
			initAudit(staffAccount);
		mvAccountRepository.save(staffAccount);
		//Init customer
		Customer customer = Customer.builder().customerName("Khách vãng lai")
				.dateOfBirth(LocalDate.of(2000, 1, 8)).sex(true).build();
			initAudit(customer);
		mvCustomerRepository.save(customer);

		systemConfigInitData.setValue("Y");
		mvConfigRepository.save(systemConfigInitData);
	}

	private List<SystemConfig> initConfigModels(ConfigCode[] configs) {
		List<SystemConfig> systemConfigList = new ArrayList<>();
    	for (ConfigCode c : configs) {
			SystemConfig cnfModel = new SystemConfig(c.name(), c.getDescription(), c.getDefaultValue());
			initAudit(cnfModel);
			systemConfigList.add(cnfModel);
		}
		return systemConfigList;
	}

	private BaseEntity initAudit(BaseEntity baseEntity) {
		baseEntity.setCreatedBy(-1l);
		baseEntity.setLastUpdatedBy("SA");
		return baseEntity;
	}

	public static String getResourceUploadPath() {
		return mvResourceUploadPath;
	}

	private void initNewConfigIfDatabaseNotDefined(List<SystemConfig> initCnfList) {
		for (SystemConfig sysConfig : initCnfList) {
			if (mvConfigRepository.findByCode(sysConfig.getCode()) == null)
				mvConfigRepository.save(sysConfig);
		}
	}
}