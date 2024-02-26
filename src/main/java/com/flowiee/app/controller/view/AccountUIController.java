package com.flowiee.app.controller.view;

import com.flowiee.app.base.BaseController;
import com.flowiee.app.exception.DataExistsException;
import com.flowiee.app.exception.NotFoundException;
import com.flowiee.app.utils.CommonUtils;
import com.flowiee.app.utils.EndPointUtil;
import com.flowiee.app.utils.PagesUtils;
import com.flowiee.app.entity.Account;
import com.flowiee.app.model.role.ActionOfModule;
import com.flowiee.app.model.role.FlowieeRole;
import com.flowiee.app.model.role.Role;
import com.flowiee.app.security.ValidateModuleSystem;
import com.flowiee.app.service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(EndPointUtil.SYS_ACCOUNT)
public class AccountUIController extends BaseController {
    @Autowired private RoleService roleService;
    @Autowired private ValidateModuleSystem validateModuleSystem;

    @GetMapping
    public ModelAndView findAllAccount() {
        validateModuleSystem.readAccount(true);
        ModelAndView modelAndView = new ModelAndView(PagesUtils.SYS_ACCOUNT);
        modelAndView.addObject("account", new Account());
        modelAndView.addObject("listAccount", accountService.findAll());
        List<Role> newRole = new ArrayList<>();
        modelAndView.addObject("list", newRole);
        return baseView(modelAndView);
    }

    @GetMapping(value = "/{id}")
    public ModelAndView findDetailAccountById(@PathVariable("id") Integer accountId) {
        validateModuleSystem.readAccount(true);
        if (accountId <= 0 || accountService.findById(accountId) == null) {
            throw new NotFoundException("Account not found!");
        }
        ModelAndView modelAndView = new ModelAndView(PagesUtils.SYS_ACCOUNT_DETAIL);
        List<FlowieeRole> roleOfAccount = roleService.findAllRoleByAccountId(accountId);
        modelAndView.addObject("listRole", roleOfAccount);
        modelAndView.addObject("accountInfo", accountService.findById(accountId));
        return baseView(modelAndView);
    }

    @PostMapping(value = "/insert")
    public ModelAndView save(@ModelAttribute("account") Account account) {
        validateModuleSystem.insertAccount(true);
        if (accountService.findByUsername(account.getUsername()) != null) {
            throw new DataExistsException("Username exists!");
        }
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        String password = account.getPassword();
        account.setPassword(bCrypt.encode(password));
        accountService.save(account);
        return new ModelAndView("redirect:/sys/tai-khoan");
    }

    @PostMapping(value = "/update/{id}")
    public ModelAndView update(@ModelAttribute("account") Account accountEntity,
                               @PathVariable("id") Integer accountId,
                               HttpServletRequest request) {
        validateModuleSystem.updateAccount(true);
        if (accountId <= 0 || accountService.findById(accountId) == null) {
            throw new NotFoundException("Account not found!");
        }
        Account acc = accountService.findById(accountId);
        accountEntity.setId(accountId);
        accountEntity.setUsername(acc.getUsername());
        accountEntity.setPassword(acc.getPassword());
        accountEntity.setLastUpdatedBy(CommonUtils.getCurrentAccountUsername());
        accountService.update(accountEntity, accountId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping(value = "/delete/{id}")
    public ModelAndView deleteAccount(@PathVariable("id") Integer accountId) {
        validateModuleSystem.deleteAccount(true);
        if (accountId <= 0 ||accountService.findById(accountId) == null) {
            throw new NotFoundException("Account not found!");
        }
        Account account = accountService.findById(accountId);
        account.setTrangThai(false);
        accountService.save(account);
        return new ModelAndView("redirect:/sys/tai-khoan");
    }

    @PostMapping("/update-permission/{id}")
    public ModelAndView updatePermission(@PathVariable("id") Integer accountId, HttpServletRequest request) {
        validateModuleSystem.updateAccount(true);
        if (accountId <= 0 || accountService.findById(accountId) == null) {
            throw new NotFoundException("Account not found!");
        }
        roleService.deleteAllRole(accountId);
        List<ActionOfModule> listAction = roleService.findAllAction();
        for (ActionOfModule sysAction : listAction) {
            String clientActionKey = request.getParameter(sysAction.getActionKey());
            if (clientActionKey != null) {
                boolean isAuthorSelected = clientActionKey.equals("on");
                if (isAuthorSelected) {
                    roleService.updatePermission(sysAction.getModuleKey(), sysAction.getActionKey(), accountId);
                }
            }
        }
        return new ModelAndView("redirect:/sys/tai-khoan/" + accountId);
    }
}