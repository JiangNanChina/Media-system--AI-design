package com.example.photography.model.enums;

/**
 * 部门类型枚举
 */
public enum DepartmentType {
    PHOTOGRAPHY("摄影部"),
    EDITING("采编部"),
    REVIEW("审核部"),
    OPERATIONS("运营部"),
    PUBLICITY("宣传部"),
    CUSTOM("自定义");
    
    private final String description;
    
    DepartmentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
