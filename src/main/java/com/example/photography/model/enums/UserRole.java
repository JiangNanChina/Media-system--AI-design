package com.example.photography.model.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    MEMBER("成员"),
    MINISTER("部长"),
    DIRECTOR("主任"),
    ADVISOR("指导老师"),
    SUPER_ADMIN("系统超级管理员"),
    /** @deprecated 仅用于兼容旧数据，新账号应使用 SUPER_ADMIN。 */
    @Deprecated
    ADMIN("管理员（兼容）");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN || this == ADMIN;
    }

    public boolean canManageBusiness() {
        return this == MINISTER || this == DIRECTOR || isSuperAdmin();
    }

    public boolean canReviewSubmission() {
        return canManageBusiness();
    }
}
