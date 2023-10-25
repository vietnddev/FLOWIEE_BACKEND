package com.flowiee.app.sanpham.controller;

import com.flowiee.app.common.authorization.KiemTraQuyenModuleKhoTaiLieu;
import com.flowiee.app.common.utils.FlowieeUtil;
import com.flowiee.app.common.utils.PagesUtil;
import com.flowiee.app.hethong.entity.FlowieeImport;
import com.flowiee.app.hethong.service.AccountService;
import com.flowiee.app.hethong.service.FlowieeImportService;
import com.flowiee.app.hethong.service.NotificationService;
import com.flowiee.app.sanpham.entity.GoodsImport;
import com.flowiee.app.sanpham.services.GoodsImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/storage/goods")
public class GoodsImportController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private GoodsImportService goodsImportService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private KiemTraQuyenModuleKhoTaiLieu kiemTraQuyenModuleKho;

    @GetMapping("")
    public ModelAndView loadPage() {
        if (!accountService.isLogin()) {
            return new ModelAndView(PagesUtil.PAGE_LOGIN);
        }
        if (kiemTraQuyenModuleKho.kiemTraQuyenTaoPhieuNhapHang()) {
            ModelAndView modelAndView = new ModelAndView("");
            GoodsImport goodsImportPresent = goodsImportService.findDraftImportPresent(FlowieeUtil.ACCOUNT_ID);
            if (goodsImportPresent == null) {
                goodsImportPresent = goodsImportService.createDraftImport();
            }
            modelAndView.addObject("goodsImport", new GoodsImport());
            modelAndView.addObject("draftGoodsImport", goodsImportPresent);
            modelAndView.addObject("listNotification", notificationService.findAllByReceiveId(FlowieeUtil.ACCOUNT_ID));
            return modelAndView;
        } else {
            return new ModelAndView(PagesUtil.PAGE_UNAUTHORIZED);
        }
    }

    @GetMapping("/reset/{id}")
    public String clear(@PathVariable("id") Integer draftImportId) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleKho.kiemTraQuyenTaoPhieuNhapHang()) {
            goodsImportService.delete(draftImportId);
            return "redirect:";
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }

    @PostMapping("/send-approval/{id}")
    public String sendApproval(@PathVariable("id") Integer importId) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleKho.kiemTraQuyenTaoPhieuNhapHang()) {
            goodsImportService.updateStatus(importId, "");
            return "redirect:";
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable("id") Integer importId) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleKho.kiemTraQuyenTaoPhieuNhapHang()) {
            goodsImportService.updateStatus(importId, "");
            return "redirect:";
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }
}