package com.example.photography.dto.response;

import com.example.photography.model.enums.AnnouncementType;
import java.time.LocalDateTime;

/**
 * 公告响应DTO
 */
public class AnnouncementResponse {
    
    private Long id;
    private String title;
    private String content;
    private AnnouncementType type;
    private String createdByName;
    private String createdByUsername;
    private Boolean published;
    private Integer priority;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private Boolean archived;
    
    // Constructors
    public AnnouncementResponse() {}
    
    public AnnouncementResponse(Long id, String title, String content, AnnouncementType type,
                              String createdByName, String createdByUsername,
                              Boolean published, Integer priority, Long viewCount,
                              LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime publishedAt, Boolean archived) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createdByName = createdByName;
        this.createdByUsername = createdByUsername;
        this.published = published;
        this.priority = priority;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.publishedAt = publishedAt;
        this.archived = archived;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public AnnouncementType getType() {
        return type;
    }
    
    public void setType(AnnouncementType type) {
        this.type = type;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public String getCreatedByUsername() {
        return createdByUsername;
    }
    
    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }
    
    public Boolean getPublished() {
        return published;
    }
    
    public void setPublished(Boolean published) {
        this.published = published;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Long getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public Boolean getArchived() {
        return archived;
    }
    
    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
