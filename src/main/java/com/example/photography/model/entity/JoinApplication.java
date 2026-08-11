package com.example.photography.model.entity;

import com.example.photography.model.enums.Gender;
import com.example.photography.model.enums.JoinApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "join_applications", indexes = {
        @Index(name = "idx_join_application_number", columnList = "application_number", unique = true),
        @Index(name = "idx_join_application_status", columnList = "status"),
        @Index(name = "idx_join_application_email", columnList = "qq_email")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class JoinApplication extends BaseEntity {
    @Column(name = "application_number", nullable = false, unique = true, length = 40)
    private String applicationNumber;

    @Column(name = "real_name", nullable = false, length = 80)
    private String realName;

    @Column(name = "qq_email", nullable = false, length = 120)
    private String qqEmail;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(nullable = false, length = 160)
    private String college;

    @Column(nullable = false, length = 160)
    private String major;

    @Column(name = "enrollment_year", nullable = false)
    private Integer enrollmentYear;

    @Column(name = "self_introduction", nullable = false, columnDefinition = "TEXT")
    private String selfIntroduction;

    @Column(name = "work_original_filename", length = 255)
    private String workOriginalFilename;

    @Column(name = "work_stored_filename", length = 255)
    private String workStoredFilename;

    @Column(name = "work_mime_type", length = 100)
    private String workMimeType;

    @Column(name = "work_file_size")
    private Long workFileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinApplicationStatus status = JoinApplicationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "review_feedback", length = 1000)
    private String reviewFeedback;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "interview_qq_group", length = 50)
    private String interviewQqGroup;

    @Column(name = "notification_sent", nullable = false)
    private Boolean notificationSent = false;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "notification_error", length = 1000)
    private String notificationError;
}
