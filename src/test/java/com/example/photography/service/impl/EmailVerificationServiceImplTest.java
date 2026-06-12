package com.example.photography.service.impl;

import com.example.photography.model.entity.EmailNotificationLog;
import com.example.photography.model.entity.EmailVerificationCode;
import com.example.photography.repository.EmailNotificationLogRepository;
import com.example.photography.repository.EmailVerificationCodeRepository;
import com.example.photography.service.EmailDeliveryService;
import com.example.photography.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceImplTest {
    private EmailVerificationCodeRepository codeRepository;
    private EmailNotificationLogRepository logRepository;
    private EmailDeliveryService emailDeliveryService;
    private MailSettingsService mailSettingsService;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private EmailVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        codeRepository = mock(EmailVerificationCodeRepository.class);
        logRepository = mock(EmailNotificationLogRepository.class);
        emailDeliveryService = mock(EmailDeliveryService.class);
        mailSettingsService = mock(MailSettingsService.class);
        userService = mock(UserService.class);
        passwordEncoder = new BCryptPasswordEncoder();

        service = new EmailVerificationServiceImpl();
        ReflectionTestUtils.setField(service, "emailVerificationCodeRepository", codeRepository);
        ReflectionTestUtils.setField(service, "emailNotificationLogRepository", logRepository);
        ReflectionTestUtils.setField(service, "emailDeliveryService", emailDeliveryService);
        ReflectionTestUtils.setField(service, "mailSettingsService", mailSettingsService);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
    }

    @Test
    void sendRegisterCodeStoresOnlyHashAndSendsMail() {
        when(userService.existsByEmail("user@qq.com")).thenReturn(false);
        when(codeRepository.findByEmailAndPurposeAndUsedAtIsNullAndDeletedFalse("user@qq.com", "REGISTER"))
                .thenReturn(Collections.emptyList());
        when(codeRepository.save(any(EmailVerificationCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendRegisterCode("User@qq.com");

        ArgumentCaptor<EmailVerificationCode> captor = ArgumentCaptor.forClass(EmailVerificationCode.class);
        verify(codeRepository).save(captor.capture());
        EmailVerificationCode savedCode = captor.getValue();

        assertThat(savedCode.getEmail()).isEqualTo("user@qq.com");
        assertThat(savedCode.getCodeHash()).isNotBlank();
        assertThat(savedCode.getCodeHash()).doesNotMatch("\\d{6}");
        assertThat(savedCode.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(emailDeliveryService).sendHtmlMail(eq("user@qq.com"), eq("注册邮箱验证码"), any(String.class));
        verify(logRepository).save(any(EmailNotificationLog.class));
    }

    @Test
    void verifyRegisterCodeMarksCodeAsUsed() {
        EmailVerificationCode code = buildCode("123456");
        when(codeRepository.findTopByEmailAndPurposeAndUsedAtIsNullAndDeletedFalseOrderByCreatedAtDesc("user@qq.com", "REGISTER"))
                .thenReturn(Optional.of(code));

        service.verifyRegisterCode("user@qq.com", "123456");

        assertThat(code.getUsedAt()).isNotNull();
        verify(codeRepository).save(code);
    }

    @Test
    void verifyRegisterCodeIncrementsAttemptsOnWrongCode() {
        EmailVerificationCode code = buildCode("123456");
        when(codeRepository.findTopByEmailAndPurposeAndUsedAtIsNullAndDeletedFalseOrderByCreatedAtDesc("user@qq.com", "REGISTER"))
                .thenReturn(Optional.of(code));

        assertThatThrownBy(() -> service.verifyRegisterCode("user@qq.com", "000000"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("邮箱验证码错误");

        assertThat(code.getAttemptCount()).isEqualTo(1);
        verify(codeRepository).save(code);
    }

    private EmailVerificationCode buildCode(String rawCode) {
        EmailVerificationCode code = new EmailVerificationCode();
        code.setEmail("user@qq.com");
        code.setPurpose("REGISTER");
        code.setCodeHash(passwordEncoder.encode(rawCode));
        code.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        code.setAttemptCount(0);
        return code;
    }
}
