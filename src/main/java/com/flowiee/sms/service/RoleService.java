package com.flowiee.sms.service;

import com.flowiee.sms.entity.AccountRole;
import com.flowiee.sms.model.role.ActionModel;
import com.flowiee.sms.model.role.RoleModel;

import java.util.List;

public interface RoleService {
    List<RoleModel> findAllRoleByAccountId(Integer accountId);

    List<ActionModel> findAllAction();

    AccountRole findById(Integer id);

    List<AccountRole> findByAccountId(Integer accountId);

    String updatePermission(String moduleKey, String actionKey, Integer accountId);

    boolean isAuthorized(int accountId, String module, String action);

    String deleteAllRole(Integer accountId);
}