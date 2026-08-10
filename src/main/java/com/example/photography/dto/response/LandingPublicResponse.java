package com.example.photography.dto.response;

import com.example.photography.model.entity.LandingContentItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class LandingPublicResponse {
    private Map<String, String> settings;
    private List<LandingContentItem> campusFeatures;
    private List<LandingContentItem> departmentShowcases;
}
