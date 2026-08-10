package com.example.photography.service;

import com.example.photography.model.entity.EmailVerificationCode;
import com.example.photography.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationCodeService {
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
    public static final String VIDEO_SUBMISSION = "VIDEO_SUBMISSION";
    private static final int EXPIRE_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final EmailVerificationCodeRepository repository;
    private final EmailDeliveryService emailDeliveryService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public void send(String email, String purpose, String subject) {
        String normalized = normalize(email);
        LocalDateTime now = LocalDateTime.now();
        List<EmailVerificationCode> active = repository
                .findByEmailAndPurposeAndUsedAtIsNullAndDeletedFalse(normalized, purpose);
        active.forEach(item -> item.setUsedAt(now));
        repository.saveAll(active);

        String code = String.format("%06d", random.nextInt(1_000_000));
        EmailVerificationCode entity = new EmailVerificationCode();
        entity.setEmail(normalized);
        entity.setPurpose(purpose);
        entity.setCodeHash(passwordEncoder.encode(code));
        entity.setExpiresAt(now.plusMinutes(EXPIRE_MINUTES));
        entity.setAttemptCount(0);
        repository.save(entity);

        String content = "<div style=\"font-family:Arial,sans-serif;color:#0f172a\">"
                + "<h2>校融媒体管理系统</h2><p>您的验证码为：</p>"
                + "<p style=\"font-size:28px;font-weight:700;letter-spacing:6px\">" + code + "</p>"
                + "<p>验证码 10 分钟内有效，请勿转发给他人。</p></div>";
        emailDeliveryService.sendHtmlMail(normalized, subject, content);
    }

    public void verify(String email, String purpose, String code) {
        String normalized = normalize(email);
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("邮箱验证码不能为空");
        }
        EmailVerificationCode entity = repository
                .findTopByEmailAndPurposeAndUsedAtIsNullAndDeletedFalseOrderByCreatedAtDesc(normalized, purpose)
                .orElseThrow(() -> new IllegalArgumentException("邮箱验证码不存在或已失效"));
        LocalDateTime now = LocalDateTime.now();
        if (entity.getExpiresAt().isBefore(now) || entity.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException("邮箱验证码已失效，请重新获取");
        }
        if (!passwordEncoder.matches(code.trim(), entity.getCodeHash())) {
            entity.setAttemptCount(entity.getAttemptCount() + 1);
            repository.save(entity);
            throw new IllegalArgumentException("邮箱验证码错误");
        }
        entity.setUsedAt(now);
        repository.save(entity);
    }

    private String normalize(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
