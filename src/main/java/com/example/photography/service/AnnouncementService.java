package com.example.photography.service;

import com.example.photography.dto.request.AnnouncementCreateRequest;
import com.example.photography.dto.response.AnnouncementResponse;
import com.example.photography.model.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {
    
    /**
     * 创建公告
     */
    Announcement createAnnouncement(Long createdBy, AnnouncementCreateRequest request);
    
    /**
     * 更新公告
     */
    Announcement updateAnnouncement(Long id, AnnouncementCreateRequest request);
    
    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);
    
    /**
     * 根据ID查找公告
     */
    Announcement findById(Long id);
    
    /**
     * 根据ID查找公告（预加载创建者信息）
     */
    Announcement findByIdWithAuthor(Long id);
    
    /**
     * 发布公告
     */
    void publishAnnouncement(Long id);
    
    /**
     * 取消发布公告
     */
    void unpublishAnnouncement(Long id);
    
    /**
     * 增加查看次数
     */
    void incrementViewCount(Long id);
    
    /**
     * 获取已发布的公告列表（按优先级和时间排序）
     */
    List<Announcement> getPublishedAnnouncements();
    
    /**
     * 分页获取已发布的公告
     */
    Page<Announcement> getPublishedAnnouncements(Pageable pageable);
    
    /**
     * 分页获取已发布的公告（返回DTO，避免序列化问题）
     */
    Page<AnnouncementResponse> getPublishedAnnouncementsResponse(Pageable pageable);
    
    /**
     * 分页获取所有公告（管理员用）
     */
    Page<Announcement> getAllAnnouncements(Pageable pageable);
    
    /**
     * 分页获取所有公告（返回DTO，避免序列化问题）
     */
    Page<AnnouncementResponse> getAllAnnouncementsResponse(Pageable pageable);
    
    /**
     * 搜索公告（返回DTO，支持多条件搜索）
     */
    Page<AnnouncementResponse> searchAnnouncementsResponse(String keyword, String type, Boolean published, Pageable pageable);
    
    /**
     * 根据创建者查找公告
     */
    Page<Announcement> findByCreatedBy(Long createdById, Pageable pageable);
    
    /**
     * 搜索公告
     */
    Page<Announcement> searchAnnouncements(String keyword, Pageable pageable);
    
    /**
     * 获取最新公告（首页用）
     */
    List<Announcement> getLatestAnnouncements(int limit);
    
    /**
     * 获取公告统计信息
     */
    AnnouncementStatistics getAnnouncementStatistics();
    
    /**
     * 获取已发布公告数量
     */
    long getPublishedAnnouncementCount();
    
    /**
     * 归档公告
     */
    void archiveAnnouncement(Long id);
    
    /**
     * 取消归档公告
     */
    void unarchiveAnnouncement(Long id);
    
    /**
     * 创建系统公告（用于系统自动生成的通知）
     */
    Announcement createSystemAnnouncement(String title, String content);
    
    /**
     * 获取登录弹窗公告（排除归档的公告）
     */
    Page<AnnouncementResponse> getLoginPopupAnnouncementsResponse(Pageable pageable);
    
    /**
     * 公告统计信息类
     */
    class AnnouncementStatistics {
        private long totalAnnouncements;
        private long publishedAnnouncements;
        private long unpublishedAnnouncements;
        private long totalViews;
        
        // Constructors, getters and setters
        public AnnouncementStatistics() {}
        
        public AnnouncementStatistics(long totalAnnouncements, long publishedAnnouncements, 
                                    long unpublishedAnnouncements, long totalViews) {
            this.totalAnnouncements = totalAnnouncements;
            this.publishedAnnouncements = publishedAnnouncements;
            this.unpublishedAnnouncements = unpublishedAnnouncements;
            this.totalViews = totalViews;
        }
        
        public long getTotalAnnouncements() { return totalAnnouncements; }
        public void setTotalAnnouncements(long totalAnnouncements) { this.totalAnnouncements = totalAnnouncements; }
        
        public long getPublishedAnnouncements() { return publishedAnnouncements; }
        public void setPublishedAnnouncements(long publishedAnnouncements) { this.publishedAnnouncements = publishedAnnouncements; }
        
        public long getUnpublishedAnnouncements() { return unpublishedAnnouncements; }
        public void setUnpublishedAnnouncements(long unpublishedAnnouncements) { this.unpublishedAnnouncements = unpublishedAnnouncements; }
        
        public long getTotalViews() { return totalViews; }
        public void setTotalViews(long totalViews) { this.totalViews = totalViews; }
    }
}
