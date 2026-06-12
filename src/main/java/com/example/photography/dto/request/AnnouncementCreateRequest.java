package com.example.photography.dto.request;

import com.example.photography.model.enums.AnnouncementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 公告创建请求DTO
 */
public class AnnouncementCreateRequest {
    
    @NotBlank(message = "公告标题不能为空")
    private String title;
    
    @NotBlank(message = "公告内容不能为空")
    private String content;
    
    @NotNull(message = "公告类型不能为空")
    private AnnouncementType type = AnnouncementType.GENERAL; // 公告类型
    
    private Boolean published = false; // 是否发布
    
    private Integer priority = 0; // 优先级
    
    // Constructors
    public AnnouncementCreateRequest() {}
    
    public AnnouncementCreateRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    // Getters and Setters
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
}
