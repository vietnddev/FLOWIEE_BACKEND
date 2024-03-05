package com.flowiee.app.controller.view;

import com.flowiee.app.base.BaseController;
import com.flowiee.app.entity.SystemConfig;
import com.flowiee.app.exception.NotFoundException;
import com.flowiee.app.service.*;
import com.flowiee.app.utils.PagesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/sys")
public class SystemUIController extends BaseController {
    @Autowired private ConfigService configService;

    @GetMapping("/notification")
    public ModelAndView getAllNotification() {
        return baseView(new ModelAndView(PagesUtils.SYS_NOTIFICATION));
    }

    @GetMapping("/log")
    public ModelAndView showPageLog() {
        vldModuleSystem.readLog(true);
        return baseView(new ModelAndView(PagesUtils.SYS_LOG));
    }

    @GetMapping("/config")
    public ModelAndView showConfig() {
        vldModuleSystem.setupConfig(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.SYS_CONFIG);
        modelAndView.addObject("listConfig", configService.findAll());
        return baseView(modelAndView);
    }

    @PostMapping("/config/update/{id}")
    public ModelAndView update(@ModelAttribute("config") SystemConfig config, @PathVariable("id") Integer configId) {
        vldModuleSystem.setupConfig(true);
        if (configId <= 0 || configService.findById(configId) == null) {
            throw new NotFoundException("Config not found!");
        }
        configService.update(config, configId);
        return new ModelAndView("redirect:/he-thong/config");
    }
}