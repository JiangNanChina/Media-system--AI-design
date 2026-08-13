package com.example.photography.service.impl;

import com.example.photography.service.EmailNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 邮件提醒定时任务。
 */
@Component
@Slf4j
public class EmailReminderScheduler {
    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private EmailRecordCleanupService emailRecordCleanupService;

    @Scheduled(cron = "0 * * * * *")
    public void sendDutyAndCheckinReminders() {
        try {
            emailNotificationService.sendDutyReminders();
            emailNotificationService.sendCheckinReminders();
        } catch (Exception e) {
            log.error("执勤/晚自习邮件提醒任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void sendBorrowOverdueReminders() {
        try {
            emailNotificationService.sendBorrowOverdueReminders();
        } catch (Exception e) {
            log.error("设备逾期归还邮件提醒任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void cleanupExpiredEmailRecords() {
        try {
            emailRecordCleanupService.cleanupExpiredRecords();
        } catch (Exception e) {
            log.error("邮件日志与验证码记录清理任务执行失败", e);
        }
    }
}
