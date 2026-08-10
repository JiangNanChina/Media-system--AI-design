package com.example.photography.model.entity;

import com.example.photography.model.enums.LandingSectionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "landing_content_items")
@Data
@EqualsAndHashCode(callSuper = true)
public class LandingContentItem extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 40)
    private LandingSectionType sectionType;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(nullable = false)
    private Boolean published = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
