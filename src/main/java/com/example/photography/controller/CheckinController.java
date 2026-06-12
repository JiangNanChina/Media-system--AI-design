package com.example.photography.controller;

import com.example.photography.dto.request.CheckinRequest;
import com.example.photography.dto.request.CheckoutRequest;
import com.example.photography.dto.request.LocationValidationRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.CheckinResponse;
import com.example.photography.dto.response.LocationValidationResponse;
import com.example.photography.model.entity.CheckinRecord;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.dto.response.CheckinConfigurationResponse;
import com.example.photography.dto.response.CheckinRecordDetailResponse;
import com.example.photography.service.CheckinService;
import com.example.photography.service.CheckinConfigurationService;
import com.example.photography.repository.CheckinRecordRepository;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.example.photography.util.CoordinateConverter;

/**
 * 打卡控制器
 */
@RestController
@RequestMapping("/checkin")
@Slf4j
@Tag(name = "晚自习打卡", description = "晚自习打卡功能、记录查询等操作")
public class CheckinController {
    
    @Autowired
    private CheckinService checkinService;
    
    @Autowired
    private CheckinConfigurationService checkinConfigurationService;
    
    /**
     * 从认证信息中获取用户ID的统一方法
     */
    private Long getUserIdFromAuthentication() {
        log.info("=== 开始获取用户ID ===");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication: {}", authentication != null ? authentication.getClass().getName() : "null");
        
        if (authentication == null) {
            log.error("Authentication为null");
            throw new RuntimeException("用户未认证");
        }
        
        log.info("Authentication详情: name={}, principal={}, details={}, authorities={}", 
            authentication.getName(), 
            authentication.getPrincipal(), 
            authentication.getDetails(), 
            authentication.getAuthorities());
        
        Object details = authentication.getDetails();
        log.info("Details类型: {}, 值: {}", details != null ? details.getClass().getName() : "null", details);
        
        if (details instanceof Long) {
            Long userId = (Long) details;
            log.info("成功获取用户ID (Long类型): {}", userId);
            return userId;
        } else if (details != null) {
            try {
                Long userId = Long.parseLong(details.toString());
                log.info("成功获取用户ID (转换类型): {}", userId);
                return userId;
            } catch (NumberFormatException e) {
                log.error("用户ID格式错误: details={}, error={}", details, e.getMessage());
                throw new RuntimeException("用户ID格式错误: " + details);
            }
        }
        
        log.error("用户ID不存在，details为null");
        throw new RuntimeException("用户ID不存在");
    }
    
    @Autowired
    private CheckinRecordRepository checkinRecordRepository;
    
    // ========== 打卡功能 ==========
    
    @PostMapping("/signin")
    @Operation(summary = "签到", description = "用户进行签到")
    public ApiResponse<CheckinResponse> checkin(@Valid @RequestBody CheckinRequest request, 
                                               HttpServletRequest httpRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            
            CheckinResponse response = checkinService.checkin(request, userId);
            return ApiResponse.success("签到成功", response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/signout")
    @Operation(summary = "签退", description = "用户进行签退")
    public ApiResponse<CheckinResponse> checkout(@Valid @RequestBody CheckoutRequest request, 
                                                HttpServletRequest httpRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            
            CheckinResponse response = checkinService.checkout(request, userId);
            return ApiResponse.success("签退成功", response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/can-checkin")
    @Operation(summary = "检查是否可以签到", description = "检查当前用户是否可以进行签到")
    public ApiResponse<Boolean> canCheckin(@RequestParam Long configurationId, 
                                         @RequestParam(required = false) Double latitude,
                                         @RequestParam(required = false) Double longitude,
                                         @RequestParam(required = false, defaultValue = "GPS") String checkinMethod) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            
            boolean canCheckin = checkinService.validateConfiguration(configurationId, latitude, longitude, userId, checkinMethod);
            return ApiResponse.success("签到检查完成", canCheckin);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping({"/today-status", "/today"})
    @Operation(summary = "获取今日签到状态", description = "获取当前用户今日的签到状态")
    public ApiResponse<Map<String, Object>> getTodayStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            
            Map<String, Object> status = checkinService.getTodayCheckinStatus(userId);
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/records/today")
    @Operation(summary = "获取今日打卡记录详情", description = "获取当前用户今日的打卡记录详情")
    public ApiResponse<CheckinRecord> getTodayRecord() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            
            // 查找今日打卡记录（使用JOIN FETCH预加载关联实体，避免懒加载问题）
            LocalDate today = LocalDate.now();
            List<CheckinRecord> records = checkinRecordRepository.findByUserIdAndCheckinDateWithFetch(userId, today);
            
            if (!records.isEmpty()) {
                // 优先返回真实签到记录，请假占位记录不应遮挡未请假的打卡配置
                CheckinRecord record = records.stream()
                        .filter(item -> item.getStatus() != CheckinRecord.CheckinStatus.LEAVE)
                        .findFirst()
                        .orElse(records.get(0));
                return ApiResponse.success(record);
            } else {
                return ApiResponse.success(null);
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ========== 记录查询 ==========
    
    @GetMapping("/user-records")
    @Operation(summary = "获取我的打卡记录", description = "获取当前用户的打卡记录")
    public ApiResponse<Page<CheckinRecord>> getUserRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("获取用户打卡记录请求: page={}, size={}", page, size);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                log.error("Authentication为null");
                return ApiResponse.error("用户未认证");
            }
            
            log.info("认证信息类型: {}", authentication.getClass().getName());
            log.info("认证信息: name={}, principal={}, details={}, authorities={}", 
                authentication.getName(), 
                authentication.getPrincipal(), 
                authentication.getDetails(), 
                authentication.getAuthorities());
            
            Object details = authentication.getDetails();
            if (details == null) {
                log.error("Authentication.details为null");
                return ApiResponse.error("用户ID不存在");
            }
            
            Long userId;
            try {
                userId = (Long) details;
                log.info("成功获取用户ID: {}", userId);
            } catch (ClassCastException e) {
                log.error("无法将details转换为Long: details类型={}, 值={}", details.getClass().getName(), details);
                return ApiResponse.error("用户ID格式错误");
            }
            
            log.info("查询用户{}的打卡记录: 页码={}, 每页大小={}", userId, page, size);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkinTime"));
            Page<CheckinRecord> records = checkinService.getUserCheckinRecords(userId, pageable);
            
            log.info("查询结果: 总数={}, 当前页记录数={}", records.getTotalElements(), records.getContent().size());
            
            return ApiResponse.success(records);
        } catch (Exception e) {
            log.error("获取用户打卡记录失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/all-records")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有打卡记录", description = "管理员获取所有打卡记录（仅管理员）")
    public ApiResponse<Page<CheckinRecord>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CheckinRecord.CheckinStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkinTime"));
            Page<CheckinRecord> records = checkinService.getAllCheckinRecords(keyword, status, startDate, endDate, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取个人打卡统计", description = "获取当前用户的打卡统计数据")
    public ApiResponse<Map<String, Object>> getMyStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            
            Map<String, Object> statistics = checkinService.getCheckinStatistics(userId, startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/user-statistics")
    @Operation(summary = "获取用户打卡统计", description = "获取当前用户的打卡统计数据（前端兼容接口）")
    public ApiResponse<Map<String, Object>> getUserStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            
            Map<String, Object> statistics = checkinService.getCheckinStatistics(userId, startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/admin-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取管理员打卡统计", description = "管理员获取全体打卡统计数据（仅管理员）")
    public ApiResponse<Map<String, Object>> getAdminStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Map<String, Object> statistics = checkinService.getGlobalCheckinStatistics(startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ========== 前端需要的额外端点 ==========
    
    @GetMapping("/available-configurations")
    @Operation(summary = "获取可用的打卡配置", description = "获取当前用户可用的打卡配置列表（仅返回用户有权限的配置）")
    public ApiResponse<java.util.List<CheckinConfigurationResponse>> getAvailableConfigurations() {
        try {
            Long userId = getUserIdFromAuthentication();
            
            java.util.List<CheckinConfiguration> configurations = checkinService.getUserAuthorizedConfigurations(userId);
            
            // 转换为响应DTO，避免序列化问题
            java.util.List<CheckinConfigurationResponse> responseList = configurations.stream()
                .map(CheckinConfigurationResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
                
            return ApiResponse.success("获取成功", responseList);
        } catch (Exception e) {
            log.error("获取可用打卡配置失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/recent-records")
    @Operation(summary = "获取最近的打卡记录", description = "获取当前用户最近的打卡记录")
    public ApiResponse<java.util.List<CheckinRecordDetailResponse>> getRecentRecords(
            @RequestParam(defaultValue = "5") int size) {
        try {
            log.info("获取最近打卡记录请求: size={}", size);
            
            Long userId = getUserIdFromAuthentication();
            log.info("查询用户{}的最近{}条打卡记录", userId, size);
            
            List<CheckinRecord> records = checkinService.getRecentCheckinRecords(userId, size);
            
            // 转换为响应DTO，避免序列化问题
            java.util.List<CheckinRecordDetailResponse> responseList = records.stream()
                .map(CheckinRecordDetailResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
            
            log.info("查询结果: 记录数={}", responseList.size());
            
            return ApiResponse.success("获取成功", responseList);
        } catch (Exception e) {
            log.error("获取最近打卡记录失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/configurations-by-location")
    @Operation(summary = "根据地点名称获取配置", description = "根据地点名称获取相关的打卡配置")
    public ApiResponse<java.util.List<CheckinConfigurationResponse>> getConfigurationsByLocation(
            @RequestParam String locationName) {
        try {
            java.util.List<CheckinConfigurationResponse> configurations = 
                checkinConfigurationService.getConfigurationsByLocationName(locationName);
            return ApiResponse.success("获取成功", configurations);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/nearby-configurations")
    @Operation(summary = "获取附近的打卡配置", description = "根据位置获取附近的打卡配置")
    public ApiResponse<java.util.List<CheckinConfigurationResponse>> getNearbyConfigurations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "50000") Integer radius) {
        try {
            log.info("获取附近配置请求: latitude={}, longitude={}, radius={}", latitude, longitude, radius);

            // 只返回当前用户有权限的配置
            Long userId = getUserIdFromAuthentication();
            java.util.List<CheckinConfiguration> authorized = checkinService.getUserAuthorizedConfigurations(userId);

            java.util.List<CheckinConfigurationResponse> nearbyConfigurations =
                authorized.stream()
                    .filter(cfg -> cfg.getLongitude() != null && cfg.getLatitude() != null)
                    .filter(cfg -> calculateDistance(latitude, longitude, cfg.getLatitude(), cfg.getLongitude()) <= radius)
                    .map(CheckinConfigurationResponse::fromEntity)
                    .collect(java.util.stream.Collectors.toList());

            log.info("附近且有权限的配置数量: {}", nearbyConfigurations.size());
            return ApiResponse.success("获取成功", nearbyConfigurations);
        } catch (Exception e) {
            log.error("获取附近配置失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 计算两点之间的距离（米）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径，单位：公里
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // 转换为米
        
        return distance;
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    @PostMapping("/validate-location")
    @Operation(summary = "验证位置", description = "验证用户当前位置是否在打卡范围内")
    public ApiResponse<LocationValidationResponse> validateLocation(
            @Valid @RequestBody LocationValidationRequest request) {
        try {
            log.info("位置验证请求: locationId={}, latitude={}, longitude={}", 
                request.getLocationId(), request.getLatitude(), request.getLongitude());
            
            // 当前用户
            Long userId = getUserIdFromAuthentication();
            
            // 获取目标位置配置（用于坐标与提示）
            CheckinConfigurationResponse config = checkinConfigurationService.getConfigurationById(request.getLocationId());
            if (config == null) {
                return ApiResponse.success("验证完成", 
                    new LocationValidationResponse(false, "指定的位置配置不存在"));
            }
            
            // 先做权限校验：用户必须在配置的考勤人员中
            boolean authorized;
            try {
                java.util.List<CheckinConfiguration> authorizedConfigs = checkinService.getUserAuthorizedConfigurations(userId);
                authorized = authorizedConfigs.stream().anyMatch(c -> c.getId().equals(request.getLocationId()));
            } catch (Exception ex) {
                log.warn("检查用户权限失败: userId={}, configId={}", userId, request.getLocationId(), ex);
                authorized = false;
            }
            if (!authorized) {
                return ApiResponse.success("验证完成", 
                    new LocationValidationResponse(false, "您未被配置为该打卡时段的考勤人员"));
            }
            
            // 调用服务层综合校验（时间窗口/位置半径等）
            boolean canCheckin = checkinService.validateConfiguration(request.getLocationId(), request.getLatitude(), request.getLongitude(), userId, "GPS");
            
            if (config.getLongitude() == null || config.getLatitude() == null) {
                return ApiResponse.success("验证完成", 
                    new LocationValidationResponse(false, "位置配置缺少坐标信息"));
            }
            
            // 🔍 调试日志：打印原始坐标
            log.info("🔍 位置验证调试 - 用户原始坐标(WGS84): lat={}, lng={}", request.getLatitude(), request.getLongitude());
            log.info("🔍 位置验证调试 - 目标位置坐标: lat={}, lng={}", config.getLatitude(), config.getLongitude());
            log.info("🔍 位置验证调试 - 目标地址: {}", config.getLocationAddress());
            
            // 自适应距离计算（与Service层逻辑保持一致）
            // 1) rawDistance: 直接用传入坐标与配置坐标计算
            // 2) convertedDistance: 将传入的WGS84转换为GCJ-02后再计算
            double rawDistance = calculateDistance(request.getLatitude(), request.getLongitude(), 
                config.getLatitude(), config.getLongitude());
            
            double convertedDistance;
            try {
                double[] userGcj02 = CoordinateConverter.wgs84ToGcj02(request.getLatitude(), request.getLongitude());
                convertedDistance = calculateDistance(userGcj02[0], userGcj02[1], 
                    config.getLatitude(), config.getLongitude());
                log.info("🔍 位置验证调试 - WGS84({}, {}) -> GCJ-02({}, {})", 
                    request.getLatitude(), request.getLongitude(), userGcj02[0], userGcj02[1]);
            } catch (Exception convEx) {
                log.warn("坐标转换失败，使用原始坐标: {}", convEx.getMessage());
                convertedDistance = rawDistance;
            }
            
            // 智能选择距离（如果raw距离明显更小，说明传入坐标可能已经是GCJ-02）
            double distance;
            if (rawDistance + 30 < convertedDistance && rawDistance <= 120) {
                distance = rawDistance;
                log.info("🔍 位置验证调试 - 采用原始距离(疑似已是GCJ-02): raw={}m, converted={}m", rawDistance, convertedDistance);
            } else {
                distance = convertedDistance;
                log.info("🔍 位置验证调试 - 采用转换距离(WGS84→GCJ-02): converted={}m, raw={}m", convertedDistance, rawDistance);
            }
            
            log.info("🔍 位置验证调试 - 最终距离: {}米", distance);
            int allowedRadius = 100; // 默认100米
            
            // 详细判断失败原因
            String message;
            if (canCheckin) {
                message = String.format("位置验证通过，距离目标位置 %.0f 米", distance);
            } else {
                boolean locationValid = distance <= allowedRadius;
                
                if (!locationValid) {
                    message = String.format("位置超出范围：当前距离 %.0f 米，允许范围 %d 米", distance, allowedRadius);
                } else {
                    // 位置正确但验证失败，大概率是时间问题
                    java.time.LocalTime now = java.time.LocalTime.now();
                    java.time.LocalTime startTime = config.getStartTime();
                    java.time.LocalTime endTime = config.getEndTime();
                    
                    // 获取提前和延迟时间
                    int earlyMinutes = config.getEarlyCheckinMinutes() != null ? config.getEarlyCheckinMinutes() : 0;
                    int lateMinutes = config.getLateCheckinMinutes() != null ? config.getLateCheckinMinutes() : 0;
                    java.time.LocalTime earliestTime = startTime.minusMinutes(earlyMinutes);
                    java.time.LocalTime latestTime = endTime.plusMinutes(lateMinutes);
                    
                    message = String.format("当前不在打卡时间段内。当前时间: %s，允许时间: %s - %s（位置正常，距离 %.0f 米；如仍异常，请检查浏览器定位权限与网络）", 
                        now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        earliestTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        latestTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        distance);
                }
            }
            
            log.info("位置验证结果: userId={}, configId={}, canCheckin={}, distance={}", userId, request.getLocationId(), canCheckin, distance);
            
            return ApiResponse.success("验证完成", 
                new LocationValidationResponse(canCheckin, message, distance, allowedRadius));
                
        } catch (Exception e) {
            log.error("位置验证失败:", e);
            return ApiResponse.error("位置验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除打卡记录
     */
    @DeleteMapping("/records/{id}")
    @Operation(summary = "删除打卡记录", description = "删除指定的打卡记录")
    public ApiResponse<Void> deleteCheckinRecord(@PathVariable Long id) {
        try {
            log.info("删除打卡记录请求: recordId={}", id);
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                log.error("Authentication为null");
                return ApiResponse.error("用户未认证");
            }
            
            Object details = authentication.getDetails();
            if (details == null) {
                log.error("Authentication.details为null");
                return ApiResponse.error("用户ID不存在");
            }
            
            Long userId;
            try {
                userId = (Long) details;
                log.info("当前用户ID: {}", userId);
            } catch (ClassCastException e) {
                log.error("无法获取用户ID，details类型: {}", details.getClass().getName(), e);
                return ApiResponse.error("用户ID格式错误");
            }
            
            // 检查用户是否为管理员
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            // 检查记录是否存在且有权限删除
            boolean deleted = checkinService.deleteCheckinRecord(id, userId, isAdmin);
            
            if (deleted) {
                log.info("打卡记录删除成功: recordId={}, userId={}", id, userId);
                return ApiResponse.success("删除成功");
            } else {
                log.warn("打卡记录删除失败: recordId={}, userId={}", id, userId);
                return ApiResponse.error("记录不存在或无权限删除");
            }
            
        } catch (Exception e) {
            log.error("删除打卡记录失败: recordId={}", id, e);
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取待审核的签到记录列表（管理员）
     */
    @GetMapping("/audit/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取待审核签到记录", description = "管理员查看所有待审核的签到记录")
    public ApiResponse<Page<CheckinRecordDetailResponse>> getPendingAuditRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("管理员查询待审核签到记录: page={}, size={}", page, size);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkinTime"));
            Page<CheckinRecord> records = checkinService.getPendingAuditRecords(pageable);
            
            // 转换为DetailResponse
            Page<CheckinRecordDetailResponse> response = records.map(CheckinRecordDetailResponse::fromEntity);
            
            log.info("待审核记录数量: {}", records.getTotalElements());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("查询待审核记录失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 审核签到记录 - 通过
     */
    @PostMapping("/audit/{recordId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审核通过签到", description = "管理员审核通过签到记录，标记为已签到")
    public ApiResponse<CheckinRecordDetailResponse> approveCheckin(
            @PathVariable Long recordId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            Long adminId = getUserIdFromAuthentication();
            String auditNotes = requestBody != null ? requestBody.get("auditNotes") : null;
            
            log.info("管理员审核通过签到: recordId={}, adminId={}, notes={}", recordId, adminId, auditNotes);
            
            CheckinRecord record = checkinService.approveCheckin(recordId, adminId, auditNotes);
            CheckinRecordDetailResponse response = CheckinRecordDetailResponse.fromEntity(record);
            
            return ApiResponse.success("审核通过，已标记为正常签到", response);
            
        } catch (Exception e) {
            log.error("审核通过失败: recordId={}", recordId, e);
            return ApiResponse.error("审核失败: " + e.getMessage());
        }
    }
    
    /**
     * 审核签到记录 - 拒绝（标记为缺勤）
     */
    @PostMapping("/audit/{recordId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审核拒绝签到", description = "管理员审核拒绝签到记录，标记为缺勤")
    public ApiResponse<CheckinRecordDetailResponse> rejectCheckin(
            @PathVariable Long recordId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            Long adminId = getUserIdFromAuthentication();
            String auditNotes = requestBody != null ? requestBody.get("auditNotes") : null;
            
            log.info("管理员审核拒绝签到: recordId={}, adminId={}, notes={}", recordId, adminId, auditNotes);
            
            CheckinRecord record = checkinService.rejectCheckin(recordId, adminId, auditNotes);
            CheckinRecordDetailResponse response = CheckinRecordDetailResponse.fromEntity(record);
            
            return ApiResponse.success("审核拒绝，已标记为缺勤", response);
            
        } catch (Exception e) {
            log.error("审核拒绝失败: recordId={}", recordId, e);
            return ApiResponse.error("审核失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取待审核记录数量统计
     */
    @GetMapping("/audit/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取待审核记录数量", description = "获取待审核签到记录的数量")
    public ApiResponse<Map<String, Object>> getPendingAuditCount() {
        try {
            long count = checkinService.getPendingAuditCount();
            
            Map<String, Object> result = Map.of(
                "pendingCount", count,
                "hasPending", count > 0
            );
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("获取待审核数量失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取已审核通过的签到记录列表（管理员）
     */
    @GetMapping("/audit/approved")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取已审核通过的签到记录", description = "管理员查看所有已审核通过的签到记录")
    public ApiResponse<Page<CheckinRecordDetailResponse>> getApprovedAuditRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("管理员查询已审核通过签到记录: page={}, size={}", page, size);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "auditTime"));
            Page<CheckinRecord> records = checkinService.getAuditRecordsByStatus(
                CheckinRecord.AuditStatus.APPROVED, 
                pageable
            );
            
            Page<CheckinRecordDetailResponse> response = records.map(CheckinRecordDetailResponse::fromEntity);
            
            log.info("已审核通过记录数量: {}", records.getTotalElements());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("查询已审核通过记录失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取已审核拒绝的签到记录列表（管理员）
     */
    @GetMapping("/audit/rejected")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取已审核拒绝的签到记录", description = "管理员查看所有已审核拒绝的签到记录")
    public ApiResponse<Page<CheckinRecordDetailResponse>> getRejectedAuditRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("管理员查询已审核拒绝签到记录: page={}, size={}", page, size);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "auditTime"));
            Page<CheckinRecord> records = checkinService.getAuditRecordsByStatus(
                CheckinRecord.AuditStatus.REJECTED, 
                pageable
            );
            
            Page<CheckinRecordDetailResponse> response = records.map(CheckinRecordDetailResponse::fromEntity);
            
            log.info("已审核拒绝记录数量: {}", records.getTotalElements());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("查询已审核拒绝记录失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 物理删除签到记录（管理员专用）
     */
    @DeleteMapping("/records/{recordId}/physical")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "物理删除签到记录", description = "管理员永久删除签到记录，释放数据库空间")
    public ApiResponse<Void> deleteCheckinRecordPhysically(@PathVariable Long recordId) {
        try {
            // 从Security上下文获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "未知用户";
            
            log.warn("⚠️ 管理员 {} 请求物理删除签到记录: recordId={}", username, recordId);
            
            checkinService.deleteCheckinRecordPhysically(recordId);
            
            log.info("✅ 签到记录已物理删除: recordId={}", recordId);
            return ApiResponse.success("签到记录已永久删除");
            
        } catch (Exception e) {
            log.error("物理删除签到记录失败: recordId={}", recordId, e);
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}
