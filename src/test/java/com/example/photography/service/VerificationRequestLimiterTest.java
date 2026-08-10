package com.example.photography.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationRequestLimiterTest {
    @Test
    void rejectsRepeatedRequestsForTheSameEmailDuringCooldown() {
        VerificationRequestLimiter limiter = new VerificationRequestLimiter();
        limiter.check("10001@qq.com", "REGISTER", "192.0.2.1");

        assertThatThrownBy(() -> limiter.check("10001@qq.com", "REGISTER", "192.0.2.2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("频繁");
    }

    @Test
    void limitsRequestsFromOneIpAcrossDifferentEmails() {
        VerificationRequestLimiter limiter = new VerificationRequestLimiter();
        for (int i = 0; i < 20; i++) {
            limiter.check("user" + i + "@qq.com", "PASSWORD_RESET", "192.0.2.10");
        }

        assertThatThrownBy(() -> limiter.check("overflow@qq.com", "PASSWORD_RESET", "192.0.2.10"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("当前网络");
    }
}

