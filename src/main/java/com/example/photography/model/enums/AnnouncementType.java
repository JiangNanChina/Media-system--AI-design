package com.example.photography.model.enums;

/**
 * 公告类型枚举
 */
public enum AnnouncementType {
    SYSTEM("系统通知"),
    IMPORTANT("重要公告"),
    GENERAL("一般通知"),
    ACTIVITY("活动公告");
    
    private final String description;
    
    AnnouncementType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
