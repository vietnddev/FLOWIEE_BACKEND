package com.flowiee.app.category.controller;

import com.flowiee.app.config.KiemTraQuyenModuleDanhMuc;
import com.flowiee.app.common.exception.BadRequestException;
import com.flowiee.app.common.utils.EndPointUtil;
import com.flowiee.app.common.utils.FileUtil;
import com.flowiee.app.common.utils.FlowieeUtil;
import com.flowiee.app.common.utils.PagesUtil;
import com.flowiee.app.category.entity.KenhBanHang;
import com.flowiee.app.category.service.KenhBanHangService;
import com.flowiee.app.system.service.AccountService;
import com.flowiee.app.system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/danh-muc/kenh-ban-hang")
public class KenhBanHangController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private KenhBanHangService kenhBanHangService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private KiemTraQuyenModuleDanhMuc kiemTraQuyenModule;

    @GetMapping("")
    public ModelAndView findAll() {
        if (!accountService.isLogin()) {
            return new ModelAndView(PagesUtil.PAGE_LOGIN);
        }
        if (kiemTraQuyenModule.kiemTraQuyenXem()) {
            ModelAndView modelAndView = new ModelAndView(PagesUtil.PAGE_DANHMUC_KENHBANHANG);
            List<KenhBanHang> list = kenhBanHangService.findAll();
            modelAndView.addObject("listDanhMuc", list);
            modelAndView.addObject("kenhBanHang", new KenhBanHang());
            modelAndView.addObject("templateImportName", FileUtil.TEMPLATE_IE_DM_LOAIKENHBANHANG);
            modelAndView.addObject("url_template", EndPointUtil.DANHMUC_KENHBANHANG_TEMPLATE);
            modelAndView.addObject("url_import", EndPointUtil.DANHMUC_KENHBANHANG_IMPORT);
            modelAndView.addObject("url_export", EndPointUtil.DANHMUC_KENHBANHANG_EXPORT);
            modelAndView.addObject("listNotification", notificationService.findAllByReceiveId(FlowieeUtil.ACCOUNT_ID));
            if (kiemTraQuyenModule.kiemTraQuyenThemMoi()) {
                modelAndView.addObject("action_create", "enable");
            }
            if (kiemTraQuyenModule.kiemTraQuyenCapNhat()) {
                modelAndView.addObject("action_update", "enable");
            }
            if (kiemTraQuyenModule.kiemTraQuyenXoa()) {
                modelAndView.addObject("action_delete", "enable");
            }
            return modelAndView;
        } else {
            return new ModelAndView(PagesUtil.PAGE_UNAUTHORIZED);
        }
    }

    @PostMapping("/insert")
    public String insert(@ModelAttribute("kenhBanHang") KenhBanHang kenhBanHang) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        kenhBanHangService.save(kenhBanHang);
        return "redirect:";
    }

    @PostMapping("/update/{id}")
    public String update(@ModelAttribute("kenhBanHang") KenhBanHang kenhBanHang,
                         @PathVariable("id") int id, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (id <= 0) {
            throw new BadRequestException();
        }
        kenhBanHangService.update(kenhBanHang, id);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        kenhBanHangService.delete(id);
        return "redirect:" + request.getHeader("referer");
    }

    @GetMapping("/template")
    public ResponseEntity<?> exportTemplate() {
        if (!accountService.isLogin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(PagesUtil.PAGE_LOGIN);
        }
        if (kiemTraQuyenModule.kiemTraQuyenExport()) {
            byte[] dataExport = kenhBanHangService.exportTemplate();
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + FileUtil.TEMPLATE_IE_DM_LOAIKENHBANHANG + ".xlsx");
            return new ResponseEntity<>(new ByteArrayResource(dataExport), header, HttpStatus.CREATED);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(PagesUtil.PAGE_UNAUTHORIZED);
        }
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile file) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModule.kiemTraQuyenExport()) {
            kenhBanHangService.importData(file);
            return "redirect:" + EndPointUtil.DANHMUC_KENHBANHANG_VIEW;
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportData() {
        if (!accountService.isLogin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(PagesUtil.PAGE_LOGIN);
        }
        if (kiemTraQuyenModule.kiemTraQuyenExport()) {
            byte[] dataExport = kenhBanHangService.exportData();
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + FileUtil.TEMPLATE_IE_DM_LOAIKENHBANHANG + ".xlsx");
            return new ResponseEntity<>(new ByteArrayResource(dataExport), header, HttpStatus.CREATED);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(PagesUtil.PAGE_UNAUTHORIZED);
        }
    }
}