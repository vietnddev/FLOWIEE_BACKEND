package com.flowiee.pms.service.system;

import com.flowiee.pms.entity.system.SystemConfig;

import java.util.List;
import java.util.Optional;

public interface ConfigService {
    List<SystemConfig> findAll();

    Optional<SystemConfig> findById(Long configId);

    SystemConfig update(SystemConfig systemConfig, Long configId);

    String refreshApp();

    List<SystemConfig> getSystemConfigs(String[] configCodes);

    SystemConfig getSystemConfig(String configCode);

    List<SystemConfig> getSystemConfigs(List<String> configCodes);
}