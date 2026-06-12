package com.example.photography.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户个人资料更新请求DTO
 * 只包含用户可以自己修改的字段
 */
public class UserProfileUpdateRequest {
    
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 6, message = "邮箱验证码长度不能超过6个字符")
    private String emailCode;
    
    @Size(max = 20, message = "电话号码长度不能超过20个字符")
    private String phone;
    
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;
    
    // Constructors
    public UserProfileUpdateRequest() {}
    
    public UserProfileUpdateRequest(String realName, String email, String phone, String avatarUrl) {
        this.realName = realName;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }
    
    // Getters and Setters
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

    public String getEmailCode() {
        return emailCode;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    @Override
    public String toString() {
        return "UserProfileUpdateRequest{" +
                "realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", emailCode='" + (emailCode == null ? null : "***") + '\'' +
                ", phone='" + phone + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
