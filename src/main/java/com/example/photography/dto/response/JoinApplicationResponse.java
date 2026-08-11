package com.example.photography.dto.response;

import com.example.photography.model.entity.JoinApplication;
import com.example.photography.model.enums.Gender;
import com.example.photography.model.enums.JoinApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JoinApplicationResponse {
    private Long id;
    private String applicationNumber;
    private String realName;
    private String qqEmail;
    private String phone;
    private Gender gender;
    private String genderDescription;
    private String college;
    private String major;
    private Integer enrollmentYear;
    private String selfIntroduction;
    private String workOriginalFilename;
    private String workMimeType;
    private Long workFileSize;
    private Boolean hasWork;
    private JoinApplicationStatus status;
    private String statusDescription;
    private String reviewerName;
    private String reviewFeedback;
    private LocalDateTime reviewedAt;
    private String interviewQqGroup;
    private Boolean notificationSent;
    private LocalDateTime notifiedAt;
    private String notificationError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JoinApplicationResponse from(JoinApplication entity) {
        JoinApplicationResponse response = new JoinApplicationResponse();
        response.setId(entity.getId());
        response.setApplicationNumber(entity.getApplicationNumber());
        response.setRealName(entity.getRealName());
        response.setQqEmail(entity.getQqEmail());
        response.setPhone(entity.getPhone());
        response.setGender(entity.getGender());
        response.setGenderDescription(entity.getGender() != null ? entity.getGender().getDescription() : "");
        response.setCollege(entity.getCollege());
        response.setMajor(entity.getMajor());
        response.setEnrollmentYear(entity.getEnrollmentYear());
        response.setSelfIntroduction(entity.getSelfIntroduction());
        response.setWorkOriginalFilename(entity.getWorkOriginalFilename());
        response.setWorkMimeType(entity.getWorkMimeType());
        response.setWorkFileSize(entity.getWorkFileSize());
        response.setHasWork(entity.getWorkStoredFilename() != null && !entity.getWorkStoredFilename().isBlank());
        response.setStatus(entity.getStatus());
        response.setStatusDescription(entity.getStatus() != null ? entity.getStatus().getDescription() : "");
        response.setReviewerName(entity.getReviewer() != null ? entity.getReviewer().getRealName() : null);
        response.setReviewFeedback(entity.getReviewFeedback());
        response.setReviewedAt(entity.getReviewedAt());
        response.setInterviewQqGroup(entity.getInterviewQqGroup());
        response.setNotificationSent(Boolean.TRUE.equals(entity.getNotificationSent()));
        response.setNotifiedAt(entity.getNotifiedAt());
        response.setNotificationError(entity.getNotificationError());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
