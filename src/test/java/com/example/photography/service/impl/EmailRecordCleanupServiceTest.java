package com.example.photography.service.impl;

import com.example.photography.repository.EmailNotificationLogRepository;
import com.example.photography.repository.EmailVerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailRecordCleanupServiceTest {
    private MailSettingsService mailSettingsService;
    private EmailNotificationLogRepository logRepository;
    private EmailVerificationCodeRepository codeRepository;
    private EmailRecordCleanupService service;

    @BeforeEach
    void setUp() {
        mailSettingsService = mock(MailSettingsService.class);
        logRepository = mock(EmailNotificationLogRepository.class);
        codeRepository = mock(EmailVerificationCodeRepository.class);
        service = new EmailRecordCleanupService(mailSettingsService, logRepository, codeRepository);
    }

    @Test
    void cleanupExpiredRecordsDeletesLogsAndInactiveCodesBeforeRetentionCutoff() {
        when(mailSettingsService.getLogRetentionDays()).thenReturn(30);
        when(logRepository.deleteBySentAtBefore(any(LocalDateTime.class))).thenReturn(12);
        when(codeRepository.deleteExpiredOrInactiveBefore(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(5);

        EmailRecordCleanupService.CleanupResult result = service.cleanupExpiredRecords();

        assertThat(result.getRetentionDays()).isEqualTo(30);
        assertThat(result.getDeletedLogs()).isEqualTo(12);
        assertThat(result.getDeletedCodes()).isEqualTo(5);
        assertThat(result.getCutoff()).isBefore(LocalDateTime.now().minusDays(29));
        verify(logRepository).deleteBySentAtBefore(any(LocalDateTime.class));
        verify(codeRepository).deleteExpiredOrInactiveBefore(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}
