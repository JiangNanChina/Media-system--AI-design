package com.example.photography.service.impl;

import com.example.photography.service.EmailDeliveryService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 基于站点配置动态创建QQ邮箱SMTP发送器。
 */
@Service
@Slf4j
public class EmailDeliveryServiceImpl implements EmailDeliveryService {
    @Autowired
    private MailSettingsService mailSettingsService;

    @Override
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        if (!StringUtils.hasText(to)) {
            throw new RuntimeException("收件邮箱不能为空");
        }

        MailSettingsService.MailSettings settings = mailSettingsService.getRequiredSettings();
        JavaMailSenderImpl mailSender = createMailSender(settings);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(
                    settings.getQqAccount(),
                    StringUtils.hasText(settings.getSenderName()) ? settings.getSenderName() : "融媒体管理系统",
                    StandardCharsets.UTF_8.name()
            ));
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送失败: " + buildFriendlyErrorMessage(e), e);
        }
    }

    @Override
    public void sendTestMail(String to) {
        sendHtmlMail(
                to,
                "QQ邮箱配置测试",
                EmailTemplateRenderer.renderTestMail()
        );
    }

    private JavaMailSenderImpl createMailSender(MailSettingsService.MailSettings settings) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(settings.getSmtpHost());
        mailSender.setPort(settings.getSmtpPort());
        mailSender.setUsername(settings.getQqAccount());
        mailSender.setPassword(settings.getQqAuthCode());
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", String.valueOf(settings.isSmtpSslEnabled()));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        if (settings.isSmtpSslEnabled()) {
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.socketFactory.fallback", "false");
        }
        return mailSender;
    }

    private String buildFriendlyErrorMessage(Exception e) {
        Throwable root = getRootCause(e);
        if (root instanceof UnknownHostException) {
            return "SMTP服务器地址无法解析，请检查站点设置中的 SMTP服务器。QQ邮箱通常填写 smtp.qq.com，不要填写QQ邮箱账号";
        }
        String message = root.getMessage();
        if (message != null && message.toLowerCase().contains("authentication")) {
            return "SMTP认证失败，请确认QQ邮箱账号和SMTP授权码正确，授权码不是QQ登录密码";
        }
        if (message != null && message.toLowerCase().contains("connect")) {
            return "无法连接SMTP服务器，请确认SMTP服务器、端口和SSL设置正确。QQ邮箱推荐 smtp.qq.com、端口465、开启SSL";
        }
        return e.getMessage();
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
