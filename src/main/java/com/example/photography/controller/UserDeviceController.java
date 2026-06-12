package com.example.photography.controller;

import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.UserDeviceResponse;
import com.example.photography.model.entity.User;
import com.example.photography.service.UserDeviceService;
import com.example.photography.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户设备管理控制器
 */
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "设备管理", description = "用户设备绑定和管理")
public class UserDeviceController {
    
    private final UserDeviceService userDeviceService;
    
    /**
     * 获取当前用户的设备列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的设备", description = "获取当前用户绑定的所有设备")
    public ApiResponse<List<UserDeviceResponse>> getMyDevices() {
        System.out.println("=== 开始处理 /api/devices/my 请求 ===");
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            System.out.println("当前用户: " + (currentUser != null ? currentUser.getUsername() : "null"));
            
            if (currentUser == null) {
                System.out.println("用户未登录，返回错误");
                return ApiResponse.error("用户未登录");
            }
            
            System.out.println("开始查询用户设备，用户ID: " + currentUser.getId());
            List<UserDeviceResponse> devices = userDeviceService.getUserDevices(currentUser);
            System.out.println("查询到设备数量: " + devices.size());
            
            return ApiResponse.success("获取成功", devices);
        } catch (Exception e) {
            System.out.println("获取设备列表时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("获取设备列表失败: " + e.getMessage());
        }
    }
    
    // 用户解绑功能已移除 - 防止绕过单设备限制
    // 只有管理员可以强制解绑设备
    
    /**
     * 管理员获取所有设备列表（分页）
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有设备", description = "管理员获取系统中所有设备列表")
    public ApiResponse<Page<UserDeviceResponse>> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        try {
            List<UserDeviceResponse> allDevices = userDeviceService.getAllDevices();
            
            // 如果有关键词搜索，进行过滤
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.toLowerCase().trim();
                allDevices = allDevices.stream()
                    .filter(device -> 
                        (device.getUser() != null && 
                         (device.getUser().getUsername().toLowerCase().contains(lowerKeyword) ||
                          device.getUser().getRealName().toLowerCase().contains(lowerKeyword))) ||
                        (device.getDeviceName() != null && device.getDeviceName().toLowerCase().contains(lowerKeyword)) ||
                        (device.getOsInfo() != null && device.getOsInfo().toLowerCase().contains(lowerKeyword)) ||
                        (device.getBrowserInfo() != null && device.getBrowserInfo().toLowerCase().contains(lowerKeyword))
                    )
                    .toList();
            }
            
            // 分页处理
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allDevices.size());
            
            List<UserDeviceResponse> pageContent = allDevices.subList(start, end);
            Page<UserDeviceResponse> devicePage = new PageImpl<>(pageContent, pageable, allDevices.size());
            
            return ApiResponse.success("获取成功", devicePage);
        } catch (Exception e) {
            return ApiResponse.error("获取设备列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员强制解绑设备
     */
    @DeleteMapping("/admin/{deviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "强制解绑设备", description = "管理员强制解绑指定设备")
    public ApiResponse<Void> adminUnbindDevice(@PathVariable Long deviceId) {
        try {
            userDeviceService.adminUnbindDevice(deviceId);
            return ApiResponse.success("设备强制解绑成功");
        } catch (Exception e) {
            return ApiResponse.error("设备强制解绑失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员重置用户设备绑定
     */
    @PostMapping("/admin/reset-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重置用户设备绑定", description = "管理员重置指定用户的所有设备绑定")
    public ApiResponse<Void> resetUserDevices(@PathVariable Long userId) {
        try {
            userDeviceService.resetUserDevices(userId);
            return ApiResponse.success("用户设备绑定重置成功");
        } catch (Exception e) {
            return ApiResponse.error("重置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取设备统计信息
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取设备统计", description = "获取设备使用统计信息")
    public ApiResponse<Map<String, Object>> getDeviceStatistics() {
        try {
            List<UserDeviceResponse> allDevices = userDeviceService.getAllDevices();
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 总设备数
            statistics.put("totalDevices", allDevices.size());
            
            // 激活设备数
            long activeDevices = allDevices.stream()
                .filter(device -> device.getIsActive() != null && device.getIsActive())
                .count();
            statistics.put("activeDevices", activeDevices);
            
            // 设备类型分布
            Map<String, Long> deviceTypeCount = new HashMap<>();
            allDevices.stream()
                .filter(device -> device.getDeviceType() != null)
                .forEach(device -> {
                    String type = device.getDeviceType();
                    deviceTypeCount.put(type, deviceTypeCount.getOrDefault(type, 0L) + 1);
                });
            statistics.put("deviceTypeDistribution", deviceTypeCount);
            
            // 绑定状态分布
            Map<String, Long> bindStatusCount = new HashMap<>();
            allDevices.stream()
                .filter(device -> device.getBindStatus() != null)
                .forEach(device -> {
                    String status = device.getBindStatus();
                    bindStatusCount.put(status, bindStatusCount.getOrDefault(status, 0L) + 1);
                });
            statistics.put("bindStatusDistribution", bindStatusCount);
            
            // 操作系统分布（简化）
            Map<String, Long> osCount = new HashMap<>();
            allDevices.stream()
                .filter(device -> device.getOsInfo() != null)
                .forEach(device -> {
                    String os = device.getOsInfo();
                    String osType = "其他";
                    if (os.toLowerCase().contains("windows")) {
                        osType = "Windows";
                    } else if (os.toLowerCase().contains("mac") || os.toLowerCase().contains("ios")) {
                        osType = "macOS/iOS";
                    } else if (os.toLowerCase().contains("android")) {
                        osType = "Android";
                    } else if (os.toLowerCase().contains("linux")) {
                        osType = "Linux";
                    }
                    osCount.put(osType, osCount.getOrDefault(osType, 0L) + 1);
                });
            statistics.put("osDistribution", osCount);
            
            return ApiResponse.success("获取统计信息成功", statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理长时间未活跃的设备
     */
    @PostMapping("/admin/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清理未活跃设备", description = "清理长时间未活跃的设备")
    public ApiResponse<Void> cleanupInactiveDevices() {
        try {
            userDeviceService.cleanupInactiveDevices();
            return ApiResponse.success("设备清理完成，已彻底删除无效设备记录");
        } catch (Exception e) {
            return ApiResponse.error("设备清理失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理未激活设备
     */
    @PostMapping("/admin/cleanup-unbound")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清理未激活设备", description = "管理员清理所有未激活或已撤销的设备记录")
    public ApiResponse<Void> cleanupUnboundDevices() {
        try {
            userDeviceService.cleanupUnboundDevices();
            return ApiResponse.success("未激活设备清理完成，已彻底删除无效记录");
        } catch (Exception e) {
            return ApiResponse.error("清理失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试用：创建移动端设备记录
     */
    @PostMapping("/admin/create-test-mobile")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建测试移动设备", description = "为演示目的创建一个测试移动设备记录")
    public ApiResponse<Void> createTestMobileDevice() {
        try {
            userDeviceService.createTestMobileDevice();
            return ApiResponse.success("测试移动设备创建成功");
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员物理删除设备记录
     */
    @DeleteMapping("/admin/physical/{deviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "物理删除设备记录", description = "管理员彻底删除设备记录和相关审计日志，释放存储空间")
    public ApiResponse<Void> adminPhysicalDeleteDevice(@PathVariable Long deviceId) {
        try {
            userDeviceService.adminPhysicalDeleteDevice(deviceId);
            return ApiResponse.success("设备记录已彻底删除，存储空间已释放");
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员删除用户所有设备记录
     */
    @DeleteMapping("/admin/user/{userId}/all-devices")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户所有设备", description = "管理员删除指定用户的所有设备记录和审计日志")
    public ApiResponse<Void> adminDeleteAllUserDevices(@PathVariable Long userId) {
        try {
            userDeviceService.adminDeleteAllUserDevices(userId);
            return ApiResponse.success("用户所有设备记录已彻底删除");
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}
