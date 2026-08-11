package com.example.photography.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinApplicationReviewRequest {
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @Size(max = 1000, message = "审核备注不能超过1000字")
    private String feedback;
}
