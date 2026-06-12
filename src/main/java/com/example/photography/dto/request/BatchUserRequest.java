package com.example.photography.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 批量用户操作请求DTO
 */
public class BatchUserRequest {
    
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;
    
    // Constructors
    public BatchUserRequest() {}
    
    public BatchUserRequest(List<Long> userIds) {
        this.userIds = userIds;
    }
    
    // Getters and Setters
    public List<Long> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
}
