package com.example.photography.service;

/**
 * 邮件发送服务
 */
public interface EmailDeliveryService {
    void sendHtmlMail(String to, String subject, String htmlContent);

    void sendTestMail(String to);
}
