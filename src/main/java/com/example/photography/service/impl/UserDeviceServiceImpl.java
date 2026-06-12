package com.example.photography.service.impl;

import com.example.photography.dto.request.DeviceInfoRequest;
import com.example.photography.dto.response.UserDeviceResponse;
import com.example.photography.model.entity.DeviceAuditLog;
import com.example.photography.model.entity.User;
import com.example.photography.model.entity.UserDevice;
import com.example.photography.repository.DeviceAuditLogRepository;
import com.example.photography.repository.UserDeviceRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.UserDeviceService;
import com.example.photography.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户设备管理服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeviceServiceImpl implements UserDeviceService {
    
    private final UserDeviceRepository userDeviceRepository;
    private final DeviceAuditLogRepository deviceAuditLogRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public DeviceValidationResult validateAndBindDevice(User user, DeviceInfoRequest deviceInfo, String ipAddress, String userAgent) {
        log.info("验证用户设备: userId={}, fingerprint={}", user.getId(), deviceInfo.getDeviceFingerprint());
        
        // 根据设备类型，允许绑定两个槽位：PC端（DESKTOP）一个、移动端（MOBILE/TABLET）一个
        UserDevice.DeviceType incomingType = parseDeviceType(deviceInfo.getDeviceType());
        boolean isDesktop = incomingType == UserDevice.DeviceType.DESKTOP;
        boolean isMobileGroup = incomingType == UserDevice.DeviceType.MOBILE || incomingType == UserDevice.DeviceType.TABLET;

        if (isDesktop) {
            return validateDeviceInSlot(
                    user,
                    deviceInfo,
                    ipAddress,
                    UserDevice.DeviceType.DESKTOP,
                    List.of(UserDevice.DeviceType.DESKTOP),
                    "PC端",
                    "当前账号已在其他PC设备上绑定，无法在此设备登录。如需更换设备，请联系管理员。"
            );
        }

        if (isMobileGroup) {
            return validateDeviceInSlot(
                    user,
                    deviceInfo,
                    ipAddress,
                    incomingType,
                    List.of(UserDevice.DeviceType.MOBILE, UserDevice.DeviceType.TABLET),
                    "移动端",
                    "当前账号已在其他移动设备上绑定，无法在此设备登录。如需更换设备，请联系管理员。"
            );
        }

        // 若类型未知，拒绝绑定
        return new DeviceValidationResult(false, "无法识别的设备类型，拒绝登录", null, DeviceValidationResult.ValidationAction.DEVICE_BLOCKED);
    }

    private DeviceValidationResult validateDeviceInSlot(User user,
                                                       DeviceInfoRequest deviceInfo,
                                                       String ipAddress,
                                                       UserDevice.DeviceType suspendedType,
                                                       List<UserDevice.DeviceType> activeTypes,
                                                       String slotName,
                                                       String blockedMessage) {
        List<UserDevice> activeDevices = userDeviceRepository.findActiveDevicesByUserAndTypes(user, activeTypes);

        Optional<UserDevice> matchingActiveDevice = findExactFingerprintMatch(activeDevices, deviceInfo);
        if (matchingActiveDevice.isPresent()) {
            UserDevice device = matchingActiveDevice.get();
            updateDeviceInfo(device, deviceInfo, ipAddress);
            userDeviceRepository.save(device);
            log.info("{}设备验证通过: userId={}, deviceId={}", slotName, user.getId(), device.getId());
            return new DeviceValidationResult(true, "设备验证通过", device, DeviceValidationResult.ValidationAction.DEVICE_MATCHED);
        }

        Optional<UserDevice> legacyCompatibleDevice = findLegacyCompatibleDevice(activeDevices, deviceInfo);
        if (legacyCompatibleDevice.isPresent()) {
            UserDevice device = legacyCompatibleDevice.get();
            updateDeviceInfo(device, deviceInfo, ipAddress);
            userDeviceRepository.save(device);
            log.info("{}旧设备指纹已兼容更新: userId={}, deviceId={}", slotName, user.getId(), device.getId());
            return new DeviceValidationResult(true, "设备信息已更新", device, DeviceValidationResult.ValidationAction.DEVICE_MATCHED);
        }

        if (!activeDevices.isEmpty()) {
            log.warn("{}设备指纹不匹配，拒绝登录: userId={}, 当前设备指纹={}", 
                slotName, user.getId(), deviceInfo.getDeviceFingerprint());
            return new DeviceValidationResult(false, blockedMessage, null, DeviceValidationResult.ValidationAction.DEVICE_BLOCKED);
        }

        List<UserDevice> suspendedDevices = userDeviceRepository.findSuspendedDevicesByUserAndType(user, suspendedType);
        Optional<UserDevice> matchingSuspendedDevice = findExactFingerprintMatch(suspendedDevices, deviceInfo)
                .or(() -> findLegacyCompatibleDevice(suspendedDevices, deviceInfo));

        if (matchingSuspendedDevice.isPresent()) {
            UserDevice device = matchingSuspendedDevice.get();
            device.setIsActive(true);
            device.setBindStatus(UserDevice.BindStatus.ACTIVE);
            updateDeviceInfo(device, deviceInfo, ipAddress);
            userDeviceRepository.save(device);
            log.info("重新激活{}设备: userId={}, deviceId={}", slotName, user.getId(), device.getId());
            return new DeviceValidationResult(true, "设备重新激活成功", device, DeviceValidationResult.ValidationAction.DEVICE_REACTIVATED);
        }

        UserDevice saved = userDeviceRepository.save(createNewDevice(user, deviceInfo, ipAddress));
        log.info("绑定{}设备成功: userId={}, deviceId={}", slotName, user.getId(), saved.getId());
        return new DeviceValidationResult(true, "设备绑定成功", saved, DeviceValidationResult.ValidationAction.FIRST_BIND);
    }
    
    @Override
    public UserDevice getActiveDevice(User user) {
        return userDeviceRepository.findActiveDeviceByUser(user).orElse(null);
    }

    @Override
    public UserDevice getActiveDesktopDevice(User user) {
        return userDeviceRepository.findActiveDevicesByUserAndTypes(user, List.of(UserDevice.DeviceType.DESKTOP))
                .stream().findFirst().orElse(null);
    }

    @Override
    public UserDevice getActiveMobileDevice(User user) {
        return userDeviceRepository.findActiveDevicesByUserAndTypes(user, List.of(UserDevice.DeviceType.MOBILE, UserDevice.DeviceType.TABLET))
                .stream().findFirst().orElse(null);
    }

    @Override
    public Optional<UserDevice> findActiveDeviceByFingerprint(User user, String deviceFingerprint) {
        return userDeviceRepository.findActiveByUserAndDeviceFingerprint(user, deviceFingerprint);
    }
    
    @Override
    public List<UserDeviceResponse> getUserDevices(User user) {
        System.out.println("=== UserDeviceService.getUserDevices 开始 ===");
        System.out.println("查询用户: " + user.getUsername() + " (ID: " + user.getId() + ")");
        
        try {
            List<UserDevice> devices = userDeviceRepository.findAllByUser(user);
            System.out.println("从数据库查询到的设备数量: " + devices.size());
            
            for (int i = 0; i < devices.size(); i++) {
                UserDevice device = devices.get(i);
                System.out.println("设备 " + (i+1) + ": ID=" + device.getId() + 
                    ", 名称=" + device.getDeviceName() + 
                    ", 激活=" + device.getIsActive() + 
                    ", 用户=" + (device.getUser() != null ? device.getUser().getUsername() : "null"));
            }
            
            List<UserDeviceResponse> responses = devices.stream()
                    .map(device -> {
                        try {
                            System.out.println("转换设备: " + device.getId());
                            return UserDeviceResponse.fromEntity(device);
                        } catch (Exception e) {
                            System.out.println("转换设备 " + device.getId() + " 时发生异常: " + e.getMessage());
                            e.printStackTrace();
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());
            
            System.out.println("转换后的响应数量: " + responses.size());
            System.out.println("=== UserDeviceService.getUserDevices 完成 ===");
            return responses;
        } catch (Exception e) {
            System.out.println("getUserDevices 发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // 用户解绑功能已移除 - 防止绕过单设备限制
    // 只有管理员可以强制解绑设备
    
    @Override
    @Transactional
    public void updateDeviceActiveTime(UserDevice device) {
        LocalDateTime now = LocalDateTime.now();
        userDeviceRepository.updateLastActiveTime(device.getId(), now, now);
    }
    
    @Override
    public List<UserDeviceResponse> getAllDevices() {
        // 管理员查看所有设备（包括停用的设备）
        List<UserDevice> devices = userDeviceRepository.findAllDevicesForAdmin();
        return devices.stream()
                .map(UserDeviceResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void adminUnbindDevice(Long deviceId) {
        Optional<UserDevice> deviceOpt = userDeviceRepository.findById(deviceId);
        if (deviceOpt.isPresent()) {
            UserDevice device = deviceOpt.get();
            Long userId = device.getUser().getId();
            String deviceName = device.getDeviceName();
            
            // 直接删除设备记录以释放存储空间
            userDeviceRepository.delete(device);
            
            log.info("管理员强制解绑并删除设备记录: deviceId={}, userId={}, deviceName={}", 
                    deviceId, userId, deviceName);
        }
    }
    
    @Override
    @Transactional
    public void resetUserDevices(Long userId) {
        List<UserDevice> devices = userDeviceRepository.findAllByUser_IdAndIsActiveTrueAndDeletedFalse(userId);
        
        if (!devices.isEmpty()) {
            // 记录删除的设备信息用于日志
            List<String> deviceNames = devices.stream()
                    .map(UserDevice::getDeviceName)
                    .collect(Collectors.toList());
            
            // 直接删除所有设备记录以释放存储空间
            userDeviceRepository.deleteAll(devices);
            
            log.info("管理员重置并删除用户设备记录: userId={}, 删除设备数量={}, 设备列表={}", 
                    userId, devices.size(), deviceNames);
        }
    }
    
    @Override
    @Transactional
    public void cleanupInactiveDevices() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30); // 30天未活跃
        List<UserDevice> inactiveDevices = userDeviceRepository.findInactiveDevices(threshold);
        
        if (!inactiveDevices.isEmpty()) {
            // 记录删除的设备信息用于日志
            List<String> deviceInfo = inactiveDevices.stream()
                    .map(device -> String.format("用户%s的%s", 
                            device.getUser().getUsername(), device.getDeviceName()))
                    .collect(Collectors.toList());
            
            // 直接删除长时间未活跃的设备记录以释放存储空间
            userDeviceRepository.deleteAll(inactiveDevices);
            
            log.info("清理并删除长时间未活跃设备记录: 设备数量={}, 设备信息={}", 
                    inactiveDevices.size(), deviceInfo);
        }
    }
    
    @Override
    @Transactional
    public void cleanupUnboundDevices() {
        List<UserDevice> unboundDevices = userDeviceRepository.findByIsActiveFalseOrBindStatusNot(UserDevice.BindStatus.ACTIVE);
        
        if (!unboundDevices.isEmpty()) {
            // 记录删除的设备信息用于日志
            List<String> deviceInfo = unboundDevices.stream()
                    .map(device -> String.format("用户%s的%s(状态:%s)", 
                            device.getUser().getUsername(), 
                            device.getDeviceName(),
                            device.getBindStatus()))
                    .collect(Collectors.toList());
            
            // 直接删除未激活的设备记录以释放存储空间
            userDeviceRepository.deleteAll(unboundDevices);
            
            log.info("清理并删除未激活设备记录: 设备数量={}, 设备信息={}", 
                    unboundDevices.size(), deviceInfo);
        }
    }
    
    /**
     * 创建新设备记录
     */
    private UserDevice createNewDevice(User user, DeviceInfoRequest deviceInfo, String ipAddress) {
        UserDevice device = new UserDevice();
        device.setUser(user);
        device.setDeviceFingerprint(deviceInfo.getDeviceFingerprint());
        device.setDeviceName(deviceInfo.getDeviceName());
        device.setDeviceType(parseDeviceType(deviceInfo.getDeviceType()));
        device.setOsInfo(deviceInfo.getOsInfo());
        device.setBrowserInfo(deviceInfo.getBrowserInfo());
        device.setScreenResolution(deviceInfo.getScreenResolution());
        device.setTimezone(deviceInfo.getTimezone());
        device.setLanguage(deviceInfo.getLanguage());
        device.setIpAddress(ipAddress);
        device.setIsActive(true);
        device.setFirstBoundAt(LocalDateTime.now());
        device.setLastActiveAt(LocalDateTime.now());
        device.setBindStatus(UserDevice.BindStatus.ACTIVE);
        
        return device;
    }
    
    /**
     * 更新设备信息
     */
    private void updateDeviceInfo(UserDevice device, DeviceInfoRequest deviceInfo, String ipAddress) {
        device.setIpAddress(ipAddress);
        device.setLastActiveAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        
        // 更新可能变化的信息
        if (StringUtils.hasText(deviceInfo.getDeviceFingerprint())) {
            device.setDeviceFingerprint(deviceInfo.getDeviceFingerprint());
        }
        if (StringUtils.hasText(deviceInfo.getDeviceName())) {
            device.setDeviceName(deviceInfo.getDeviceName());
        }
        UserDevice.DeviceType deviceType = parseDeviceType(deviceInfo.getDeviceType());
        if (deviceType != UserDevice.DeviceType.UNKNOWN) {
            device.setDeviceType(deviceType);
        }
        if (StringUtils.hasText(deviceInfo.getOsInfo())) {
            device.setOsInfo(deviceInfo.getOsInfo());
        }
        if (StringUtils.hasText(deviceInfo.getBrowserInfo())) {
            device.setBrowserInfo(deviceInfo.getBrowserInfo());
        }
        if (deviceInfo.getScreenResolution() != null) {
            device.setScreenResolution(deviceInfo.getScreenResolution());
        }
        if (deviceInfo.getTimezone() != null) {
            device.setTimezone(deviceInfo.getTimezone());
        }
        if (deviceInfo.getLanguage() != null) {
            device.setLanguage(deviceInfo.getLanguage());
        }
    }

    private Optional<UserDevice> findExactFingerprintMatch(List<UserDevice> devices, DeviceInfoRequest deviceInfo) {
        if (deviceInfo == null || !StringUtils.hasText(deviceInfo.getDeviceFingerprint())) {
            return Optional.empty();
        }

        return devices.stream()
                .filter(device -> deviceInfo.getDeviceFingerprint().equals(device.getDeviceFingerprint()))
                .findFirst();
    }

    private Optional<UserDevice> findLegacyCompatibleDevice(List<UserDevice> devices, DeviceInfoRequest deviceInfo) {
        return devices.stream()
                .filter(device -> canMigrateLegacyFingerprint(device, deviceInfo))
                .findFirst();
    }

    /**
     * 兼容旧版前端生成的易波动指纹。只迁移旧哈希记录，迁移后必须继续精确匹配新的稳定ID。
     */
    private boolean canMigrateLegacyFingerprint(UserDevice device, DeviceInfoRequest deviceInfo) {
        if (device == null || deviceInfo == null) {
            return false;
        }

        if (!isLegacyFingerprint(device.getDeviceFingerprint()) || !StringUtils.hasText(deviceInfo.getDeviceFingerprint())) {
            return false;
        }

        UserDevice.DeviceType incomingType = parseDeviceType(deviceInfo.getDeviceType());
        if (!isSameDeviceGroup(device.getDeviceType(), incomingType)) {
            return false;
        }

        return isSameFamily(device.getOsInfo(), deviceInfo.getOsInfo())
                && isCompatibleBrowserFamily(device.getBrowserInfo(), deviceInfo.getBrowserInfo())
                && isSameScreenResolution(device.getScreenResolution(), deviceInfo.getScreenResolution());
    }

    private boolean isLegacyFingerprint(String fingerprint) {
        if (!StringUtils.hasText(fingerprint)) {
            return false;
        }

        if (fingerprint.startsWith("browser_") || fingerprint.startsWith("fallback_")) {
            return false;
        }

        return fingerprint.matches("(?i)^[0-9a-f]{8,128}$");
    }

    private boolean isSameDeviceGroup(UserDevice.DeviceType existingType, UserDevice.DeviceType incomingType) {
        if (existingType == null || incomingType == null || incomingType == UserDevice.DeviceType.UNKNOWN) {
            return false;
        }

        boolean existingMobile = existingType == UserDevice.DeviceType.MOBILE || existingType == UserDevice.DeviceType.TABLET;
        boolean incomingMobile = incomingType == UserDevice.DeviceType.MOBILE || incomingType == UserDevice.DeviceType.TABLET;

        if (existingMobile || incomingMobile) {
            return existingMobile && incomingMobile;
        }

        return existingType == incomingType;
    }

    private boolean isSameFamily(String existingValue, String incomingValue) {
        String existingFamily = normalizeFamily(existingValue);
        String incomingFamily = normalizeFamily(incomingValue);
        return StringUtils.hasText(existingFamily)
                && StringUtils.hasText(incomingFamily)
                && existingFamily.equalsIgnoreCase(incomingFamily);
    }

    private boolean isCompatibleBrowserFamily(String existingBrowser, String incomingBrowser) {
        String existingFamily = normalizeFamily(existingBrowser).toLowerCase();
        String incomingFamily = normalizeFamily(incomingBrowser).toLowerCase();

        if (!StringUtils.hasText(existingFamily) || !StringUtils.hasText(incomingFamily)) {
            return false;
        }

        if (existingFamily.equals(incomingFamily)) {
            return true;
        }

        // 旧版前端会把 Edge、三星、QQ、微信等 Chromium 内核浏览器识别为 Chrome。
        if ("chrome".equals(existingFamily)
                && List.of("edge", "opera", "samsung internet", "uc browser", "qq browser", "wechat").contains(incomingFamily)) {
            return true;
        }

        // iOS 上第三方浏览器 UA 常带 Safari，旧版前端会误识别为 Safari。
        return "safari".equals(existingFamily)
                && List.of("chrome ios", "edge ios", "firefox ios", "wechat").contains(incomingFamily);
    }

    private String normalizeFamily(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim()
                .replaceAll("\\s+[\\d._]+$", "")
                .replaceAll("\\s+", " ");
    }

    private boolean isSameScreenResolution(String existingResolution, String incomingResolution) {
        String existing = normalizeScreenResolution(existingResolution);
        String incoming = normalizeScreenResolution(incomingResolution);
        return StringUtils.hasText(existing)
                && StringUtils.hasText(incoming)
                && existing.equalsIgnoreCase(incoming);
    }

    private String normalizeScreenResolution(String resolution) {
        if (!StringUtils.hasText(resolution) || !resolution.contains("x")) {
            return resolution;
        }

        String[] parts = resolution.toLowerCase().split("x");
        if (parts.length != 2) {
            return resolution;
        }

        try {
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());
            int min = Math.min(width, height);
            int max = Math.max(width, height);
            return min + "x" + max;
        } catch (NumberFormatException e) {
            return resolution;
        }
    }
    
    /**
     * 彻底清理可能冲突的设备（物理删除）
     */
    @Transactional
    private void deactivateOldDevices(User user, UserDevice.DeviceType deviceType) {
        log.info("停用用户 {} 的旧 {} 设备", user.getUsername(), deviceType);
        
        try {
            List<UserDevice> oldDevices;
            if (deviceType == UserDevice.DeviceType.DESKTOP) {
                oldDevices = userDeviceRepository.findActiveDevicesByUserAndTypes(user, List.of(UserDevice.DeviceType.DESKTOP));
            } else {
                oldDevices = userDeviceRepository.findActiveDevicesByUserAndTypes(user, List.of(UserDevice.DeviceType.MOBILE, UserDevice.DeviceType.TABLET));
            }
            
            for (UserDevice device : oldDevices) {
                device.setIsActive(false);
                device.setBindStatus(UserDevice.BindStatus.SUSPENDED);
                device.setUpdatedAt(LocalDateTime.now());
                userDeviceRepository.save(device);
                log.info("停用设备: ID={}, 用户={}, 类型={}", device.getId(), user.getUsername(), device.getDeviceType());
            }
            
            userDeviceRepository.flush();
            log.info("用户 {} 的旧 {} 设备停用完成", user.getUsername(), deviceType);
        } catch (Exception e) {
            log.error("停用用户 {} 的 {} 设备时发生错误: {}", user.getUsername(), deviceType, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 处理设备冲突 - 安全地停用旧设备并创建新设备
     */
    @Transactional
    /*
     * 注释：handleDeviceConflict 方法已移除
     * 
     * 新的设备绑定策略变更说明：
     * - 不再允许设备替换操作
     * - 采用严格的设备指纹验证机制
     * - 当设备指纹不匹配时，直接拒绝登录并提示用户联系管理员
     * - 这样可以防止恶意设备替换，提高系统安全性
     * 
     * 如果需要更换设备，必须通过管理员后台进行操作
     */
    
    /**
     * 解析设备类型
     */
    private UserDevice.DeviceType parseDeviceType(String deviceTypeStr) {
        if (deviceTypeStr == null) {
            return UserDevice.DeviceType.UNKNOWN;
        }
        
        try {
            return UserDevice.DeviceType.valueOf(deviceTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UserDevice.DeviceType.UNKNOWN;
        }
    }
    
    @Override
    @Transactional
    public void createTestMobileDevice() {
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 创建一个测试移动设备
        UserDevice mobileDevice = new UserDevice();
        mobileDevice.setUser(currentUser);
        mobileDevice.setDeviceFingerprint("test_mobile_" + System.currentTimeMillis());
        mobileDevice.setDeviceName("测试手机");
        mobileDevice.setDeviceType(UserDevice.DeviceType.MOBILE);
        mobileDevice.setOsInfo("Android 14");
        mobileDevice.setBrowserInfo("Chrome Mobile 120.0.0");
        mobileDevice.setScreenResolution("1080x2340");
        mobileDevice.setTimezone("Asia/Shanghai");
        mobileDevice.setLanguage("zh-CN");
        mobileDevice.setIpAddress("192.168.1.100");
        mobileDevice.setIsActive(true);
        mobileDevice.setBindStatus(UserDevice.BindStatus.ACTIVE);
        mobileDevice.setFirstBoundAt(LocalDateTime.now().minusDays(1));
        mobileDevice.setLastActiveAt(LocalDateTime.now().minusHours(2));
        
        // 先停用可能的冲突设备
        deactivateOldDevices(currentUser, UserDevice.DeviceType.MOBILE);
        
        userDeviceRepository.save(mobileDevice);
        
        // 记录审计日志
        createAuditLog(currentUser, mobileDevice, DeviceAuditLog.ActionType.DEVICE_CREATED, 
                      "创建测试移动设备", "127.0.0.1", "Test User Agent");
        
        log.info("为用户 {} 创建了测试移动设备", currentUser.getUsername());
    }
    
    /**
     * 创建审计日志
     */
    private void createAuditLog(User user, UserDevice device, DeviceAuditLog.ActionType actionType, 
                               String description, String ipAddress, String userAgent) {
        try {
            DeviceAuditLog auditLog = new DeviceAuditLog();
            auditLog.setUser(user);
            auditLog.setDeviceId(device != null ? device.getId() : null);
            auditLog.setDeviceFingerprint(device != null ? device.getDeviceFingerprint() : "unknown");
            auditLog.setDeviceName(device != null ? device.getDeviceName() : "unknown");
            auditLog.setDeviceType(device != null ? device.getDeviceType() : UserDevice.DeviceType.UNKNOWN);
            auditLog.setActionType(actionType);
            auditLog.setActionDescription(description);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setActionTime(LocalDateTime.now());
            
            deviceAuditLogRepository.save(auditLog);
            log.debug("审计日志已记录: user={}, action={}, device={}", user.getUsername(), actionType, device != null ? device.getId() : "null");
        } catch (Exception e) {
            log.error("记录审计日志失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响主业务流程
        }
    }
    
    @Override
    @Transactional
    public void adminPhysicalDeleteDevice(Long deviceId) {
        try {
            // 查找设备记录
            Optional<UserDevice> deviceOpt = userDeviceRepository.findById(deviceId);
            if (deviceOpt.isEmpty()) {
                throw new RuntimeException("设备记录不存在");
            }
            
            UserDevice device = deviceOpt.get();
            User user = device.getUser();
            
            log.warn("管理员物理删除设备: deviceId={}, user={}, fingerprint={}", 
                    deviceId, user.getUsername(), device.getDeviceFingerprint());
            
            // 1. 先删除相关的审计日志
            deviceAuditLogRepository.deleteByDeviceId(deviceId);
            
            // 2. 记录删除操作的审计日志（在删除设备记录之前）
            createAuditLog(user, device, DeviceAuditLog.ActionType.DEVICE_DELETED, 
                          "管理员物理删除设备记录", "127.0.0.1", "Admin Operation");
            
            // 3. 删除设备记录
            userDeviceRepository.deleteById(deviceId);
            
            log.info("设备记录物理删除完成: deviceId={}, user={}", deviceId, user.getUsername());
            
        } catch (Exception e) {
            log.error("物理删除设备记录失败: deviceId={}, error={}", deviceId, e.getMessage());
            throw new RuntimeException("删除设备记录失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void adminDeleteAllUserDevices(Long userId) {
        try {
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            log.warn("管理员删除用户所有设备记录: userId={}, username={}", userId, user.getUsername());
            
            // 1. 获取用户所有设备记录
            List<UserDevice> userDevices = userDeviceRepository.findAllByUser(user);
            
            if (userDevices.isEmpty()) {
                log.info("用户 {} 没有设备记录需要删除", user.getUsername());
                return;
            }
            
            log.info("找到用户 {} 的 {} 个设备记录，准备删除", user.getUsername(), userDevices.size());
            
            // 2. 删除所有相关的审计日志
            deviceAuditLogRepository.deleteByUser(user);
            
            // 3. 记录批量删除操作的审计日志
            createAuditLog(user, null, DeviceAuditLog.ActionType.DEVICE_DELETED, 
                          String.format("管理员批量删除用户所有设备记录，共%d个设备", userDevices.size()), 
                          "127.0.0.1", "Admin Operation");
            
            // 4. 物理删除所有设备记录
            userDeviceRepository.deleteByUserId(userId);
            
            log.info("用户 {} 的所有设备记录删除完成，共删除 {} 个设备", user.getUsername(), userDevices.size());
            
        } catch (Exception e) {
            log.error("删除用户所有设备记录失败: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("删除用户设备记录失败: " + e.getMessage());
        }
    }
}
