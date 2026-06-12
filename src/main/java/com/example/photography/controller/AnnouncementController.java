package com.example.photography.controller;

import com.example.photography.dto.request.AnnouncementCreateRequest;
import com.example.photography.dto.response.AnnouncementResponse;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.model.entity.Announcement;
import com.example.photography.service.AnnouncementService;
import com.example.photography.service.impl.AnnouncementServiceImpl;
import com.example.photography.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/announcements")
@Tag(name = "公告管理", description = "公告的增删改查、发布、查看等操作")
public class AnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    @GetMapping("/public")
    @Operation(summary = "获取已发布的公告", description = "获取所有已发布的公告（公开接口）")
    public ApiResponse<List<Announcement>> getPublishedAnnouncements() {
        try {
            List<Announcement> announcements = announcementService.getPublishedAnnouncements();
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/public/paged")
    @Operation(summary = "分页获取已发布的公告", description = "分页获取已发布的公告（公开接口）")
    public ApiResponse<Page<AnnouncementResponse>> getPublishedAnnouncementsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 只获取已发布的公告，使用DTO避免JSON序列化问题
            Page<AnnouncementResponse> announcements = announcementService.getPublishedAnnouncementsResponse(pageable);
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/public/login-popup")
    @Operation(summary = "获取登录弹窗公告", description = "获取登录弹窗显示的公告（排除归档的公告）")
    public ApiResponse<Page<AnnouncementResponse>> getLoginPopupAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Sort sort = Sort.by(Sort.Direction.DESC, "priority", "createdAt");
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 获取登录弹窗公告（排除归档的公告）
            Page<AnnouncementResponse> announcements = announcementService.getLoginPopupAnnouncementsResponse(pageable);
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 创建模拟公告数据
     */
    private List<Announcement> createMockAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        
        // 创建第一条公告
        Announcement announcement1 = new Announcement();
        announcement1.setId(1L);
        announcement1.setTitle("欢迎使用融媒体管理系统");
        announcement1.setContent("欢迎使用融媒体管理系统！本系统提供设备管理、借用管理、用户管理等功能。如有问题请联系管理员。");
        announcement1.setPublished(true);
        announcement1.setPriority(1);
        announcement1.setViewCount(0L);
        announcement1.setDeleted(false);
        announcement1.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
        announcement1.setUpdatedAt(java.time.LocalDateTime.now().minusDays(1));
        
        // 创建第二条公告
        Announcement announcement2 = new Announcement();
        announcement2.setId(2L);
        announcement2.setTitle("系统使用须知");
        announcement2.setContent("请各位用户遵守系统使用规范：\n1. 借用设备前请仔细检查设备状态\n2. 按时归还设备\n3. 如有设备损坏请及时报告\n4. 保持良好的使用习惯");
        announcement2.setPublished(true);
        announcement2.setPriority(2);
        announcement2.setViewCount(0L);
        announcement2.setDeleted(false);
        announcement2.setCreatedAt(java.time.LocalDateTime.now().minusHours(12));
        announcement2.setUpdatedAt(java.time.LocalDateTime.now().minusHours(12));
        
        announcements.add(announcement1);
        announcements.add(announcement2);
        
        return announcements;
    }
    
    /**
     * 标记公告为已读（模拟实现）
     */
    @PutMapping("/{id}/view")
    @Operation(summary = "标记公告为已读", description = "用户查看公告后标记为已读")
    public ApiResponse<String> markAnnouncementAsRead(@PathVariable Long id) {
        System.out.println("=== 标记公告已读 ===");
        System.out.println("公告ID: " + id);
        
        try {
            // 模拟标记已读操作
            // 在实际实现中，这里会更新数据库中的阅读记录
            System.out.println("公告 " + id + " 已标记为已读");
            return ApiResponse.success("公告已标记为已读");
        } catch (Exception e) {
            System.err.println("标记公告已读失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("标记已读失败");
        }
    }
    
    @GetMapping("/public/latest")
    @Operation(summary = "获取最新公告", description = "获取最新的几条公告（首页用）")
    public ApiResponse<List<Announcement>> getLatestAnnouncements(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Announcement> announcements = announcementService.getLatestAnnouncements(limit);
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页获取所有公告", description = "管理员分页获取所有公告（仅管理员）")
    public ApiResponse<Page<AnnouncementResponse>> getAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean published) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 根据搜索条件获取公告
            Page<AnnouncementResponse> announcements = announcementService.searchAnnouncementsResponse(
                keyword, type, published, pageable);
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索公告", description = "根据关键字搜索公告")
    public ApiResponse<Page<Announcement>> searchAnnouncements(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Announcement> announcements = announcementService.searchAnnouncements(keyword, pageable);
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取我创建的公告", description = "获取当前用户创建的公告（仅管理员）")
    public ApiResponse<Page<Announcement>> getMyAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            Pageable pageable = PageRequest.of(page, size);
            Page<Announcement> announcements = announcementService.findByCreatedBy(userId, pageable);
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取公告", description = "根据公告ID获取公告详情并增加查看次数")
    public ApiResponse<AnnouncementResponse> getAnnouncementById(@PathVariable Long id) {
        try {
            // 先获取公告详情（预加载创建者信息），确保公告存在
            Announcement announcement = announcementService.findByIdWithAuthor(id);
            // 如果公告存在，再增加查看次数
            announcementService.incrementViewCount(id);
            // 转换为DTO
            AnnouncementResponse response = ((AnnouncementServiceImpl) announcementService).convertToAnnouncementResponse(announcement);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建公告", description = "创建新公告（仅管理员）")
    public ApiResponse<Void> createAnnouncement(@Valid @RequestBody AnnouncementCreateRequest request) {
        try {
            Long createdBy = SecurityUtils.getCurrentUserId();
            if (createdBy == null) {
                return ApiResponse.error("无法获取当前用户信息");
            }
            Announcement announcement = announcementService.createAnnouncement(createdBy, request);
            // 返回简单的成功消息而不是完整的实体对象，避免JSON序列化问题
            return ApiResponse.success("公告创建成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新公告", description = "更新公告信息（仅管理员）")
    public ApiResponse<Void> updateAnnouncement(@PathVariable Long id, 
                                                       @Valid @RequestBody AnnouncementCreateRequest request) {
        try {
            Announcement announcement = announcementService.updateAnnouncement(id, request);
            // 返回简单的成功消息而不是完整的实体对象，避免JSON序列化问题
            return ApiResponse.success("公告更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除公告", description = "删除指定公告（仅管理员）")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return ApiResponse.success("公告删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "发布公告", description = "发布指定公告（仅管理员）")
    public ApiResponse<Void> publishAnnouncement(@PathVariable Long id) {
        try {
            announcementService.publishAnnouncement(id);
            return ApiResponse.success("公告发布成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取消发布公告", description = "取消发布指定公告（仅管理员）")
    public ApiResponse<Void> unpublishAnnouncement(@PathVariable Long id) {
        try {
            announcementService.unpublishAnnouncement(id);
            return ApiResponse.success("公告已取消发布");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "归档公告", description = "归档指定公告，归档后不会在登录弹窗显示（仅管理员）")
    public ApiResponse<Void> archiveAnnouncement(@PathVariable Long id) {
        try {
            announcementService.archiveAnnouncement(id);
            return ApiResponse.success("公告已归档");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/unarchive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取消归档公告", description = "取消归档指定公告，取消归档后可在登录弹窗显示（仅管理员）")
    public ApiResponse<Void> unarchiveAnnouncement(@PathVariable Long id) {
        try {
            announcementService.unarchiveAnnouncement(id);
            return ApiResponse.success("公告已取消归档");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 修复查看次数显示问题（临时修复端点）
     */
    @PostMapping("/fix-view-counts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修复查看次数", description = "修复现有公告的查看次数显示问题（仅管理员）")
    public ApiResponse<Void> fixViewCounts() {
        try {
            ((AnnouncementServiceImpl) announcementService).fixNullViewCounts();
            return ApiResponse.success("查看次数修复完成");
        } catch (Exception e) {
            return ApiResponse.error("修复失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/count")
    @Operation(summary = "获取已发布公告数量", description = "获取当前已发布的公告数量")
    public ApiResponse<Long> getPublishedAnnouncementCount() {
        try {
            long count = announcementService.getPublishedAnnouncementCount();
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取公告统计信息", description = "获取公告统计数据（仅管理员）")
    public ApiResponse<AnnouncementService.AnnouncementStatistics> getAnnouncementStatistics() {
        try {
            AnnouncementService.AnnouncementStatistics statistics = announcementService.getAnnouncementStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
