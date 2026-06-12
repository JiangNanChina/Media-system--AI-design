package com.example.photography.controller;

import com.example.photography.dto.request.CheckinConfigurationRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.CheckinConfigurationResponse;
import com.example.photography.service.CheckinConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打卡配置管理控制器
 */
@RestController
@RequestMapping("/checkin/configurations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "打卡配置管理", description = "统一管理打卡地点和时间配置")
public class CheckinConfigurationController {
    
    private final CheckinConfigurationService configurationService;
    
    /**
     * 创建打卡配置
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建打卡配置", description = "创建新的打卡配置，关联地点和时段")
    public ApiResponse<CheckinConfigurationResponse> createConfiguration(
            @Valid @RequestBody CheckinConfigurationRequest request) {
        CheckinConfigurationResponse response = configurationService.createConfiguration(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 更新打卡配置
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新打卡配置", description = "更新指定的打卡配置")
    public ApiResponse<CheckinConfigurationResponse> updateConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody CheckinConfigurationRequest request) {
        CheckinConfigurationResponse response = configurationService.updateConfiguration(id, request);
        return ApiResponse.success(response);
    }
    
    /**
     * 删除打卡配置
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除打卡配置", description = "物理删除指定的打卡配置（需检查关联记录）")
    public ApiResponse<Void> deleteConfiguration(@PathVariable Long id) {
        try {
            configurationService.deleteConfiguration(id);
            return ApiResponse.success("配置删除成功");
        } catch (Exception e) {
            log.error("删除配置失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 强制删除打卡配置
     */
    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "强制删除打卡配置", description = "强制删除配置及其所有关联的打卡记录")
    public ApiResponse<Void> forceDeleteConfiguration(@PathVariable Long id) {
        try {
            configurationService.forceDeleteConfiguration(id);
            return ApiResponse.success("配置及关联记录删除成功");
        } catch (Exception e) {
            log.error("强制删除配置失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 切换配置状态
     */
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "切换配置状态", description = "启用或禁用打卡配置")
    public ApiResponse<CheckinConfigurationResponse> toggleConfigurationStatus(@PathVariable Long id) {
        CheckinConfigurationResponse response = configurationService.toggleConfigurationStatus(id);
        return ApiResponse.success(response);
    }
    
    /**
     * 获取配置详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取配置详情", description = "根据ID获取打卡配置详情")
    public ApiResponse<CheckinConfigurationResponse> getConfiguration(@PathVariable Long id) {
        CheckinConfigurationResponse response = configurationService.getConfigurationById(id);
        return ApiResponse.success(response);
    }
    
    /**
     * 获取所有配置（分页）
     */
    @GetMapping
    @Operation(summary = "获取配置列表", description = "分页获取所有打卡配置")
    public ApiResponse<Page<CheckinConfigurationResponse>> getAllConfigurations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            log.info("获取配置列表请求: page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CheckinConfigurationResponse> configurations = configurationService.getAllConfigurations(pageable);
            
            log.info("获取配置列表成功，共 {} 条记录", configurations.getTotalElements());
            return ApiResponse.success(configurations);
        } catch (Exception e) {
            log.error("获取配置列表失败", e);
            return ApiResponse.error("获取配置列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索配置
     */
    @GetMapping("/search")
    @Operation(summary = "搜索配置", description = "根据关键词搜索打卡配置")
    public ApiResponse<Page<CheckinConfigurationResponse>> searchConfigurations(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<CheckinConfigurationResponse> configurations = configurationService.searchConfigurations(keyword, pageable);
        return ApiResponse.success(configurations);
    }
    
    /**
     * 获取启用的配置
     */
    @GetMapping("/active")
    @Operation(summary = "获取启用的配置", description = "获取所有启用状态的打卡配置")
    public ApiResponse<List<CheckinConfigurationResponse>> getActiveConfigurations() {
        List<CheckinConfigurationResponse> configurations = configurationService.getActiveConfigurations();
        return ApiResponse.success(configurations);
    }
    
    /**
     * 根据地点名称获取配置
     */
    @GetMapping("/by-location-name/{locationName}")
    @Operation(summary = "根据地点名称获取配置", description = "获取指定地点名称的所有配置")
    public ApiResponse<List<CheckinConfigurationResponse>> getConfigurationsByLocationName(@PathVariable String locationName) {
        List<CheckinConfigurationResponse> configurations = configurationService.getConfigurationsByLocationName(locationName);
        return ApiResponse.success(configurations);
    }
    
    /**
     * 根据时段名称获取配置
     */
    @GetMapping("/by-session-name/{sessionName}")
    @Operation(summary = "根据时段名称获取配置", description = "获取指定时段名称的所有配置")
    public ApiResponse<List<CheckinConfigurationResponse>> getConfigurationsBySessionName(@PathVariable String sessionName) {
        List<CheckinConfigurationResponse> configurations = configurationService.getConfigurationsBySessionName(sessionName);
        return ApiResponse.success(configurations);
    }
    
    /**
     * 更新配置排序
     */
    @PutMapping("/order")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新配置排序", description = "批量更新配置的排序顺序")
    public ApiResponse<Void> updateConfigurationOrder(@RequestBody List<Long> configurationIds) {
        configurationService.updateConfigurationOrder(configurationIds);
        return ApiResponse.success(null);
    }
    
    /**
     * 获取配置统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取配置统计", description = "获取打卡配置的统计信息")
    public ApiResponse<Object> getConfigurationStatistics() {
        Object statistics = configurationService.getConfigurationStatistics();
        return ApiResponse.success(statistics);
    }
}
