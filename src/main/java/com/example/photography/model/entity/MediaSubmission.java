package com.example.photography.model.entity;

import com.example.photography.model.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_submissions", indexes = {
        @Index(name = "idx_submission_number", columnList = "submission_number", unique = true),
        @Index(name = "idx_submission_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class MediaSubmission extends BaseEntity {
    @Column(name = "submission_number", nullable = false, unique = true, length = 40)
    private String submissionNumber;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "submitter_name", nullable = false, length = 80)
    private String submitterName;
    @Column(nullable = false, length = 20)
    private String phone;
    @Column(name = "qq_email", nullable = false, length = 120)
    private String qqEmail;
    @Column(name = "organization", length = 160)
    private String organization;
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
    @Column(name = "review_feedback", length = 1000)
    private String reviewFeedback;
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
