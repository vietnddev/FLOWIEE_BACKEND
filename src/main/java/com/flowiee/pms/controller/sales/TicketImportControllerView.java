package com.flowiee.pms.controller.sales;

import com.flowiee.pms.entity.product.MaterialTemp;
import com.flowiee.pms.entity.product.ProductVariantTemp;
import com.flowiee.pms.entity.sales.TicketImport;
import com.flowiee.pms.exception.ResourceNotFoundException;
import com.flowiee.pms.service.product.MaterialService;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.service.sales.SupplierService;
import com.flowiee.pms.service.sales.TicketImportService;
import com.flowiee.pms.service.storage.StorageService;
import com.flowiee.pms.controller.BaseController;

import com.flowiee.pms.utils.constants.Pages;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/stg/ticket-import")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TicketImportControllerView extends BaseController {
    StorageService        storageService;
    MaterialService       materialService;
    SupplierService       supplierService;
    TicketImportService   ticketImportService;
    ProductVariantService productVariantService;

    @GetMapping
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView viewTickets() {
        ModelAndView modelAndView = new ModelAndView(Pages.STG_TICKET_IMPORT.getTemplate());
        modelAndView.addObject("listStorages", storageService.findAll());
        return baseView(modelAndView);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView viewDetail(@PathVariable("id") Integer ticketImportId) {
        Optional<TicketImport> ticketImport = ticketImportService.findById(ticketImportId);
        if (ticketImport.isEmpty()) {
            throw new ResourceNotFoundException("Ticket import not found!");
        }
        ModelAndView modelAndView = new ModelAndView(Pages.STG_TICKET_IMPORT_DETAIL.getTemplate());
        modelAndView.addObject("ticketImportId", ticketImportId);
        modelAndView.addObject("ticketImportDetail", ticketImport.get());
        modelAndView.addObject("listProductVariant", productVariantService.findAll());
        modelAndView.addObject("listMaterial", materialService.findAll());
        modelAndView.addObject("listSupplier", supplierService.findAll());
        modelAndView.addObject("listStorage", storageService.findAll());
        return baseView(modelAndView);
    }

    @ResponseBody
    @GetMapping("/search")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public void search() {
        List<TicketImport> data = ticketImportService.findAll();
        if (data != null) {
            for (TicketImport o : data) {
                System.out.println(o.toString());
            }
        }
    }

//    @GetMapping("/update/{id}")
//    public ModelAndView update(@ModelAttribute("goodsImport") TicketImport ticketImport,
//                         @PathVariable("id") Integer importId,
//                         HttpServletRequest request) {
//        validateModuleStorage.importGoods(true);
//        if (importId <= 0 || ticketImportService.findById(importId) == null) {
//            throw new NotFoundException("Goods import not found!");
//        }
//        ticketImportService.update(ticketImport, importId);
//        return new ModelAndView("redirect:" + request.getHeader("referer"));
//    }

    @GetMapping("/reset/{id}")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView clear(@PathVariable("id") Integer draftImportId) {
        if (draftImportId <= 0 || ticketImportService.findById(draftImportId).isEmpty()) {
            throw new ResourceNotFoundException("Goods import not found!");
        }
        ticketImportService.delete(draftImportId);
        return new ModelAndView("redirect:");
    }

    @PostMapping("/send-approval/{id}")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView sendApproval(@PathVariable("id") Integer importId) {
        if (importId <= 0 || ticketImportService.findById(importId).isEmpty()) {
            throw new ResourceNotFoundException("Goods import not found!");
        }
        ticketImportService.updateStatus(importId, "");
        return new ModelAndView("redirect:");
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView approve(@PathVariable("id") Integer importId) {
        if (importId <= 0 || ticketImportService.findById(importId).isEmpty()) {
            throw new ResourceNotFoundException("Goods import not found!");
        }
        ticketImportService.updateStatus(importId, "");
        return new ModelAndView("redirect:");
    }
}