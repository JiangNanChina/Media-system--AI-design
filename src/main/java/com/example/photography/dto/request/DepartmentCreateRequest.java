package com.example.photography.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 部门创建请求DTO
 */
public class DepartmentCreateRequest {
    
    @NotBlank(message = "部门名称不能为空")
    private String name;
    
    private String description;
    
    // Constructors
    public DepartmentCreateRequest() {}
    
    public DepartmentCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
