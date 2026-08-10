package com.example.photography.dto.response;

import com.example.photography.model.entity.MediaSubmission;
import com.example.photography.model.enums.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MediaSubmissionResponse {
    private Long id;
    private String submissionNumber;
    private String title;
    private String description;
    private String submitterName;
    private String phone;
    private String qqEmail;
    private String organization;
    private String originalFilename;
    private String mimeType;
    private Long fileSize;
    private SubmissionStatus status;
    private String reviewerName;
    private String reviewFeedback;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public static MediaSubmissionResponse from(MediaSubmission value) {
        return MediaSubmissionResponse.builder()
                .id(value.getId()).submissionNumber(value.getSubmissionNumber())
                .title(value.getTitle()).description(value.getDescription())
                .submitterName(value.getSubmitterName()).phone(value.getPhone())
                .qqEmail(value.getQqEmail()).organization(value.getOrganization())
                .originalFilename(value.getOriginalFilename()).mimeType(value.getMimeType())
                .fileSize(value.getFileSize()).status(value.getStatus())
                .reviewerName(value.getReviewer() == null ? null : value.getReviewer().getRealName())
                .reviewFeedback(value.getReviewFeedback()).reviewedAt(value.getReviewedAt())
                .createdAt(value.getCreatedAt()).build();
    }
}
