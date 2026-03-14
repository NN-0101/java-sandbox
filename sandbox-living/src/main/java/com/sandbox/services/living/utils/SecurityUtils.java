package com.sandbox.services.living.utils;

import com.sandbox.services.living.security.user.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @description: 安全上下文工具类
 * @author: 0101
 * @create: 2026/03/14
 */
public class SecurityUtils {

    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static String getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUserId() : null;
    }

    /**
     * 获取当前用户手机号
     * @return 手机号
     */
    public static String getCurrentPhone() {
        CustomUserDetails userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getPhone() : null;
    }

    /**
     * 获取当前用户详情
     * @return CustomUserDetails
     */
    public static CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        return null;
    }

    /**
     * 判断是否已认证
     * @return true/false
     */
    public static boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }
}