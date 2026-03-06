package com.teacher.management.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * 安全工具类
 * 从 SecurityContext 中快速获取当前用户信息（来自 JWT claims，零数据库查询）
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的 userId
     * 
     * @return userId，未登录时返回 null
     */
    public static Long getCurrentUserId() {
        return getDetailValue("userId", Long.class);
    }

    /**
     * 获取当前登录用户的 deptId
     * 
     * @return deptId，未登录或无部门时返回 null
     */
    public static Long getCurrentDeptId() {
        return getDetailValue("deptId", Long.class);
    }

    /**
     * 获取当前登录用户的 roleId
     * 
     * @return roleId，未登录时返回 null
     */
    public static Long getCurrentRoleId() {
        return getDetailValue("roleId", Long.class);
    }

    /**
     * 获取当前登录用户的用户名
     * 
     * @return username，未登录时返回 null
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /**
     * 从 Authentication.getDetails() 的 Map 中取值
     */
    @SuppressWarnings("unchecked")
    private static <T> T getDetailValue(String key, Class<T> clazz) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            return null;
        }
        try {
            Map<String, Object> details = (Map<String, Object>) auth.getDetails();
            Object val = details.get(key);
            if (val == null)
                return null;
            if (clazz == Long.class && val instanceof Number) {
                return clazz.cast(((Number) val).longValue());
            }
            return clazz.cast(val);
        } catch (ClassCastException e) {
            return null;
        }
    }
}
