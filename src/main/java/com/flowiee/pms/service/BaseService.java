package com.flowiee.pms.service;

import com.flowiee.pms.service.system.SystemLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseService {
    protected Logger logger = LoggerFactory.getLogger(BaseService.class);

    @Autowired
    protected SystemLogService systemLogService;
}