package com.example.photography.service;

import com.example.photography.dto.request.DeviceInfoRequest;
import com.example.photography.dto.response.UserDeviceResponse;
import com.example.photography.model.entity.User;
import com.example.photography.model.entity.UserDevice;

import java.util.List;

/**
 * 用户设备管理服务接口
 */
public interface UserDeviceService {
    
    /**
     * 验证设备并绑定（首次登录）或验证（非首次登录）
     * 
     * @param user 用户
     * @param deviceInfo 设备信息
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 验证结果
     */
    DeviceValidationResult validateAndBindDevice(User user, DeviceInfoRequest deviceInfo, String ipAddress, String userAgent);
    
    /**
     * 获取用户的激活设备
     * 
     * @param user 用户
     * @return 激活设备
     */
    UserDevice getActiveDevice(User user);

    /**
     * 获取用户的激活PC端设备
     */
    UserDevice getActiveDesktopDevice(User user);

    /**
     * 获取用户的激活移动端设备（合并 MOBILE / TABLET）
     */
    UserDevice getActiveMobileDevice(User user);

    /**
     * 根据用户与指纹查找激活设备
     */
    java.util.Optional<UserDevice> findActiveDeviceByFingerprint(User user, String deviceFingerprint);
    
    /**
     * 获取用户所有设备
     * 
     * @param user 用户
     * @return 设备列表
     */
    List<UserDeviceResponse> getUserDevices(User user);
    
    // 用户解绑功能已移除 - 防止绕过单设备限制
    // 只有管理员可以强制解绑设备
    
    /**
     * 更新设备活跃时间
     * 
     * @param device 设备
     */
    void updateDeviceActiveTime(UserDevice device);
    
    /**
     * 管理员获取所有设备
     * 
     * @return 所有设备列表
     */
    List<UserDeviceResponse> getAllDevices();
    
    /**
     * 管理员强制解绑设备
     * 
     * @param deviceId 设备ID
     */
    void adminUnbindDevice(Long deviceId);
    
    /**
     * 管理员重置用户设备绑定
     * 
     * @param userId 用户ID
     */
    void resetUserDevices(Long userId);
    
    /**
     * 清理长时间未活跃的设备
     */
    void cleanupInactiveDevices();
    
    /**
     * 清理未激活或已撤销的设备记录
     */
    void cleanupUnboundDevices();
    
    /**
     * 创建测试移动设备记录（仅用于演示）
     */
    void createTestMobileDevice();
    
    /**
     * 管理员物理删除设备记录（包括审计日志）
     * 
     * @param deviceId 设备ID
     */
    void adminPhysicalDeleteDevice(Long deviceId);
    
    /**
     * 管理员批量删除用户的所有设备记录
     * 
     * @param userId 用户ID
     */
    void adminDeleteAllUserDevices(Long userId);
    
    /**
     * 设备验证结果
     */
    public static class DeviceValidationResult {
        private boolean success;
        private String message;
        private UserDevice device;
        private ValidationAction action;
        
        public DeviceValidationResult(boolean success, String message, UserDevice device, ValidationAction action) {
            this.success = success;
            this.message = message;
            this.device = device;
            this.action = action;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UserDevice getDevice() { return device; }
        public ValidationAction getAction() { return action; }
        
        public enum ValidationAction {
            FIRST_BIND("首次绑定"),
            DEVICE_MATCHED("设备匹配"),
            DEVICE_MISMATCH("设备不匹配"),
            DEVICE_BLOCKED("设备被阻止"),
            DEVICE_REACTIVATED("设备重新激活");
            
            private final String description;
            
            ValidationAction(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
}
