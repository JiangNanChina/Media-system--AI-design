package com.example.photography.dto.response;

import com.example.photography.model.enums.UserRole;
import com.example.photography.model.enums.AccountStatus;
import java.time.LocalDateTime;

/**
 * 登录响应DTO
 */
public class LoginResponse {
    
    private String token;
    private String username;
    private String realName;
    private String email;
    private UserRole role;
    private Long userId;
    private String departmentName;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private AccountStatus accountStatus;
    private String loginSessionId;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String token, String username, String realName, String email, UserRole role, 
                        Long userId, String departmentName, String avatarUrl, LocalDateTime createdAt) {
        this.token = token;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.departmentName = departmentName;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
    public String getLoginSessionId() { return loginSessionId; }
    public void setLoginSessionId(String loginSessionId) { this.loginSessionId = loginSessionId; }
}
