package com.example.photography.service.impl;

import com.example.photography.model.entity.SiteConfig;
import com.example.photography.service.SiteConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MailSettingsServiceTest {
    private SiteConfigService siteConfigService;
    private MailSettingsService service;

    @BeforeEach
    void setUp() {
        siteConfigService = mock(SiteConfigService.class);
        service = new MailSettingsService();
        ReflectionTestUtils.setField(service, "siteConfigService", siteConfigService);
    }

    @Test
    void requiredSettingsRejectsEmailAddressAsSmtpHost() {
        mockMailConfig("scemirmt@qq.com", "2332542730@qq.com", "auth-code");

        assertThatThrownBy(() -> service.getRequiredSettings())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP服务器不能填写邮箱账号")
                .hasMessageContaining("smtp.qq.com");
    }

    @Test
    void requiredSettingsRejectsUrlAsSmtpHost() {
        mockMailConfig("https://smtp.qq.com:465", "2332542730@qq.com", "auth-code");

        assertThatThrownBy(() -> service.getRequiredSettings())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不要包含 http、端口或路径");
    }

    @Test
    void requiredSettingsAcceptsQqSmtpHost() {
        mockMailConfig("smtp.qq.com", "2332542730@qq.com", "auth-code");

        MailSettingsService.MailSettings settings = service.getRequiredSettings();

        assertThat(settings.getSmtpHost()).isEqualTo("smtp.qq.com");
        assertThat(settings.getSmtpPort()).isEqualTo(465);
        assertThat(settings.isSmtpSslEnabled()).isTrue();
    }

    private void mockMailConfig(String smtpHost, String qqAccount, String authCode) {
        when(siteConfigService.getConfigValue(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAIL_ENABLED, "false")).thenReturn("true");
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAIL_SMTP_HOST, "smtp.qq.com")).thenReturn(smtpHost);
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAIL_SMTP_PORT, "465")).thenReturn("465");
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAIL_SMTP_SSL_ENABLED, "true")).thenReturn("true");
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAIL_QQ_ACCOUNT, "")).thenReturn(qqAccount);
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAIL_QQ_AUTH_CODE, "")).thenReturn(authCode);
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAIL_SENDER_NAME, "融媒体管理系统")).thenReturn("融媒体管理系统");
    }
}
