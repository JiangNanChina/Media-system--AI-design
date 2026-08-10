package com.example.photography.service;

import com.example.photography.dto.request.LoginRequest;
import com.example.photography.dto.request.RegisterRequest;
import com.example.photography.dto.response.LoginResponse;
import com.example.photography.dto.response.RegisterResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户登录（带设备验证）
     */
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    
    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(String token);

    LoginResponse issueAccessToken(Long userId);
    
    /**
     * 验证令牌
     */
    boolean validateToken(String token);
    
    /**
     * 获取当前用户信息
     */
    LoginResponse getCurrentUser();
    
    /**
     * 用户注册
     */
    RegisterResponse register(RegisterRequest request);
    
    /**
     * 验证管理员密钥
     */
}
