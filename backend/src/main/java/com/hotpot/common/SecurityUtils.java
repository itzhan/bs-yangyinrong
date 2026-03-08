package com.hotpot.common;

import com.hotpot.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getEmployee().getId() : null;
    }

    public static String getCurrentUsername() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    public static String getCurrentRealName() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getEmployee().getRealName() : null;
    }
}
