package com.flowiee.pms.service.system.impl;

import com.flowiee.pms.config.Core;
import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.system.SystemConfig;
import com.flowiee.pms.entity.system.SystemLog;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.model.ShopInfo;
import com.flowiee.pms.utils.ChangeLog;
import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.constants.*;
import com.flowiee.pms.repository.system.ConfigRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.category.CategoryService;
import com.flowiee.pms.service.system.ConfigService;

import com.flowiee.pms.service.system.LanguageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends BaseService implements ConfigService {
    private final CategoryService mvCategoryService;
    private final LanguageService mvLanguageService;
    private final ConfigRepository mvSysConfigRepository;

    private boolean mvAppRefreshing = false;

    @Override
    public Optional<SystemConfig> findById(Long id) {
        return mvSysConfigRepository.findById(id);
    }

    @Override
    public List<SystemConfig> findAll() {
        return mvSysConfigRepository.findAll();
    }

    @Override
    public SystemConfig update(SystemConfig systemConfig, Long id) {
        Optional<SystemConfig> configOpt = this.findById(id);
        if (configOpt.isEmpty()) {
            throw new BadRequestException();
        }
        SystemConfig configBefore = ObjectUtils.clone(configOpt.get());

        systemConfig.setId(id);
        SystemConfig configUpdated = mvSysConfigRepository.save(systemConfig);

        ChangeLog changeLog = new ChangeLog(configBefore, configUpdated);
        systemLogService.writeLogUpdate(MODULE.SYSTEM, ACTION.SYS_CNF_U, MasterObject.SystemConfig, "Cập nhật cấu hình hệ thống", changeLog);
        logger.info("Update config success! {}", configUpdated.getName());

        return configUpdated;
    }

    @Transactional
    @Override
    public String refreshApp() {
        if (mvAppRefreshing) {
            throw new AppException(ErrorCode.SYSTEM_BUSY, new Object[]{}, null, getClass(), null);
        }
        mvAppRefreshing = true;
        try {
            ShopInfo lvShopInfo = CommonUtils.mvShopInfo != null ? CommonUtils.mvShopInfo : new ShopInfo();
            //Reload system configs
            List<SystemConfig> systemConfigList = this.findAll();
            Core.mvSystemConfigList.clear();
            for (SystemConfig systemConfig : systemConfigList) {
                ConfigCode lvConfigCode = ConfigCode.get(systemConfig.getCode());
                String lvConfigValue = systemConfig.getValue();

                if (lvConfigCode == null) continue;

                if (ConfigCode.resourceUploadPath.equals(lvConfigCode)) Core.mvResourceUploadPath = lvConfigValue;
                if (ConfigCode.shopName.equals(lvConfigCode))           lvShopInfo.setName(lvConfigValue);
                if (ConfigCode.shopPhoneNumber.equals(lvConfigCode))    lvShopInfo.setPhoneNumber(lvConfigValue);
                if (ConfigCode.shopEmail.equals(lvConfigCode))          lvShopInfo.setEmail(lvConfigValue);
                if (ConfigCode.shopAddress.equals(lvConfigCode))        lvShopInfo.setAddress(lvConfigValue);
                if (ConfigCode.shopLogoUrl.equals(lvConfigCode))        lvShopInfo.setLogoUrl(lvConfigValue);

                Core.mvSystemConfigList.put(lvConfigCode, systemConfig);
            }
            CommonUtils.mvShopInfo = lvShopInfo;

            //Reload category label
            List<Category> rootCategories = mvCategoryService.findRootCategory();
            for (Category c : rootCategories) {
                if (c.getType() != null && !c.getType().trim().isEmpty()) {
                    CategoryType.valueOf(c.getType()).setLabel(c.getName());
                }
            }

            //Reload order status
            List<Category> lvOrderStatusList = mvCategoryService.findOrderStatus(null);
            for (Category lvCategory : lvOrderStatusList) {
                if (Category.ROOT_LEVEL.equals(lvCategory.getCode()))
                    continue;
                OrderStatus lvOrderStatus = OrderStatus.get(lvCategory.getCode());
                if (lvOrderStatus != null) {
                    lvOrderStatus.setDescription(lvCategory.getNote());
                }
            }

            //Reload message
            mvLanguageService.reloadMessage("vi");
            mvLanguageService.reloadMessage("en");

            if (Core.START_APP_TIME != null) {
                systemLogService.writeLog(MODULE.SYSTEM, ACTION.SYS_REFRESH_APP, MasterObject.Master, LogType.U, "Refresh application", SystemLog.EMPTY, SystemLog.EMPTY);
            }

            int i = 1;
            return new StringBuilder()
                    .append("Completed the following tasks: ")
                    .append("\n " + i++ + ". ").append("Reload message vi & en")
                    .append("\n " + i++ + ". ").append("Reload system configs")
                    .append("\n " + i++ + ". ").append("Reload categories label")
                    .toString();
        } catch (RuntimeException ex) {
            throw new AppException("An error occurred while refreshing app configuration", ex);
        } finally {
            mvAppRefreshing = false;
        }
    }

    @Override
    public List<SystemConfig> getSystemConfigs(String[] configCodes) {
        return getSystemConfigs(List.of(configCodes));
    }

    @Override
    public SystemConfig getSystemConfig(String configCode) {
        return mvSysConfigRepository.findByCode(configCode);
    }

    @Override
    public List<SystemConfig> getSystemConfigs(List<String> configCodes) {
        return mvSysConfigRepository.findByCode(configCodes);
    }
}