package com.example.photography.model.enums;

/**
 * 请假状态枚举
 */
public enum LeaveStatus {
    PENDING("待审核"),
    APPROVED("已批准"),
    REJECTED("已拒绝");
    
    private final String description;
    
    LeaveStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
