package com.example.photography.service;

import com.example.photography.dto.response.DynamicQrCodeResponse;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.repository.CheckinConfigurationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * 动态签到二维码签发与校验服务。
 *
 * <p>二维码由服务端签名，客户端无法伪造。前端会在令牌过期前轮换；已扫码的令牌
 * 在自身有效期内仍可提交，避免恰逢页面刷新时误判为失效。</p>
 */
@Service
public class DynamicQrCodeService {

    private static final int TOKEN_VERSION = 2;
    private static final String TOKEN_TYPE = "CHECKIN";
    private static final long CLOCK_SKEW_SECONDS = 30;

    private final CheckinConfigurationRepository configurationRepository;
    private final ObjectMapper objectMapper;
    private final byte[] signingSecret;
    private final long validitySeconds;
    private final long refreshAfterSeconds;

    public DynamicQrCodeService(
            CheckinConfigurationRepository configurationRepository,
            ObjectMapper objectMapper,
            @Value("${checkin.qr.secret:${jwt.secret:change-me-to-a-strong-512-bit-secret-before-deploy}}") String signingSecret,
            @Value("${checkin.qr.validity-seconds:90}") long validitySeconds,
            @Value("${checkin.qr.refresh-after-seconds:45}") long refreshAfterSeconds) {
        if (signingSecret == null || signingSecret.isBlank()) {
            throw new IllegalArgumentException("动态二维码签名密钥不能为空");
        }
        if (validitySeconds < 30 || validitySeconds > 300) {
            throw new IllegalArgumentException("动态二维码有效期必须在30到300秒之间");
        }
        if (refreshAfterSeconds < 10 || refreshAfterSeconds >= validitySeconds) {
            throw new IllegalArgumentException("动态二维码刷新时间必须小于有效期且不少于10秒");
        }
        this.configurationRepository = configurationRepository;
        this.objectMapper = objectMapper;
        this.signingSecret = signingSecret.getBytes(StandardCharsets.UTF_8);
        this.validitySeconds = validitySeconds;
        this.refreshAfterSeconds = refreshAfterSeconds;
    }

    @Transactional
    public DynamicQrCodeResponse issue(Long configurationId) {
        CheckinConfiguration configuration = configurationRepository.findById(configurationId)
                .orElseThrow(() -> new RuntimeException("打卡配置不存在"));
        if (Boolean.TRUE.equals(configuration.getDeleted())) {
            throw new RuntimeException("打卡配置已删除");
        }
        if (!Boolean.TRUE.equals(configuration.getIsActive())) {
            throw new RuntimeException("打卡配置未启用，无法生成二维码");
        }

        Instant generatedAt = Instant.now();
        Instant expiresAt = generatedAt.plusSeconds(validitySeconds);
        String nonce = UUID.randomUUID().toString();
        String signature = sign(configurationId, generatedAt.toEpochMilli(), expiresAt.toEpochMilli(), nonce);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("type", TOKEN_TYPE);
        payload.put("version", TOKEN_VERSION);
        payload.put("configId", configurationId);
        payload.put("name", configuration.getName());
        payload.put("location", configuration.getLocationName());
        payload.put("session", configuration.getSessionName());
        payload.put("generateTime", generatedAt.toString());
        payload.put("expireTime", expiresAt.toString());
        payload.put("issuedAt", generatedAt.toEpochMilli());
        payload.put("expiresAt", expiresAt.toEpochMilli());
        payload.put("nonce", nonce);
        payload.put("signature", signature);

        String content;
        try {
            content = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("动态二维码内容生成失败", e);
        }

        // 保存当前展示的二维码，便于管理端查看和问题排查；验签不依赖一次整配置 PUT。
        configuration.setQrCode(content);
        configurationRepository.save(configuration);

        return DynamicQrCodeResponse.builder()
                .configurationId(configurationId)
                .content(content)
                .generatedAt(generatedAt)
                .expiresAt(expiresAt)
                .refreshAfterSeconds(refreshAfterSeconds)
                .validitySeconds(validitySeconds)
                .build();
    }

    public void validate(String content, Long expectedConfigurationId) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("二维码签到需要提供二维码内容");
        }

        final JsonNode payload;
        try {
            payload = objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("二维码格式无效，请扫描现场动态二维码");
        }

        if (!TOKEN_TYPE.equals(payload.path("type").asText())
                || payload.path("version").asInt() != TOKEN_VERSION) {
            throw new RuntimeException("二维码类型或版本无效，请扫描现场动态二维码");
        }

        long configurationId = requiredLong(payload, "configId");
        long issuedAt = requiredLong(payload, "issuedAt");
        long expiresAt = requiredLong(payload, "expiresAt");
        String nonce = requiredText(payload, "nonce");
        String providedSignature = requiredText(payload, "signature");

        if (!Long.valueOf(configurationId).equals(expectedConfigurationId)) {
            throw new RuntimeException("二维码与当前签到配置不匹配，请重新扫描");
        }

        long now = Instant.now().toEpochMilli();
        if (issuedAt > now + CLOCK_SKEW_SECONDS * 1000) {
            throw new RuntimeException("二维码签发时间异常，请刷新二维码后重试");
        }
        if (expiresAt <= issuedAt || expiresAt - issuedAt > validitySeconds * 1000 + 1000) {
            throw new RuntimeException("二维码有效期异常，请刷新二维码后重试");
        }
        if (now > expiresAt + CLOCK_SKEW_SECONDS * 1000) {
            throw new RuntimeException("二维码已过期，请扫描现场最新二维码");
        }

        String expectedSignature = sign(configurationId, issuedAt, expiresAt, nonce);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new RuntimeException("二维码签名无效，请扫描现场动态二维码");
        }
    }

    private long requiredLong(JsonNode payload, String field) {
        JsonNode value = payload.get(field);
        if (value == null || !value.canConvertToLong()) {
            throw new RuntimeException("二维码缺少必要字段: " + field);
        }
        return value.asLong();
    }

    private String requiredText(JsonNode payload, String field) {
        String value = payload.path(field).asText("");
        if (value.isBlank()) {
            throw new RuntimeException("二维码缺少必要字段: " + field);
        }
        return value;
    }

    private String sign(long configurationId, long issuedAt, long expiresAt, String nonce) {
        String canonical = TOKEN_VERSION + "|" + configurationId + "|" + issuedAt + "|" + expiresAt + "|" + nonce;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("动态二维码签名失败", e);
        }
    }
}
