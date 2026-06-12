package com.example.photography.service;

/**
 * 邮箱验证码服务
 */
public interface EmailVerificationService {
    void sendRegisterCode(String email);

    void verifyRegisterCode(String email, String code);
}
