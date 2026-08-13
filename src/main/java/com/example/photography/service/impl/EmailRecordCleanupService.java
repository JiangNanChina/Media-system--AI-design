package com.example.photography.service.impl;

import com.example.photography.repository.EmailNotificationLogRepository;
import com.example.photography.repository.EmailVerificationCodeRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 清理过期邮件发送日志和验证码记录，避免邮件相关数据持续占用数据库空间。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailRecordCleanupService {
    private final MailSettingsService mailSettingsService;
    private final EmailNotificationLogRepository emailNotificationLogRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;

    @Transactional
    public CleanupResult cleanupExpiredRecords() {
        int retentionDays = mailSettingsService.getLogRetentionDays();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(retentionDays);

        int deletedLogs = emailNotificationLogRepository.deleteBySentAtBefore(cutoff);
        int deletedCodes = emailVerificationCodeRepository.deleteExpiredOrInactiveBefore(cutoff, now);

        if (deletedLogs > 0 || deletedCodes > 0) {
            log.info("邮件记录清理完成: retentionDays={}, cutoff={}, deletedLogs={}, deletedCodes={}",
                    retentionDays, cutoff, deletedLogs, deletedCodes);
        } else {
            log.debug("邮件记录清理完成，无需删除: retentionDays={}, cutoff={}", retentionDays, cutoff);
        }

        return new CleanupResult(retentionDays, cutoff, deletedLogs, deletedCodes);
    }

    @Getter
    public static class CleanupResult {
        private final int retentionDays;
        private final LocalDateTime cutoff;
        private final int deletedLogs;
        private final int deletedCodes;

        public CleanupResult(int retentionDays, LocalDateTime cutoff, int deletedLogs, int deletedCodes) {
            this.retentionDays = retentionDays;
            this.cutoff = cutoff;
            this.deletedLogs = deletedLogs;
            this.deletedCodes = deletedCodes;
        }
    }
}
