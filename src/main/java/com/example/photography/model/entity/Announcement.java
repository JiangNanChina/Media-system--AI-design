package com.example.photography.model.entity;

import com.example.photography.model.enums.AnnouncementType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 公告实体
 */
@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {
    
    @NotBlank(message = "公告标题不能为空")
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "公告内容不能为空")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "发布者不能为空")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "borrowRecords", "studyCheckins", "dutyRecords", "leaveRequests"})
    private User author;
    
    @Column(name = "published", nullable = false)
    private Boolean published = false;
    
    @Enumerated(EnumType.STRING)
    @NotNull(message = "公告类型不能为空")
    @Column(name = "type", nullable = false, length = 20)
    private AnnouncementType type = AnnouncementType.GENERAL; // 公告类型，默认为一般通知
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 0; // 优先级，数字越大优先级越高
    
    @Column(name = "view_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L; // 查看次数
    
    @Column(name = "publish_time")
    private java.time.LocalDateTime publishedAt; // 发布时间
    
    @Column(name = "archived", nullable = false)
    private Boolean archived = false; // 是否归档，归档后不会在登录弹窗显示
    
    // Constructors
    public Announcement() {}
    
    public Announcement(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
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
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public Boolean getPublished() {
        return published;
    }
    
    public void setPublished(Boolean published) {
        this.published = published;
    }
    
    public AnnouncementType getType() {
        return type;
    }
    
    public void setType(AnnouncementType type) {
        this.type = type;
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
    
    public java.time.LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(java.time.LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public Boolean getArchived() {
        return archived;
    }
    
    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
