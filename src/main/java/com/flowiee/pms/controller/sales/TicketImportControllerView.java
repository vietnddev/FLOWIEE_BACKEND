package com.flowiee.pms.controller.sales;

import com.flowiee.pms.entity.sales.Supplier;
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

import java.util.*;

@Controller
@RequestMapping("/stg/ticket-import")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TicketImportControllerView extends BaseController {
    StorageService mvStorageService;
    MaterialService mvMaterialService;
    SupplierService mvSupplierService;
    TicketImportService mvTicketImportService;
    ProductVariantService mvProductVariantService;

    @GetMapping
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView viewTickets() {
        ModelAndView modelAndView = new ModelAndView(Pages.STG_TICKET_IMPORT.getTemplate());
        modelAndView.addObject("listStorages", mvStorageService.findAll());
        return baseView(modelAndView);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView viewDetail(@PathVariable("id") Integer ticketImportId) {
        Optional<TicketImport> ticketImport = mvTicketImportService.findById(ticketImportId);
        if (ticketImport.isEmpty()) {
            throw new ResourceNotFoundException("Ticket import not found!");
        }

        List<Supplier> suppliers = new ArrayList<>();
        if (ticketImport.get().getSupplier() != null) {
            suppliers.add(new Supplier(ticketImport.get().getSupplier().getId(), ticketImport.get().getSupplier().getName()));
        } else {
            suppliers.add(new Supplier(-1, "Chọn nhà cung cấp"));
        }
        suppliers.addAll(mvSupplierService.findAll(-1, -1, ticketImport.get().getSupplier() != null ? List.of(ticketImport.get().getSupplier().getId()) : null).getContent());

        ModelAndView modelAndView = new ModelAndView(Pages.STG_TICKET_IMPORT_DETAIL.getTemplate());
        modelAndView.addObject("ticketImportId", ticketImportId);
        modelAndView.addObject("ticketImportDetail", ticketImport.get());
        modelAndView.addObject("listProductVariant", mvProductVariantService.findAll());
        modelAndView.addObject("listMaterial", mvMaterialService.findAll());
        modelAndView.addObject("listSupplier", suppliers);
        modelAndView.addObject("listStorage", mvStorageService.findAll());
        return baseView(modelAndView);
    }

    @ResponseBody
    @GetMapping("/search")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public void search() {
        List<TicketImport> data = mvTicketImportService.findAll();
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
        if (draftImportId <= 0 || mvTicketImportService.findById(draftImportId).isEmpty()) {
            throw new ResourceNotFoundException("Goods import not found!");
        }
        mvTicketImportService.delete(draftImportId);
        return new ModelAndView("redirect:");
    }

    @PostMapping("/send-approval/{id}")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView sendApproval(@PathVariable("id") Integer importId) {
        if (importId <= 0 || mvTicketImportService.findById(importId).isEmpty()) {
            throw new ResourceNotFoundException("Goods import not found!");
        }
        mvTicketImportService.updateStatus(importId, "");
        return new ModelAndView("redirect:");
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("@vldModuleSales.importGoods(true)")
    public ModelAndView approve(@PathVariable("id") Integer importId) {
        if (importId <= 0 || mvTicketImportService.findById(importId).isEmpty()) {
            throw new ResourceNotFoundException("Goods import not found!");
        }
        mvTicketImportService.updateStatus(importId, "");
        return new ModelAndView("redirect:");
    }
}