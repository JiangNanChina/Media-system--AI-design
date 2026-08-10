package com.example.photography.dto.request;

import com.example.photography.model.enums.SubmissionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionReviewRequest {
    @NotNull
    private SubmissionStatus status;
    private String feedback;
}
