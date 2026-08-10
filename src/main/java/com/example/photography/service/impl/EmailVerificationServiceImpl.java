package com.example.photography.service.impl;

import com.example.photography.model.entity.EmailNotificationLog;
import com.example.photography.model.entity.EmailVerificationCode;
import com.example.photography.repository.EmailNotificationLogRepository;
import com.example.photography.repository.EmailVerificationCodeRepository;
import com.example.photography.service.EmailDeliveryService;
import com.example.photography.service.EmailVerificationService;
import com.example.photography.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 注册邮箱验证码服务。
 */
@Service
@Slf4j
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final String PURPOSE_REGISTER = "REGISTER";
    private static final String TYPE_REGISTER_CODE = "REGISTER_CODE";
    private static final String BUSINESS_EMAIL_VERIFICATION_CODE = "EMAIL_VERIFICATION_CODE";
    private static final int EXPIRE_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private EmailVerificationCodeRepository emailVerificationCodeRepository;

    @Autowired
    private EmailNotificationLogRepository emailNotificationLogRepository;

    @Autowired
    private EmailDeliveryService emailDeliveryService;

    @Autowired
    private MailSettingsService mailSettingsService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(noRollbackFor = RuntimeException.class)
    public void sendRegisterCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userService.existsByEmail(normalizedEmail)) {
            return;
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        LocalDateTime now = LocalDateTime.now();

        List<EmailVerificationCode> previousCodes = emailVerificationCodeRepository
                .findByEmailAndPurposeAndUsedAtIsNullAndDeletedFalse(normalizedEmail, PURPOSE_REGISTER);
        previousCodes.forEach(item -> item.setUsedAt(now));
        emailVerificationCodeRepository.saveAll(previousCodes);

        EmailVerificationCode verificationCode = new EmailVerificationCode();
        verificationCode.setEmail(normalizedEmail);
        verificationCode.setPurpose(PURPOSE_REGISTER);
        verificationCode.setCodeHash(passwordEncoder.encode(code));
        verificationCode.setExpiresAt(now.plusMinutes(EXPIRE_MINUTES));
        verificationCode.setAttemptCount(0);
        emailVerificationCodeRepository.save(verificationCode);

        try {
            emailDeliveryService.sendHtmlMail(
                    normalizedEmail,
                    "注册邮箱验证码",
                    buildRegisterCodeContent(code)
            );
            saveEmailSendLog(verificationCode, normalizedEmail, true, null);
        } catch (RuntimeException e) {
            verificationCode.setDeleted(true);
            emailVerificationCodeRepository.save(verificationCode);
            saveEmailSendLog(verificationCode, normalizedEmail, false, e.getMessage());
            throw e;
        }
    }

    @Override
    public void verifyRegisterCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("邮箱验证码不能为空");
        }

        EmailVerificationCode verificationCode = emailVerificationCodeRepository
                .findTopByEmailAndPurposeAndUsedAtIsNullAndDeletedFalseOrderByCreatedAtDesc(normalizedEmail, PURPOSE_REGISTER)
                .orElseThrow(() -> new RuntimeException("邮箱验证码不存在或已失效，请重新获取"));

        LocalDateTime now = LocalDateTime.now();
        if (verificationCode.getExpiresAt().isBefore(now)) {
            throw new RuntimeException("邮箱验证码已过期，请重新获取");
        }
        if (verificationCode.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new RuntimeException("邮箱验证码错误次数过多，请重新获取");
        }

        if (!passwordEncoder.matches(code.trim(), verificationCode.getCodeHash())) {
            verificationCode.setAttemptCount(verificationCode.getAttemptCount() + 1);
            emailVerificationCodeRepository.save(verificationCode);
            if (verificationCode.getAttemptCount() >= MAX_ATTEMPTS) {
                throw new RuntimeException("邮箱验证码错误次数过多，请重新获取");
            }
            throw new RuntimeException("邮箱验证码错误");
        }

        verificationCode.setUsedAt(now);
        emailVerificationCodeRepository.save(verificationCode);
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("邮箱不能为空");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String buildRegisterCodeContent(String code) {
        return EmailTemplateRenderer.renderRegisterCode(code, EXPIRE_MINUTES);
    }

    private void saveEmailSendLog(EmailVerificationCode verificationCode, String email, boolean success, String errorMessage) {
        LocalDateTime sentAt = LocalDateTime.now();
        EmailNotificationLog logEntity = new EmailNotificationLog();
        logEntity.setNotificationType(TYPE_REGISTER_CODE);
        logEntity.setBusinessType(BUSINESS_EMAIL_VERIFICATION_CODE);
        logEntity.setBusinessId(verificationCode.getId() == null ? 0L : verificationCode.getId());
        logEntity.setRecipientEmail(email);
        logEntity.setRecipientName("注册用户");
        logEntity.setPeriodKey("register-" + sentAt.format(PERIOD_FORMATTER));
        logEntity.setSuccess(success);
        logEntity.setErrorMessage(truncate(errorMessage, 1000));
        logEntity.setSentAt(sentAt);
        emailNotificationLogRepository.save(logEntity);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
