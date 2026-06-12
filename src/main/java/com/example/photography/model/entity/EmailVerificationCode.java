package com.example.photography.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 邮箱验证码实体，只保存验证码哈希。
 */
@Entity
@Table(
        name = "email_verification_codes",
        indexes = {
                @Index(name = "idx_email_code_email_purpose", columnList = "email,purpose"),
                @Index(name = "idx_email_code_expires_at", columnList = "expires_at")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailVerificationCode extends BaseEntity {
    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "purpose", nullable = false, length = 50)
    private String purpose;

    @Column(name = "code_hash", nullable = false, length = 120)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;
}
