package com.example.photography.utils;

import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {
    
    private static UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        SecurityUtils.userRepository = userRepository;
    }
    
    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            String username = authentication.getName();
            if (username == null || "anonymousUser".equals(username)) {
                return null;
            }
            
            return userRepository.findByUsernameAndDeletedFalse(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            String username = authentication.getName();
            return "anonymousUser".equals(username) ? null : username;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isCurrentUserAdmin() {
        User user = getCurrentUser();
        return user != null && UserRole.ADMIN.equals(user.getRole());
    }
}
