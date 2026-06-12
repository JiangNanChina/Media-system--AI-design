package com.example.photography.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 邮件通知日志，用于记录发送结果并防止重复提醒。
 */
@Entity
@Table(
        name = "email_notification_logs",
        indexes = {
                @Index(name = "idx_email_notification_dedupe", columnList = "notification_type,business_id,recipient_email,period_key"),
                @Index(name = "idx_email_notification_sent_at", columnList = "sent_at")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailNotificationLog extends BaseEntity {
    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "recipient_email", nullable = false, length = 120)
    private String recipientEmail;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "period_key", nullable = false, length = 100)
    private String periodKey;

    @Column(name = "success", nullable = false)
    private Boolean success = false;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
