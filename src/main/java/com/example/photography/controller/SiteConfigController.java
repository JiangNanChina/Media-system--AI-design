package com.example.photography.controller;

import com.example.photography.dto.request.MailTestRequest;
import com.example.photography.dto.request.SiteConfigRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.EmailNotificationLogResponse;
import com.example.photography.dto.response.SiteConfigResponse;
import com.example.photography.model.entity.EmailNotificationLog;
import com.example.photography.model.entity.SiteConfig;
import com.example.photography.repository.EmailNotificationLogRepository;
import com.example.photography.service.EmailNotificationService;
import com.example.photography.service.SiteConfigService;
import com.example.photography.util.FileUploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站点配置管理控制器
 */
@RestController
@RequestMapping("/site-config")
@Slf4j
@Tag(name = "站点配置管理", description = "管理登录界面背景、网站LOGO等全局配置")
public class SiteConfigController {
    
    @Autowired
    private SiteConfigService siteConfigService;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private EmailNotificationLogRepository emailNotificationLogRepository;
    
    /**
     * 获取公开配置（无需认证）
     */
    @GetMapping("/public")
    @Operation(summary = "获取公开配置", description = "获取前端展示需要的公开配置信息")
    public ApiResponse<Map<String, String>> getPublicConfigs() {
        try {
            System.out.println("=== 收到公开配置请求 ===");
            Map<String, String> configs = siteConfigService.getPublicConfigs();
            System.out.println("公开配置响应: " + configs);
            return ApiResponse.success(configs);
        } catch (Exception e) {
            System.err.println("获取公开配置失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("获取配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有配置
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有配置", description = "管理员获取所有站点配置列表")
    public ApiResponse<List<SiteConfigResponse>> getAllConfigs() {
        try {
            List<SiteConfig> configs = siteConfigService.getAllConfigs();
            List<SiteConfigResponse> responses = configs.stream()
                    .map(SiteConfigResponse::fromEntity)
                    .collect(Collectors.toList());
            return ApiResponse.success(responses);
        } catch (Exception e) {
            return ApiResponse.error("获取配置列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据配置键获取配置
     */
    @GetMapping("/admin/{configKey}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取单个配置", description = "根据配置键获取配置详情")
    public ApiResponse<SiteConfigResponse> getConfigByKey(@PathVariable String configKey) {
        try {
            SiteConfig config = siteConfigService.getConfigByKey(configKey);
            if (config == null) {
                return ApiResponse.error("配置不存在");
            }
            return ApiResponse.success(SiteConfigResponse.fromEntity(config));
        } catch (Exception e) {
            return ApiResponse.error("获取配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建或更新配置
     */
    @PostMapping("/admin/save")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "保存配置", description = "创建或更新站点配置")
    public ApiResponse<SiteConfigResponse> saveConfig(@Valid @RequestBody SiteConfigRequest request) {
        try {
            SiteConfig config = siteConfigService.saveOrUpdateConfig(request);
            return ApiResponse.success("配置保存成功", SiteConfigResponse.fromEntity(config));
        } catch (Exception e) {
            return ApiResponse.error("保存配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新配置", description = "根据ID更新配置")
    public ApiResponse<SiteConfigResponse> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody SiteConfigRequest request) {
        try {
            SiteConfig config = siteConfigService.updateConfig(id, request);
            return ApiResponse.success("配置更新成功", SiteConfigResponse.fromEntity(config));
        } catch (Exception e) {
            return ApiResponse.error("更新配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除配置", description = "删除指定配置")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        try {
            siteConfigService.deleteConfig(id);
            return ApiResponse.success("配置删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传网站LOGO
     */
    @PostMapping("/admin/upload-logo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上传网站LOGO", description = "上传并设置网站LOGO")
    public ApiResponse<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("请选择要上传的文件");
            }
            
            String logoPath = siteConfigService.uploadLogo(file);
            return ApiResponse.success("LOGO上传成功", logoPath);
        } catch (Exception e) {
            return ApiResponse.error("上传LOGO失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传登录背景
     */
    @PostMapping("/admin/upload-background")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上传登录背景", description = "上传并设置登录页面背景图")
    public ApiResponse<String> uploadLoginBackground(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("请选择要上传的文件");
            }
            
            String backgroundPath = siteConfigService.uploadLoginBackground(file);
            return ApiResponse.success("背景图上传成功", backgroundPath);
        } catch (Exception e) {
            return ApiResponse.error("上传背景图失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新配置
     */
    @PostMapping("/admin/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量更新配置", description = "批量更新多个配置项")
    public ApiResponse<List<SiteConfigResponse>> batchUpdateConfigs(
            @Valid @RequestBody List<SiteConfigRequest> requests) {
        try {
            List<SiteConfig> configs = requests.stream()
                    .map(siteConfigService::saveOrUpdateConfig)
                    .collect(Collectors.toList());
            
            List<SiteConfigResponse> responses = configs.stream()
                    .map(SiteConfigResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success("批量更新成功", responses);
        } catch (Exception e) {
            return ApiResponse.error("批量更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/admin/mail/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "测试QQ邮箱配置", description = "发送测试邮件验证QQ邮箱SMTP配置是否可用")
    public ApiResponse<Void> testMail(@Valid @RequestBody MailTestRequest request) {
        try {
            emailNotificationService.sendTestMail(request.getEmail());
            return ApiResponse.success("测试邮件已发送");
        } catch (Exception e) {
            return ApiResponse.error("测试邮件发送失败: " + e.getMessage());
        }
    }

    @GetMapping("/admin/mail/logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取邮件发送日志", description = "分页查看QQ邮箱验证码与提醒邮件发送记录")
    public ApiResponse<Page<EmailNotificationLogResponse>> getMailLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String notificationType,
            @RequestParam(required = false) Boolean success) {
        try {
            int safePage = Math.max(page, 0);
            int safeSize = Math.min(Math.max(size, 1), 100);
            String typeFilter = notificationType == null || notificationType.trim().isEmpty()
                    ? null
                    : notificationType.trim();

            Pageable pageable = PageRequest.of(safePage, safeSize);
            Page<EmailNotificationLog> logs = emailNotificationLogRepository.searchLogs(typeFilter, success, pageable);
            return ApiResponse.success(logs.map(EmailNotificationLogResponse::fromEntity));
        } catch (Exception e) {
            return ApiResponse.error("获取邮件日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化默认配置
     */
    @PostMapping("/admin/init-defaults")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "初始化默认配置", description = "初始化系统默认配置")
    public ApiResponse<Void> initDefaultConfigs() {
        try {
            siteConfigService.initDefaultConfigs();
            return ApiResponse.success("默认配置初始化成功");
        } catch (Exception e) {
            return ApiResponse.error("初始化失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置为默认配置
     */
    @PostMapping("/admin/reset-defaults")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重置为默认配置", description = "重置所有配置为系统默认值")
    public ApiResponse<Void> resetToDefaults() {
        try {
            siteConfigService.resetToDefaults();
            return ApiResponse.success("配置重置成功");
        } catch (Exception e) {
            return ApiResponse.error("重置失败: " + e.getMessage());
        }
    }
    
    /**
     * 修复路径重复问题
     */
    @PostMapping("/admin/fix-paths")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修复路径重复问题", description = "修复LOGO和背景路径中的重复uploads前缀")
    public ApiResponse<String> fixDuplicatePaths() {
        try {
            siteConfigService.fixDuplicatePaths();
            return ApiResponse.success("路径修复成功");
        } catch (Exception e) {
            log.error("修复路径失败", e);
            return ApiResponse.error("修复路径失败: " + e.getMessage());
        }
    }
    
    /**
     * 🔧 调试API：检查站点图片文件状态
     */
    @GetMapping("/admin/debug-images")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "调试图片文件状态", description = "检查站点LOGO和背景图片的文件状态和路径信息")
    public ApiResponse<Map<String, Object>> debugImages() {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // 获取当前配置
            Map<String, String> configs = siteConfigService.getPublicConfigs();
            debugInfo.put("configs", configs);
            
            // 检查LOGO文件
            String logoPath = configs.get("site.logo");
            Map<String, Object> logoInfo = new HashMap<>();
            logoInfo.put("path", logoPath);
            logoInfo.put("exists", logoPath != null ? fileUploadUtil.fileExists(logoPath) : false);
            logoInfo.put("size", logoPath != null ? fileUploadUtil.getFileSize(logoPath) : 0);
            debugInfo.put("logo", logoInfo);
            
            // 检查背景文件
            String backgroundPath = configs.get("login.background");
            Map<String, Object> backgroundInfo = new HashMap<>();
            backgroundInfo.put("path", backgroundPath);
            backgroundInfo.put("exists", backgroundPath != null ? fileUploadUtil.fileExists(backgroundPath) : false);
            backgroundInfo.put("size", backgroundPath != null ? fileUploadUtil.getFileSize(backgroundPath) : 0);
            debugInfo.put("background", backgroundInfo);
            
            // 系统信息
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("os", System.getProperty("os.name"));
            systemInfo.put("user.dir", System.getProperty("user.dir"));
            debugInfo.put("system", systemInfo);
            
            log.info("站点图片调试信息: {}", debugInfo);
            
            return ApiResponse.success(debugInfo);
        } catch (Exception e) {
            log.error("获取调试信息失败", e);
            return ApiResponse.error("获取调试信息失败: " + e.getMessage());
        }
    }
}
