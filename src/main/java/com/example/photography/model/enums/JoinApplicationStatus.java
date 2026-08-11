package com.example.photography.model.enums;

public enum JoinApplicationStatus {
    PENDING("待审核"),
    INTERVIEW("进入面试"),
    REJECTED("已驳回");

    private final String description;

    JoinApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
