package com.example.photography.dto.response;

import com.example.photography.model.enums.UserRole;

/**
 * 用户注册响应DTO
 */
public class RegisterResponse {
    
    private Long id;
    private String username;
    private String realName;
    private String email;
    private UserRole role;
    private String departmentName;
    private String message;
    
    // 构造函数
    public RegisterResponse() {}
    
    public RegisterResponse(Long id, String username, String realName, String email, 
                          UserRole role, String departmentName, String message) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.role = role;
        this.departmentName = departmentName;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "RegisterResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", departmentName='" + departmentName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
