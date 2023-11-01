package com.flowiee.app.product.controller;

import com.flowiee.app.common.exception.NotFoundException;
import com.flowiee.app.common.utils.PagesUtil;
import com.flowiee.app.system.service.AccountService;
import com.flowiee.app.product.entity.ProductAttribute;
import com.flowiee.app.product.services.ProductAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/san-pham/attribute/")
public class ProductAttributeController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProductAttributeService productAttributeService;

    //Thêm mới thuộc tính
    @PostMapping(value = "/insert")
    public String insertAttributes(HttpServletRequest request, @ModelAttribute("thuocTinhSanPham") ProductAttribute productAttribute) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        productAttributeService.save(productAttribute);
        return "redirect:" + request.getHeader("referer");
    }

    @Transactional
    @PostMapping(value = "/update/{id}")
    public String updateAttribute(@ModelAttribute("thuocTinhSanPham") ProductAttribute attribute,
                                  HttpServletRequest request, @PathVariable("id") Integer id) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        attribute.setId(id);
        productAttributeService.update(attribute, id);
        return "redirect:" + request.getHeader("referer");
    }

    @Transactional
    @PostMapping(value = "/delete/{id}")
    public String deleteAttribute(@ModelAttribute("attribute") ProductAttribute attribute,
                                  HttpServletRequest request, @PathVariable("id") Integer attributeID) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (productAttributeService.findById(attributeID) != null) {
            productAttributeService.delete(attributeID);
            return "redirect:" + request.getHeader("referer");
        } else {
            throw new NotFoundException();
        }
    }
}