package com.flowiee.pms.base;

import com.flowiee.pms.exception.AuthenticationException;
import com.flowiee.pms.exception.ForbiddenException;
import com.flowiee.pms.utils.CommonUtils;
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
        return authentication.isAuthenticated();
    }
    
    protected boolean isAuthorized(String action, boolean throwException) {
        if (isAuthenticated()) {
            if (CommonUtils.ADMINISTRATOR.equals(CommonUtils.getUserPrincipal().getUsername())) {
                return true;
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals(action)) {
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
}