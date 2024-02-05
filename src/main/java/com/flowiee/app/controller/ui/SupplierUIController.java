package com.flowiee.app.controller.ui;

import com.flowiee.app.exception.NotFoundException;
import com.flowiee.app.security.ValidateModuleProduct;

import com.flowiee.app.utils.EndPointUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.flowiee.app.base.BaseController;
import com.flowiee.app.utils.PagesUtils;
import com.flowiee.app.entity.Supplier;
import com.flowiee.app.service.SupplierService;

@Controller
@RequestMapping("/san-pham/supplier")
public class SupplierUIController extends BaseController {
	@Autowired private SupplierService supplierService;
	@Autowired private ValidateModuleProduct validateModuleProduct;

	@GetMapping
	public ModelAndView viewAllSupplier() {
		validateModuleProduct.readSupplier(true);
		ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_SUPPLIER);
		modelAndView.addObject("listSupplier", supplierService.findAll());
		return baseView(modelAndView);
	}
}