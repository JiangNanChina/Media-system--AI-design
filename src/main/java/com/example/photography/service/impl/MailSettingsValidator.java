package com.example.photography.service.impl;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 邮件配置校验工具，避免把邮箱账号误填到 SMTP 服务器地址。
 */
final class MailSettingsValidator {
    static final String QQ_SMTP_HOST = "smtp.qq.com";

    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9.-]{0,252}[a-zA-Z0-9]$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private MailSettingsValidator() {
    }

    static String normalizeSmtpHost(String smtpHost) {
        if (!StringUtils.hasText(smtpHost)) {
            return "";
        }
        return smtpHost.trim().toLowerCase();
    }

    static void validateSmtpHost(String smtpHost) {
        String host = normalizeSmtpHost(smtpHost);
        if (!StringUtils.hasText(host)) {
            throw new RuntimeException("SMTP服务器不能为空，QQ邮箱请填写 smtp.qq.com");
        }
        if (host.contains("@")) {
            throw new RuntimeException("SMTP服务器不能填写邮箱账号，请填写 smtp.qq.com；QQ邮箱账号请单独填写在“QQ邮箱账号”中");
        }
        if (host.startsWith("http://") || host.startsWith("https://") || host.contains("/") || host.contains(":")) {
            throw new RuntimeException("SMTP服务器只需要填写服务器域名，QQ邮箱请填写 smtp.qq.com，不要包含 http、端口或路径");
        }
        if (!host.contains(".") || host.contains("..") || !HOST_PATTERN.matcher(host).matches()) {
            throw new RuntimeException("SMTP服务器地址格式不正确，QQ邮箱请填写 smtp.qq.com");
        }
    }

    static void validateQqAccount(String qqAccount) {
        String account = qqAccount == null ? "" : qqAccount.trim();
        if (!StringUtils.hasText(account)) {
            throw new RuntimeException("QQ邮箱账号不能为空");
        }
        if (!EMAIL_PATTERN.matcher(account).matches()) {
            throw new RuntimeException("QQ邮箱账号格式不正确，请填写完整邮箱地址，例如 example@qq.com");
        }
    }
}
