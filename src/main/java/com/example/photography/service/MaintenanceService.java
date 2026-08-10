package com.example.photography.service;

import com.example.photography.dto.request.SiteConfigRequest;
import com.example.photography.model.entity.SiteConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private static final Duration TOKEN_TTL = Duration.ofHours(2);
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final SiteConfigService siteConfigService;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    @Value("${maintenance.token-secret:${jwt.secret:change-me-to-a-strong-512-bit-secret-before-deploy}}")
    private String tokenSecret;

    public boolean isEnabled() {
        return Boolean.parseBoolean(siteConfigService.getConfigValue(SiteConfig.Keys.MAINTENANCE_ENABLED, "false"));
    }

    public Map<String, Object> publicStatus(String token) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", isEnabled());
        result.put("title", siteConfigService.getConfigValue(SiteConfig.Keys.MAINTENANCE_TITLE, "系统维护中"));
        result.put("message", siteConfigService.getConfigValue(SiteConfig.Keys.MAINTENANCE_MESSAGE,
                "管理平台正在维护，公开站点仍可正常浏览。"));
        result.put("unlocked", verifyToken(token));
        return result;
    }

    public String unlock(String password, String clientKey) {
        AttemptState state = attempts.computeIfAbsent(clientKey, key -> new AttemptState());
        Instant now = Instant.now();
        if (state.lockedUntil != null && state.lockedUntil.isAfter(now)) {
            throw new IllegalArgumentException("尝试次数过多，请15分钟后重试");
        }
        String hash = siteConfigService.getConfigValue(SiteConfig.Keys.MAINTENANCE_PASSWORD_HASH, "");
        if (!StringUtils.hasText(hash) || !passwordEncoder.matches(password, hash)) {
            state.failures++;
            if (state.failures >= MAX_ATTEMPTS) {
                state.lockedUntil = now.plus(LOCK_DURATION);
                state.failures = 0;
            }
            throw new IllegalArgumentException("维护密码错误");
        }
        attempts.remove(clientKey);
        long expires = now.plus(TOKEN_TTL).getEpochSecond();
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String payload = expires + "." + nonce;
        return payload + "." + sign(payload);
    }

    public boolean verifyToken(String token) {
        if (!StringUtils.hasText(token)) return false;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;
        String payload = parts[0] + "." + parts[1];
        try {
            long expires = Long.parseLong(parts[0]);
            return expires > Instant.now().getEpochSecond()
                    && MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            return false;
        }
    }

    public Map<String, Object> saveSettings(Boolean enabled, String password, String title, String message) {
        String currentHash = siteConfigService.getConfigValue(SiteConfig.Keys.MAINTENANCE_PASSWORD_HASH, "");
        if (Boolean.TRUE.equals(enabled) && !StringUtils.hasText(password) && !StringUtils.hasText(currentHash)) {
            throw new IllegalArgumentException("开启维护模式前必须设置维护密码");
        }
        save(SiteConfig.Keys.MAINTENANCE_ENABLED, String.valueOf(Boolean.TRUE.equals(enabled)), SiteConfig.ConfigType.BOOLEAN);
        if (StringUtils.hasText(password)) {
            if (password.length() < 8 || password.length() > 72) {
                throw new IllegalArgumentException("维护密码长度必须为8-72位");
            }
            save(SiteConfig.Keys.MAINTENANCE_PASSWORD_HASH, passwordEncoder.encode(password), SiteConfig.ConfigType.TEXT);
        }
        if (StringUtils.hasText(title)) save(SiteConfig.Keys.MAINTENANCE_TITLE, title.trim(), SiteConfig.ConfigType.TEXT);
        if (StringUtils.hasText(message)) save(SiteConfig.Keys.MAINTENANCE_MESSAGE, message.trim(), SiteConfig.ConfigType.TEXT);
        return publicStatus(null);
    }

    private void save(String key, String value, SiteConfig.ConfigType type) {
        SiteConfigRequest request = new SiteConfigRequest();
        request.setConfigKey(key);
        request.setConfigValue(value);
        request.setConfigType(type);
        request.setDescription("维护模式配置");
        siteConfigService.saveOrUpdateConfig(request);
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("无法生成维护通行凭证", e);
        }
    }

    private static final class AttemptState {
        private int failures;
        private Instant lockedUntil;
    }
}
