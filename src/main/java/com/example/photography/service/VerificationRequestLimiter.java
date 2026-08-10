package com.example.photography.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationRequestLimiter {
    private static final Duration WINDOW = Duration.ofHours(1);
    private static final Duration EMAIL_COOLDOWN = Duration.ofSeconds(60);
    private static final int EMAIL_LIMIT = 5;
    private static final int IP_LIMIT = 20;

    private final Map<String, AttemptWindow> emailWindows = new ConcurrentHashMap<>();
    private final Map<String, AttemptWindow> ipWindows = new ConcurrentHashMap<>();

    public void check(String email, String purpose, String clientIp) {
        String normalizedEmail = StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : "missing";
        String normalizedIp = StringUtils.hasText(clientIp) ? clientIp.trim() : "unknown";
        consume(emailWindows, purpose + ":" + normalizedEmail, EMAIL_LIMIT, EMAIL_COOLDOWN,
                "验证码发送过于频繁，请稍后再试");
        consume(ipWindows, purpose + ":" + normalizedIp, IP_LIMIT, Duration.ZERO,
                "当前网络验证码请求过多，请稍后再试");
    }

    private void consume(Map<String, AttemptWindow> windows, String key, int limit,
                         Duration cooldown, String message) {
        AttemptWindow window = windows.computeIfAbsent(key, ignored -> new AttemptWindow());
        synchronized (window) {
            Instant now = Instant.now();
            if (window.startedAt == null || window.startedAt.plus(WINDOW).isBefore(now)) {
                window.startedAt = now;
                window.lastAttemptAt = null;
                window.count = 0;
            }
            if (window.count >= limit || (window.lastAttemptAt != null
                    && window.lastAttemptAt.plus(cooldown).isAfter(now))) {
                throw new IllegalArgumentException(message);
            }
            window.count++;
            window.lastAttemptAt = now;
        }
    }

    private static final class AttemptWindow {
        private Instant startedAt;
        private Instant lastAttemptAt;
        private int count;
    }
}

