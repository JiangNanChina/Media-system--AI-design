package com.example.photography.model.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    MEMBER("成员"),
    ADMIN("管理员");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
