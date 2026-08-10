package com.example.photography.dto.request;

import com.example.photography.model.enums.LandingSectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LandingContentItemRequest {
    @NotNull
    private LandingSectionType sectionType;
    @NotBlank
    private String title;
    private String summary;
    private String mediaUrl;
    private String linkUrl;
    private Boolean published = true;
    private Integer sortOrder = 0;
}
