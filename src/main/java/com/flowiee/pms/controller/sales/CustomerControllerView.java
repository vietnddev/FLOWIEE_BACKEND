package com.flowiee.pms.controller.sales;

import com.flowiee.pms.controller.BaseController;
import com.flowiee.pms.model.dto.CustomerDTO;
import com.flowiee.pms.entity.sales.CustomerContact;
import com.flowiee.pms.service.sales.CustomerContactService;
import com.flowiee.pms.utils.*;
import com.flowiee.pms.exception.ResourceNotFoundException;
import com.flowiee.pms.service.sales.CustomerService;
import com.flowiee.pms.service.sales.OrderService;

import com.flowiee.pms.utils.constants.ContactType;
import com.flowiee.pms.utils.constants.Pages;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CustomerControllerView extends BaseController {
    OrderService mvOrderService;
    CustomerService mvCustomerService;
    CustomerContactService mvCustomerContactService;

    @GetMapping
    @PreAuthorize("@vldModuleSales.readCustomer(true)")
    public ModelAndView searchCustomer(@Nullable @RequestParam("name") String name,
                                       @Nullable @RequestParam("sex") String sex,
                                       @Nullable @RequestParam("birthday") String birthday,
                                       @Nullable @RequestParam("phone") String phone,
                                       @Nullable @RequestParam("email") String email,
                                       @Nullable @RequestParam("address") String address) {
        ModelAndView modelAndView = new ModelAndView(Pages.PRO_CUSTOMER.getTemplate());
        modelAndView.addObject("listCustomer", mvCustomerService.findAll(-1, -1, name, sex, birthday != null ? DateUtils.convertStringToDate(birthday, "YYYY/MM/dd") : null, phone, email, address));
        modelAndView.addObject("filter_name", name);
        modelAndView.addObject("filter_sex", sex);
        modelAndView.addObject("filter_birthday", birthday);
        modelAndView.addObject("filter_phone", phone);
        modelAndView.addObject("filter_email", email);
        modelAndView.addObject("filter_address", address);
        return baseView(modelAndView);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@vldModuleSales.readCustomer(true)")
    public ModelAndView findCustomerDetail(@PathVariable("id") Long customerId) {
        Optional<CustomerDTO> customerDTO = mvCustomerService.findById(customerId);
        if (customerDTO.isEmpty()) {
            throw new ResourceNotFoundException("Customer not found");
        }
        List<CustomerContact> listContacts = mvCustomerContactService.findContacts(customerId);
        listContacts.forEach(c -> {
            if (c.isPhoneContact()) c.setCode(ContactType.P.getLabel());
            if (c.isEmailContact()) c.setCode(ContactType.E.getLabel());
            if (c.isAddressContact()) c.setCode(ContactType.A.getLabel());
        });
        ModelAndView modelAndView = new ModelAndView(Pages.PRO_CUSTOMER_DETAIL.getTemplate());
        modelAndView.addObject("customerDetail", customerDTO.get());
        modelAndView.addObject("listCustomerContact", listContacts);
        modelAndView.addObject("listDonHang", mvOrderService.findAll());
        return baseView(modelAndView);
    }

    @PostMapping("/insert")
    @PreAuthorize("@vldModuleSales.insertCustomer(true)")
    public ModelAndView insertCustomer(CustomerDTO customer) {
        mvCustomerService.save(customer);
        return new ModelAndView("redirect:/customer");
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("@vldModuleSales.updateCustomer(true)")
    public ModelAndView updateCustomer(@ModelAttribute("customer") CustomerDTO customer, @PathVariable("id") Long customerId) {
        if (customer == null || customerId <= 0 || mvCustomerService.findById(customerId).isEmpty()) {
            throw new ResourceNotFoundException("Customer not found!");
        }
        mvCustomerService.update(customer, customerId);
        return new ModelAndView("redirect:/customer");
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("@vldModuleSales.deleteCustomer(true)")
    public ModelAndView deleteCustomer(@PathVariable("id") Long id) {
        mvCustomerService.delete(id);
        return new ModelAndView("redirect:/customer");
    }

    @PostMapping("/contact/insert")
    @PreAuthorize("@vldModuleSales.updateCustomer(true)")
    public ModelAndView insertCustomerContact(@ModelAttribute("customerContact") CustomerContact customerContact, HttpServletRequest request) {
        if (customerContact == null || customerContact.getCustomer() == null) {
            throw new ResourceNotFoundException("Customer not found!");
        }
        mvCustomerContactService.save(customerContact);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/contact/update/{id}")
    @PreAuthorize("@vldModuleSales.updateCustomer(true)")
    public ModelAndView updateCustomerContact(@ModelAttribute("customerContact") CustomerContact customerContact,
                                              @PathVariable("id") Long customerContactId,
                                              HttpServletRequest request) {
        if (customerContact == null || customerContact.getCustomer() == null) {
            throw new ResourceNotFoundException("Customer not found!");
        }
        mvCustomerContactService.update(customerContact, customerContactId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/contact/delete/{id}")
    @PreAuthorize("@vldModuleSales.updateCustomer(true)")
    public ModelAndView updateCustomerContact(@PathVariable("id") Long customerContactId, HttpServletRequest request) {
        mvCustomerContactService.delete(customerContactId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/contact/use-default/{contactId}")
    @PreAuthorize("@vldModuleSales.updateCustomer(true)")
    public ModelAndView setCustomerContactUseDefault(@RequestParam("customerId") Long customerId,
                                                     @RequestParam("contactCode") String contactCode,
                                                     @PathVariable("contactId") Long contactId,
                                                     HttpServletRequest request) {
        mvCustomerContactService.enableContactUseDefault(customerId, contactCode, contactId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/contact/undefault/{contactId}")
    @PreAuthorize("@vldModuleSales.updateCustomer(true)")
    public ModelAndView setCustomerContactUnUseDefault(@PathVariable("contactId") Long contactId, HttpServletRequest request) {
        if (contactId <= 0 || mvCustomerContactService.findById(contactId).isEmpty()) {
            throw new ResourceNotFoundException("Customer contact not found!");
        }
        mvCustomerContactService.disableContactUnUseDefault(contactId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }
}