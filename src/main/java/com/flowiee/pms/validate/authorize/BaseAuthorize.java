package com.flowiee.pms.validate.authorize;

import com.flowiee.pms.exception.AuthenticationException;
import com.flowiee.pms.exception.ForbiddenException;
import com.flowiee.pms.utils.AppConstants;
import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.constants.ACTION;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class BaseAuthorize {
    @SneakyThrows
    protected boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            throw new AuthenticationException();
        }
        if ("anonymousUser".equalsIgnoreCase(authentication.getPrincipal().toString())) {
            throw new AuthenticationException();
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ANONYMOUS".equalsIgnoreCase(authority.getAuthority())) {
                throw new AuthenticationException();
            }
        }
        return authentication.isAuthenticated();
    }
    
    protected boolean isAuthorized(ACTION action, boolean throwException) {
        if (isAuthenticated()) {
            if (AppConstants.ADMINISTRATOR.equals(CommonUtils.getUserPrincipal().getUsername())) {
                return true;
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals(action.name())) {
                    return true;
                }
            }
            if (throwException) {
                throw new ForbiddenException("You are not authorized to use this function!");
            } else {
                return false;
            }
        }
        throw new AuthenticationException();
    }

    protected boolean vldAdminRole() {
        if (CommonUtils.getUserPrincipal().isAdmin()) {
            return true;
        }
        throw new ForbiddenException("This function is for administrator use only!");
    }
}