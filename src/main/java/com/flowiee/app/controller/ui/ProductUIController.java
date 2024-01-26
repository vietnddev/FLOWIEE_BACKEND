package com.flowiee.app.controller.ui;

import com.flowiee.app.base.BaseController;
import com.flowiee.app.dto.ProductDTO;
import com.flowiee.app.entity.*;
import com.flowiee.app.exception.BadRequestException;
import com.flowiee.app.security.ValidateModuleProduct;
import com.flowiee.app.service.*;
import com.flowiee.app.exception.NotFoundException;
import com.flowiee.app.utils.AppConstants;
import com.flowiee.app.utils.CommonUtils;
import com.flowiee.app.utils.EndPointUtil;
import com.flowiee.app.utils.PagesUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(EndPointUtil.PRO_PRODUCT)
public class ProductUIController extends BaseController {
    private final ProductService          productService;
    private final ProductHistoryService   productHistoryService;
    private final FileStorageService      fileStorageService;
    private final PriceService            priceService;
    private final VoucherService          voucherService;
    private final VoucherTicketService    voucherTicketService;
    private final ValidateModuleProduct   validateModuleProduct;

    @Autowired
    public ProductUIController(ProductService productService, ProductHistoryService productHistoryService, FileStorageService fileStorageService, PriceService priceService, VoucherService voucherService, VoucherTicketService voucherTicketService, ValidateModuleProduct validateModuleProduct) {
        this.productService = productService;
        this.productHistoryService = productHistoryService;
        this.fileStorageService = fileStorageService;
        this.priceService = priceService;
        this.voucherService = voucherService;
        this.voucherTicketService = voucherTicketService;
        this.validateModuleProduct = validateModuleProduct;
    }

    @GetMapping
    public ModelAndView loadProductPage() {
        validateModuleProduct.readProduct(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_PRODUCT);
        modelAndView.addObject("templateImportName", AppConstants.TEMPLATE_I_SANPHAM);
        return baseView(modelAndView);
    }

    @GetMapping(value = "/{id}")
    public ModelAndView viewGeneralProduct(@PathVariable("id") Integer productId) {
        validateModuleProduct.readProduct(true);
        if (productId <= 0 || productService.findProductById(productId) == null) {
            throw new NotFoundException("Product not found!");
        }
        ProductDTO productDetail = ProductDTO.fromProduct(productService.findProductById(productId));
        LinkedHashMap<String, String> listProductStatus = new LinkedHashMap<>();
        if (AppConstants.PRODUCT_STATUS.ACTIVE.getLabel().equals(productDetail.getProductStatus())) {
            listProductStatus.put(AppConstants.PRODUCT_STATUS.ACTIVE.name(), AppConstants.PRODUCT_STATUS.ACTIVE.getLabel());
            listProductStatus.put(AppConstants.PRODUCT_STATUS.INACTIVE.name(), AppConstants.PRODUCT_STATUS.INACTIVE.getLabel());
        } else if (AppConstants.PRODUCT_STATUS.INACTIVE.getLabel().equals(productDetail.getProductStatus())) {
            listProductStatus.put(AppConstants.PRODUCT_STATUS.INACTIVE.name(), AppConstants.PRODUCT_STATUS.INACTIVE.getLabel());
            listProductStatus.put(AppConstants.PRODUCT_STATUS.ACTIVE.name(), AppConstants.PRODUCT_STATUS.ACTIVE.getLabel());
        }
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_PRODUCT_INFO);
        modelAndView.addObject("idSanPham", productId);
        modelAndView.addObject("detailProducts", productDetail);
        modelAndView.addObject("listBienTheSanPham", productService.findAllProductVariantOfProduct(productId));
        modelAndView.addObject("listProductStatus", listProductStatus);
        modelAndView.addObject("listProductHistory", productHistoryService.findByProduct(productId));
        //List image
        modelAndView.addObject("listImageOfSanPham", fileStorageService.getImageOfSanPham(productId));
        //Image active
        FileStorage imageActive = fileStorageService.findImageActiveOfSanPham(productId);
        modelAndView.addObject("imageActive", imageActive != null ? imageActive : new FileStorage());        
        return baseView(modelAndView);
    }

    @GetMapping(value = "/variant/{id}")
    public ModelAndView viewDetailProduct(@PathVariable("id") Integer variantId) {
        validateModuleProduct.readProduct(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_PRODUCT_VARIANT);
        modelAndView.addObject("bienTheSanPham", new ProductVariant());
        modelAndView.addObject("thuocTinhSanPham", new ProductAttribute());
        modelAndView.addObject("giaBanSanPham", new Price());
        modelAndView.addObject("listThuocTinh", productService.findAllAttributes(variantId));
        modelAndView.addObject("bienTheSanPhamId", variantId);
        modelAndView.addObject("bienTheSanPham", productService.findProductVariantById(variantId));
        modelAndView.addObject("listImageOfSanPhamBienThe", fileStorageService.getImageOfSanPhamBienThe(variantId));
        modelAndView.addObject("listPrices", priceService.findPricesByProductVariant(variantId));
        FileStorage imageActive = fileStorageService.findImageActiveOfSanPhamBienThe(variantId);
        if (imageActive == null) {
            imageActive = new FileStorage();
        }
        modelAndView.addObject("imageActive", imageActive);        
        return baseView(modelAndView);
    }

    @PostMapping("/insert")
    public ModelAndView insertProductOriginal(HttpServletRequest request, @ModelAttribute("sanPham") Product product) {
        validateModuleProduct.insertProduct(true);
        productService.saveProduct(product);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/attribute/insert")
    public ModelAndView insertProductAttribute(HttpServletRequest request, @ModelAttribute("thuocTinhSanPham") ProductAttribute productAttribute) {
        validateModuleProduct.updateProduct(true);
        productService.saveProductAttribute(productAttribute);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping(value = "/update/{id}")
    public ModelAndView updateProductOriginal(HttpServletRequest request,
                                              @ModelAttribute("sanPham") Product product,
                                              @PathVariable("id") Integer productId) {
        validateModuleProduct.updateProduct(true);
        if (product == null|| productId <= 0 || productService.findProductById(productId) == null) {
            throw new NotFoundException("Product not found!");
        }
        productService.updateProduct(product, productId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping(value = "/variant/update/{id}")
    public ModelAndView updateProductVariant(HttpServletRequest request,
                                             @ModelAttribute("productVariant") ProductVariant productVariant,
                                             @PathVariable("id") Integer productVariantId) {
        validateModuleProduct.updateProduct(true);
        if (productService.findProductVariantById(productVariantId) == null) {
            throw new NotFoundException("Product variant not found!");
        }
        productService.updateProductVariant(productVariant, productVariantId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping(value = "/attribute/update/{id}")
    public ModelAndView updateProductAttribute(@ModelAttribute("thuocTinhSanPham") ProductAttribute attribute,
                                        @PathVariable("id") Integer attributeId,
                                        HttpServletRequest request) {
        validateModuleProduct.updateProduct(true);
        if (attributeId <= 0 || productService.findProductAttributeById(attributeId) == null) {
            throw new NotFoundException("Product attribute not found!");
        }
        attribute.setId(attributeId);
        productService.updateProductAttribute(attribute, attributeId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @DeleteMapping(value = "/attribute/delete/{id}")
    public ResponseEntity<String> deleteAttribute(@PathVariable("id") Integer attributeId) {
        validateModuleProduct.updateProduct(true);
        if (productService.findProductAttributeById(attributeId) == null) {
            throw new NotFoundException("Product attribute not found!");
        }
        return ResponseEntity.ok().body(productService.deleteProductAttribute(attributeId));
    }

    @PostMapping(value = "/active-image/{sanPhamId}")
    public ModelAndView activeImageOfProductOriginal(HttpServletRequest request,
                                                    @PathVariable("sanPhamId") Integer productId,
                                                    @RequestParam("imageId") Integer imageId) {
        validateModuleProduct.updateImage(true);
        if (productId == null || productId <= 0 || imageId == null || imageId <= 0) {
            throw new NotFoundException("Product or image not found!");
        }
        fileStorageService.setImageActiveOfSanPham(productId, imageId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping(value = "/variant/active-image/{sanPhamBienTheId}")
    public ModelAndView activeImageOfProductVariant(HttpServletRequest request,
                                                    @PathVariable("sanPhamBienTheId") Integer productVariantId,
                                                    @RequestParam("imageId") Integer imageId) {
        validateModuleProduct.updateImage(true);
        if (productVariantId == null || productVariantId <= 0 || imageId == null || imageId <= 0) {
            throw new NotFoundException("Product variant or image not found!");
        }
        fileStorageService.setImageActiveOfBienTheSanPham(productVariantId, imageId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping(value = "/variant/gia-ban/update/{id}")
    public ModelAndView updateProductPrice(HttpServletRequest request,
                                           @ModelAttribute("price") Price price,
                                           @PathVariable("id") Integer productVariantId) {
        validateModuleProduct.priceManagement(true);
        if (price == null || productVariantId <= 0 || productService.findProductVariantById(productVariantId) == null) {
            throw new NotFoundException("Product variant or price not found!");
        }
        String idGiaBanHienTai = "";
        if (!request.getParameter("idGiaBan").equals("-")) {
            idGiaBanHienTai = request.getParameter("idGiaBan");
        } else {
            idGiaBanHienTai = "0";
        }
        priceService.update(price, productVariantId, Integer.parseInt(idGiaBanHienTai));
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportData() {
        validateModuleProduct.readProduct(true);
        byte[] dataExport = productService.exportData(null);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + AppConstants.TEMPLATE_E_SANPHAM + ".xlsx");
        return new ResponseEntity<>(new ByteArrayResource(dataExport), header, HttpStatus.CREATED);
    }

    /** ******************** GALLERY ******************** **/
    @GetMapping("/gallery")
    public ModelAndView viewGallery() {
        validateModuleProduct.readGallery(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_GALLERY);
        modelAndView.addObject("listImages", fileStorageService.getAllImageSanPham(AppConstants.SYSTEM_MODULE.PRODUCT.name()));
        modelAndView.addObject("listSanPham", productService.findAllProducts().getContent());
        return baseView(modelAndView);
    }

    /** ******************** VOUCHER ******************** **/
    @GetMapping("/voucher")
    public ModelAndView viewVouchers() {
        validateModuleProduct.readVoucher(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_VOUCHER);
        modelAndView.addObject("listVoucher", voucherService.findAll(null, null, null, null));
        modelAndView.addObject("listProduct", productService.findProductsIdAndProductName());
        modelAndView.addObject("listVoucherType", CommonUtils.getVoucherType());
        modelAndView.addObject("voucher", new VoucherInfo());
        modelAndView.addObject("voucherDetail", new VoucherTicket());
        return baseView(modelAndView);
    }

    @GetMapping("/voucher/detail/{id}")
    public ModelAndView viewVoucherDetail(@PathVariable("id") Integer voucherInfoId) {
        validateModuleProduct.readVoucher(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.PRO_VOUCHER_DETAIL);
        modelAndView.addObject("voucherDetail", voucherService.findById(voucherInfoId));
        modelAndView.addObject("listVoucherTicket", voucherTicketService.findByVoucherInfoId(voucherInfoId));
        modelAndView.addObject("voucher", new VoucherInfo());
        modelAndView.addObject("listVoucherType", CommonUtils.getVoucherType());
        return baseView(modelAndView);
    }
    
    @PostMapping("/voucher/insert")
    public ModelAndView insertVoucher(@ModelAttribute("voucher") VoucherInfo voucherInfo,
                                      HttpServletRequest request) throws ParseException {
        validateModuleProduct.insertVoucher(true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        voucherInfo.setStartTime(dateFormat.parse(request.getParameter("startTime_")));
        voucherInfo.setEndTime(dateFormat.parse(request.getParameter("endTime_")));

        List<Integer> listProductToApply = new ArrayList<>();
        String[] pbienTheSP = request.getParameterValues("productToApply");
        if (pbienTheSP != null) {
            for (String id : pbienTheSP) {
                listProductToApply.add(Integer.parseInt(id));
            }
        }
        if (!listProductToApply.isEmpty()) {
            voucherService.save(voucherInfo, listProductToApply);
        }
        return new ModelAndView("redirect:/san-pham/voucher");
    }

    @PostMapping("/voucher/update/{id}")
    public ModelAndView deleteVoucher(@ModelAttribute("voucherInfo") VoucherInfo voucherInfo, @PathVariable("id") Integer voucherInfoId) {
        validateModuleProduct.updateVoucher(true);
        if (voucherInfo == null) {
            throw new BadRequestException("Voucher to update not null!");
        }
        if (voucherInfoId <= 0 || voucherService.findById(voucherInfoId) == null) {
            throw new NotFoundException("VoucherId invalid!");
        }
        voucherService.update(voucherInfo ,voucherInfoId);
        return new ModelAndView("redirect:/san-pham/voucher");
    }

    @PostMapping("/voucher/delete/{id}")
    public ModelAndView deleteVoucher(@PathVariable("id") Integer voucherInfoId) {
        validateModuleProduct.deleteVoucher(true);
        voucherService.detele(voucherInfoId);
        return new ModelAndView("redirect:/san-pham/voucher");
    }
}