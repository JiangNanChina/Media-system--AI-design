package com.example.photography.service.impl;

import com.example.photography.dto.request.AnnouncementCreateRequest;
import com.example.photography.dto.response.AnnouncementResponse;
import com.example.photography.model.entity.Announcement;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.AnnouncementType;
import com.example.photography.repository.AnnouncementRepository;
import com.example.photography.service.AnnouncementService;
import com.example.photography.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 */
@Service
@Transactional
public class AnnouncementServiceImpl implements AnnouncementService {
    
    @Autowired
    private AnnouncementRepository announcementRepository;
    
    @Autowired
    private UserService userService;
    
    @Override
    public Announcement createAnnouncement(Long createdBy, AnnouncementCreateRequest request) {
        User creator = userService.findById(createdBy);
        
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        announcement.setAuthor(creator);
        announcement.setPublished(request.getPublished());
        announcement.setPriority(request.getPriority());
        announcement.setArchived(false); // 新公告默认不归档
        
        // 如果设置为发布状态，设置发布时间
        if (request.getPublished() != null && request.getPublished()) {
            announcement.setPublishedAt(java.time.LocalDateTime.now());
        }
        
        return announcementRepository.save(announcement);
    }
    
    @Override
    public Announcement updateAnnouncement(Long id, AnnouncementCreateRequest request) {
        Announcement announcement = findById(id);
        
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        
        // 处理发布状态变化
        boolean wasPublished = announcement.getPublished() != null && announcement.getPublished();
        boolean willBePublished = request.getPublished() != null && request.getPublished();
        
        announcement.setPublished(request.getPublished());
        announcement.setPriority(request.getPriority());
        
        // 如果从未发布变为发布，设置发布时间
        if (!wasPublished && willBePublished) {
            announcement.setPublishedAt(java.time.LocalDateTime.now());
        }
        // 如果从发布变为未发布，清空发布时间
        else if (wasPublished && !willBePublished) {
            announcement.setPublishedAt(null);
        }
        
        return announcementRepository.save(announcement);
    }
    
    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = findById(id);
        announcement.setDeleted(true);
        announcementRepository.save(announcement);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Announcement findById(Long id) {
        return announcementRepository.findById(id)
                .filter(announcement -> !announcement.getDeleted())
                .orElseThrow(() -> new RuntimeException("公告不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Announcement findByIdWithAuthor(Long id) {
        return announcementRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
    }
    
    @Override
    public void publishAnnouncement(Long id) {
        Announcement announcement = findById(id);
        announcement.setPublished(true);
        announcement.setPublishedAt(java.time.LocalDateTime.now());
        announcementRepository.save(announcement);
    }
    
    @Override
    public void unpublishAnnouncement(Long id) {
        Announcement announcement = findById(id);
        announcement.setPublished(false);
        announcement.setPublishedAt(null);
        announcementRepository.save(announcement);
    }
    
    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        try {
            announcementRepository.incrementViewCount(id);
        } catch (Exception e) {
            // 忽略查看计数更新失败的异常
            System.err.println("Failed to increment view count for announcement " + id + ": " + e.getMessage());
        }
    }
    
    /**
     * 修复现有数据的viewCount为NULL的问题
     */
    @Transactional
    public void fixNullViewCounts() {
        try {
            announcementRepository.fixNullViewCounts();
            System.out.println("Fixed NULL viewCount values in announcements");
        } catch (Exception e) {
            System.err.println("Failed to fix NULL viewCount values: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Announcement> getPublishedAnnouncements() {
        return announcementRepository.findPublishedAnnouncements();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Announcement> getPublishedAnnouncements(Pageable pageable) {
        try {
            return announcementRepository.findPublishedAnnouncementsPaged(pageable);
        } catch (Exception e) {
            // 如果查询失败，返回空页面
            System.err.println("查询公告失败: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementResponse> getPublishedAnnouncementsResponse(Pageable pageable) {
        try {
            Page<Announcement> announcements = announcementRepository.findPublishedAnnouncementsWithAuthorPaged(pageable);
            return announcements.map(this::convertToAnnouncementResponse);
        } catch (Exception e) {
            System.err.println("Error getting published announcements response: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Announcement> getAllAnnouncements(Pageable pageable) {
        return announcementRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementResponse> getAllAnnouncementsResponse(Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findByDeletedFalseWithAuthorOrderByCreatedAtDesc(pageable);
        return announcements.map(this::convertToAnnouncementResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementResponse> searchAnnouncementsResponse(String keyword, String type, Boolean published, Pageable pageable) {
        try {
            // 将字符串类型转换为枚举
            AnnouncementType typeEnum = null;
            if (type != null && !type.isEmpty()) {
                try {
                    typeEnum = AnnouncementType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    // 如果类型字符串无效，忽略类型过滤
                    System.out.println("Invalid announcement type: " + type);
                }
            }
            
            Page<Announcement> announcements = announcementRepository.searchAnnouncementsWithAuthor(
                keyword, typeEnum, published, pageable);
            return announcements.map(this::convertToAnnouncementResponse);
        } catch (Exception e) {
            System.err.println("Error searching announcements: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Announcement> findByCreatedBy(Long createdById, Pageable pageable) {
        return announcementRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(createdById, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Announcement> searchAnnouncements(String keyword, Pageable pageable) {
        return announcementRepository.searchAnnouncements(keyword, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Announcement> getLatestAnnouncements(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return announcementRepository.findLatestAnnouncements(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AnnouncementStatistics getAnnouncementStatistics() {
        long totalAnnouncements = announcementRepository.countByDeletedFalse();
        long publishedAnnouncements = announcementRepository.countByPublishedTrueAndDeletedFalse();
        long unpublishedAnnouncements = totalAnnouncements - publishedAnnouncements;
        
        // 计算总查看次数
        List<Announcement> allAnnouncements = announcementRepository.findByDeletedFalseOrderByCreatedAtDesc(Pageable.unpaged()).getContent();
        long totalViews = allAnnouncements.stream()
                .mapToLong(announcement -> announcement.getViewCount() != null ? announcement.getViewCount() : 0L)
                .sum();
        
        return new AnnouncementStatistics(totalAnnouncements, publishedAnnouncements, unpublishedAnnouncements, totalViews);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getPublishedAnnouncementCount() {
        return announcementRepository.countByPublishedTrueAndDeletedFalse();
    }
    
    @Override
    @Transactional
    public void archiveAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        
        if (announcement.getDeleted()) {
            throw new RuntimeException("公告已删除，无法归档");
        }
        
        announcement.setArchived(true);
        announcementRepository.save(announcement);
    }
    
    @Override
    @Transactional
    public void unarchiveAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        
        if (announcement.getDeleted()) {
            throw new RuntimeException("公告已删除，无法取消归档");
        }
        
        announcement.setArchived(false);
        announcementRepository.save(announcement);
    }
    
    @Override
    @Transactional
    public Announcement createSystemAnnouncement(String title, String content) {
        // 获取或创建系统用户
        User systemUser = getOrCreateSystemUser();
        
        // 创建系统公告
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPublished(true); // 系统公告自动发布
        announcement.setArchived(false); // 不归档
        announcement.setViewCount(0L);
        announcement.setType(AnnouncementType.SYSTEM); // 设为系统通知类型
        announcement.setPriority(100); // 设置高优先级
        announcement.setPublishedAt(java.time.LocalDateTime.now()); // 设置发布时间
        announcement.setAuthor(systemUser); // 设置系统用户为创建者
        
        return announcementRepository.save(announcement);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementResponse> getLoginPopupAnnouncementsResponse(Pageable pageable) {
        try {
            // 获取已发布且未归档的公告（使用分页查询）
            Page<Announcement> announcements = announcementRepository.findByPublishedTrueAndDeletedFalseAndArchivedFalseOrderByPriorityDescCreatedAtDesc(pageable);
            
            // 转换为DTO
            List<AnnouncementResponse> responseList = announcements.getContent().stream()
                .map(this::convertToAnnouncementResponse)
                .collect(Collectors.toList());
            
            return new PageImpl<>(responseList, pageable, announcements.getTotalElements());
        } catch (Exception e) {
            // 如果出现错误，返回空页面
            System.err.println("获取登录弹窗公告失败: " + e.getMessage());
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }
    
    /**
     * 获取或创建系统用户
     */
    private User getOrCreateSystemUser() {
        // 尝试一些常见的用户ID来找到一个存在的用户作为系统用户
        for (Long id : Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)) {
            try {
                User user = userService.findById(id);
                if (user != null) {
                    return user; // 返回找到的第一个用户
                }
            } catch (Exception e) {
                // 继续尝试下一个ID
                continue;
            }
        }
        
        // 如果前面的ID都没找到，抛出异常
        throw new RuntimeException("无法获取系统用户，请确保系统中至少有一个用户存在");
    }
    
    /**
     * 将Announcement实体转换为AnnouncementResponse DTO
     */
    public AnnouncementResponse convertToAnnouncementResponse(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setType(announcement.getType());
        response.setPublished(announcement.getPublished());
        response.setPriority(announcement.getPriority());
        response.setViewCount(announcement.getViewCount() != null ? announcement.getViewCount() : 0L);
        response.setCreatedAt(announcement.getCreatedAt());
        response.setUpdatedAt(announcement.getUpdatedAt());
        response.setPublishedAt(announcement.getPublishedAt());
        response.setArchived(announcement.getArchived() != null ? announcement.getArchived() : false);
        
        // 安全地获取创建者信息
        if (announcement.getAuthor() != null) {
            response.setCreatedByName(announcement.getAuthor().getRealName());
            response.setCreatedByUsername(announcement.getAuthor().getUsername());
        }
        
        return response;
    }
}
