package com.example.photography.model.enums;

/**
 * 借用状态枚举
 */
public enum BorrowStatus {
    PENDING("待审核"),
    APPROVED("已批准"),
    REJECTED("已拒绝"),
    BORROWED("已借出"),
    RETURNED("已归还"),
    OVERDUE("已逾期");
    
    private final String description;
    
    BorrowStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
