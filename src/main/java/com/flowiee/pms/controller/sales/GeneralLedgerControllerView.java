package com.flowiee.pms.controller.sales;

import com.flowiee.pms.controller.BaseController;
import com.flowiee.pms.utils.constants.Pages;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/ledger")
public class GeneralLedgerControllerView extends BaseController {
    @GetMapping
    @PreAuthorize("@vldModuleSales.readGeneralLedger(true)")
    public ModelAndView getGeneralLedger() {
        return baseView(new ModelAndView(Pages.SLS_LEDGER.getTemplate()));
    }
}