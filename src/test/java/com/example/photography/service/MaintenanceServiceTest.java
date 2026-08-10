package com.example.photography.service;

import com.example.photography.model.entity.SiteConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaintenanceServiceTest {
    private SiteConfigService siteConfigService;
    private MaintenanceService service;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        siteConfigService = mock(SiteConfigService.class);
        encoder = new BCryptPasswordEncoder(4);
        service = new MaintenanceService(siteConfigService, encoder);
        ReflectionTestUtils.setField(service, "tokenSecret", "test-maintenance-token-secret-with-enough-entropy");
    }

    @Test
    void requiresAPasswordBeforeMaintenanceCanBeEnabled() {
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAINTENANCE_PASSWORD_HASH, ""))
                .thenReturn("");

        assertThatThrownBy(() -> service.saveSettings(true, "", "维护", "升级中"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("必须设置维护密码");
    }

    @Test
    void issuesAndVerifiesATwoHourMaintenancePass() {
        when(siteConfigService.getConfigValue(SiteConfig.Keys.MAINTENANCE_PASSWORD_HASH, ""))
                .thenReturn(encoder.encode("maintenance-pass"));

        String token = service.unlock("maintenance-pass", "192.0.2.1");

        assertThat(service.verifyToken(token)).isTrue();
        assertThat(service.verifyToken(token + "tampered")).isFalse();
    }

    @Test
    void locksAnIpAfterFiveWrongPasswords() {
        when(siteConfigService.getConfigValue(anyString(), anyString()))
                .thenReturn(encoder.encode("maintenance-pass"));

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> service.unlock("wrong-pass", "192.0.2.1"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        assertThatThrownBy(() -> service.unlock("maintenance-pass", "192.0.2.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("15分钟");
    }
}

