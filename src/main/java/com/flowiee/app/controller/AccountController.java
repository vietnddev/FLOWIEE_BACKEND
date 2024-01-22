package com.flowiee.app.controller;

import com.flowiee.app.base.BaseController;
import com.flowiee.app.entity.Account;
import com.flowiee.app.exception.ApiException;
import com.flowiee.app.exception.BadRequestException;
import com.flowiee.app.exception.DataExistsException;
import com.flowiee.app.exception.NotFoundException;
import com.flowiee.app.model.ApiResponse;
import com.flowiee.app.model.role.FlowieeRole;
import com.flowiee.app.service.AccountService;
import com.flowiee.app.service.RoleService;
import com.flowiee.app.utils.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${app.api.prefix}/system/account")
@Tag(name = "Account system API", description = "Quản lý tài khoản hệ thống")
public class AccountController extends BaseController {
    @Autowired private AccountService accountService;
    @Autowired private RoleService roleService;

    @Operation(summary = "Find all accounts")
    @GetMapping("/all")
    public ApiResponse<List<Account>> findAllAccounts() {
        if (!super.validateModuleSystem.readAccount(true)) {
            return null;
        }
        try {
            return ApiResponse.ok(accountService.findAll());
        } catch (RuntimeException ex) {
            throw new ApiException(String.format(MessageUtils.SEARCH_ERROR_OCCURRED, "account"));
        }
    }

    @Operation(summary = "Find detail account")
    @GetMapping("/{accountId}")
    public ApiResponse<Account> findDetailAccount(@PathVariable("accountId") Integer accountId) {
        if (!super.validateModuleSystem.readAccount(true)) {
            return null;
        }
        try {
            return ApiResponse.ok(accountService.findById(accountId));
        } catch (RuntimeException ex) {
            throw new ApiException(String.format(MessageUtils.SEARCH_ERROR_OCCURRED, "account"));
        }
    }

    @Operation(summary = "Create account")
    @PostMapping(value = "/create")
    public ApiResponse<Account> save(@RequestBody Account account) {
        if (!super.validateModuleSystem.insertAccount(true)) {
            return null;
        }
        try {
            if (account == null) {
                throw new BadRequestException();
            }
            if (accountService.findByUsername(account.getUsername()) != null) {
                throw new DataExistsException("Username exists!");
            }
            return ApiResponse.ok(accountService.save(account));
        } catch (RuntimeException ex) {
            throw new ApiException(String.format(MessageUtils.UPDATE_ERROR_OCCURRED, "account"));
        }
    }

    @Operation(summary = "Update account")
    @PutMapping(value = "/update/{accountId}")
    public ApiResponse<Account> update(@RequestBody Account account, @PathVariable("accountId") Integer accountId) {
        if (!super.validateModuleSystem.updateAccount(true)) {
            return null;
        }
        try {
            if (accountId <= 0 || accountService.findById(accountId) == null) {
                throw new BadRequestException();
            }
            return ApiResponse.ok(accountService.update(account, accountId));
        } catch (RuntimeException ex) {
            throw new ApiException(String.format(MessageUtils.UPDATE_ERROR_OCCURRED, "account"));
        }
    }

    @PutMapping("/update-permission/{accountId}")
    public ApiResponse<List<FlowieeRole>> updatePermission(@RequestBody String[] actions, @PathVariable("accountId") Integer accountId) {
        if (!super.validateModuleSystem.updateAccount(true)) {
            return null;
        }
        try {
            if (accountId <= 0 || accountService.findById(accountId) == null) {
                throw new BadRequestException();
            }
//            roleService.deleteAllRole(accountId);
//            List<ActionOfModule> listAction = roleService.findAllAction();
//            for (ActionOfModule sysAction : listAction) {
//                String clientActionKey = request.getParameter(sysAction.getActionKey());
//                if (clientActionKey != null) {
//                    boolean isAuthorSelected = clientActionKey.equals("on");
//                    if (isAuthorSelected) {
//                        roleService.updatePermission(sysAction.getModuleKey(), sysAction.getActionKey(), accountId);
//                    }
//                }
//            }
            return ApiResponse.ok(null);
        } catch (RuntimeException ex) {
            throw new ApiException();
        }
    }

    @Operation(summary = "Delete account")
    @DeleteMapping(value = "/delete/{accountId}")
    public ApiResponse<String> deleteAccount(@PathVariable("accountId") Integer accountId) {
        if (!super.validateModuleSystem.deleteAccount(true)) {
            return null;
        }
        try {
            if (accountId <= 0 ||accountService.findById(accountId) == null) {
                throw new NotFoundException("Account not found!");
            }
            return ApiResponse.ok(accountService.delete(accountId));
        } catch (RuntimeException ex) {
            throw new ApiException(String.format(MessageUtils.DELETE_ERROR_OCCURRED, "account"));
        }
    }

    @Operation(summary = "Find roles of account")
    @GetMapping("/{accountId}/role")
    public ApiResponse<List<FlowieeRole>> findRolesOfAccount(@PathVariable("accountId") Integer accountId) {
        if (!super.validateModuleSystem.readAccount(true)) {
            return null;
        }
        try {
            return ApiResponse.ok(roleService.findAllRoleByAccountId(accountId));
        } catch (RuntimeException ex) {
            throw new ApiException(String.format(MessageUtils.SEARCH_ERROR_OCCURRED, "role"));
        }
    }
}