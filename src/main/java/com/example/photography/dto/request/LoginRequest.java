package com.example.photography.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
public class LoginRequest {
    
    @NotBlank(message = "账号不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @Valid
    private DeviceInfoRequest deviceInfo;
    
    // Constructors
    public LoginRequest() {}
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public DeviceInfoRequest getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(DeviceInfoRequest deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
