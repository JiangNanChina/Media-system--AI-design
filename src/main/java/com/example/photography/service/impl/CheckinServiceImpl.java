package com.example.photography.service.impl;

import com.example.photography.dto.request.CheckinRequest;
import com.example.photography.dto.request.CheckoutRequest;
import com.example.photography.dto.response.CheckinResponse;
import com.example.photography.model.entity.*;
import com.example.photography.repository.*;
import com.example.photography.service.CheckinService;
import com.example.photography.utils.CheckinWeekdayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 打卡服务实现类（重构版本）
 */
@Slf4j
@Service
@Transactional
public class CheckinServiceImpl implements CheckinService {
    
    @Autowired
    private CheckinRecordRepository checkinRecordRepository;
    
    @Autowired
    private CheckinConfigurationRepository checkinConfigurationRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckinDeviceUsageRepository checkinDeviceUsageRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public CheckinResponse checkin(CheckinRequest request, Long userId) {
        try {
            log.info("=== 开始打卡流程 ===");
            log.info("用户ID: {}, 配置ID: {}", userId, request.getConfigurationId());
            
            // 验证用户
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            log.info("用户信息: id={}, username={}, realName={}", user.getId(), user.getUsername(), user.getRealName());
            
            // 验证配置（预加载考勤人员列表以便进行权限验证）
            CheckinConfiguration configuration = checkinConfigurationRepository.findByIdWithUsers(request.getConfigurationId())
                .orElseThrow(() -> new RuntimeException("打卡配置不存在"));
            log.info("配置信息: id={}, name={}, active={}", configuration.getId(), configuration.getName(), configuration.getIsActive());
            
            // 检查配置是否启用
            if (!configuration.getIsActive()) {
                throw new RuntimeException("该打卡配置已停用");
            }

            LocalDate today = LocalDate.now();
            if (!CheckinWeekdayUtils.isRequiredOnDate(configuration.getRequiredWeekdays(), today)) {
                throw new RuntimeException("今天不需要进行该晚自习打卡");
            }
            
            // ========== 权限验证：检查用户是否被配置为考勤人员 ==========
            log.info("=== 开始权限验证 ===");
            Set<User> requiredUsers = configuration.getRequiredUsers();
            log.info("权限验证详情: userId={}, configId={}, configName={}", 
                userId, configuration.getId(), configuration.getName());
            log.info("配置的考勤人员数量: {}", requiredUsers != null ? requiredUsers.size() : 0);
            
            // 打印所有考勤人员信息
            if (requiredUsers != null) {
                log.info("考勤人员列表:");
                for (User reqUser : requiredUsers) {
                    log.info("  - 用户ID: {}, 用户名: {}, 姓名: {}", 
                        reqUser.getId(), reqUser.getUsername(), reqUser.getRealName());
                }
            } else {
                log.warn("requiredUsers is null!");
            }
            
            // 如果配置中没有设置考勤人员，则拒绝所有人签到
            if (requiredUsers == null || requiredUsers.isEmpty()) {
                log.warn("!!! 该打卡配置未设置考勤人员，拒绝签到: userId={}, configId={}", userId, configuration.getId());
                throw new RuntimeException("该打卡配置未设置考勤人员，请联系管理员先配置考勤人员列表。");
            }
            
            // 检查当前用户是否在考勤人员列表中
            log.info("检查用户 {} 是否在考勤人员列表中...", userId);
            boolean isAuthorizedUser = requiredUsers.stream()
                .peek(reqUser -> log.info("对比考勤人员: userId={} vs {}, userName={}", 
                    userId, reqUser.getId(), reqUser.getUsername()))
                .anyMatch(requiredUser -> {
                    boolean matches = requiredUser.getId().equals(userId);
                    log.info("用户匹配结果: {} == {} = {}", userId, requiredUser.getId(), matches);
                    return matches;
                });
            
            log.info("权限验证结果: isAuthorizedUser = {}", isAuthorizedUser);
            
            if (!isAuthorizedUser) {
                log.error("!!! 用户无权限进行此打卡配置的签到 !!!");
                log.error("当前用户ID: {}", userId);
                log.error("配置ID: {}", configuration.getId());
                log.error("已配置考勤人员: {}", 
                    requiredUsers.stream()
                        .map(u -> u.getId() + "(" + u.getUsername() + ")")
                        .toArray());
                throw new RuntimeException("您未被配置为该打卡时段的考勤人员，无法进行签到操作。如有疑问请联系管理员。");
            }
            
            log.info("=== 用户权限验证通过 ===: userId={}, configId={}, configName={}", 
                userId, configuration.getId(), configuration.getName());

            // 设备验证已移除：晚自习签到不再进行设备验证
            // 设备防作弊机制仅在用户登录时进行（参见 AuthServiceImpl.login 方法）
            log.info("晚自习签到：跳过设备验证（设备验证仅在登录时进行）: userId={}", user.getId());
            
            // 验证打卡时间是否在允许的时间段内
            // 注意：只比较小时和分钟，忽略秒和纳秒，避免边界问题
            LocalDateTime now = LocalDateTime.now();
            LocalTime currentTime = now.toLocalTime().withSecond(0).withNano(0);
            LocalTime startTime = configuration.getStartTime().withSecond(0).withNano(0);
            LocalTime endTime = configuration.getEndTime().withSecond(0).withNano(0);
            
            // 获取提前和延迟分钟数，默认为0
            int earlyMinutes = configuration.getEarlyCheckinMinutes() != null ? configuration.getEarlyCheckinMinutes() : 0;
            int lateMinutes = configuration.getLateCheckinMinutes() != null ? configuration.getLateCheckinMinutes() : 0;
            
            // 计算允许的打卡时间范围
            LocalTime earliestTime = startTime.minusMinutes(earlyMinutes);
            LocalTime latestTime = endTime.plusMinutes(lateMinutes);
            
            log.info("🕐 签到时间验证: configId={}, currentTime={}, earliestTime={}, latestTime={}, earlyMinutes={}, lateMinutes={}", 
                configuration.getId(), currentTime, earliestTime, latestTime, earlyMinutes, lateMinutes);
            
            // 检查当前时间是否在允许范围内
            boolean timeValid = false;
            String timeRangeDisplay = "";
            
            if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
                // 同一天内的时间段（如 09:00-17:00）
                timeValid = !currentTime.isBefore(earliestTime) && !currentTime.isAfter(latestTime);
                timeRangeDisplay = String.format("%s-%s", earliestTime.toString(), latestTime.toString());
                log.info("同日时间段签到验证: currentTime={}, earliestTime={}, latestTime={}, valid={}", 
                    currentTime, earliestTime, latestTime, timeValid);
            } else {
                // 跨天的时间段（如 23:00-01:00）
                // 当前时间在今天的晚些时候（>=earliestTime）或明天的早些时候（<=latestTime）
                timeValid = !currentTime.isBefore(earliestTime) || !currentTime.isAfter(latestTime);
                
                // 对于跨天时间段，显示更清晰的时间范围
                if (earliestTime.isAfter(LocalTime.of(12, 0))) {
                    // 如果开始时间在下午，显示为 "今天HH:mm-明天HH:mm"
                    timeRangeDisplay = String.format("今天%s-明天%s", earliestTime.toString(), latestTime.toString());
                } else {
                    // 否则正常显示
                    timeRangeDisplay = String.format("%s-%s", earliestTime.toString(), latestTime.toString());
                }
                log.info("跨日时间段签到验证: currentTime={}, earliestTime={}, latestTime={}, valid={}", 
                    currentTime, earliestTime, latestTime, timeValid);
            }
            
            if (!timeValid) {
                log.warn("❌ 签到时间不在允许范围内: configId={}, currentTime={}, allowedRange={}", 
                    configuration.getId(), currentTime, timeRangeDisplay);
                throw new RuntimeException(String.format("当前时间不在允许的打卡时间段内。允许打卡时间：%s", timeRangeDisplay));
            }
            
            log.info("✅ 签到时间验证通过: configId={}, currentTime={}", configuration.getId(), currentTime);
            
            // GPS定位功能已移除，不再进行位置验证
            String checkinMethod = request.getCheckinMethod() != null ? request.getCheckinMethod() : "QR_CODE";
            log.info("签到方式: {}", checkinMethod);

            // 验证签到方式
            validateCheckinMethod(request, configuration);
            
            // 检查今日是否已签到
            Optional<CheckinRecord> existingRecord = checkinRecordRepository
                .findByUserAndConfigurationAndDate(user, configuration, today);
            
            if (existingRecord.isPresent()
                    && existingRecord.get().getStatus() != CheckinRecord.CheckinStatus.LEAVE) {
                throw new RuntimeException("今日已签到，请勿重复签到");
            }

            claimDeviceSlot(request, configuration, user, today);
            
            // 创建签到记录；如果原记录是请假占位，允许用户实际到场后覆盖为签到记录
            CheckinRecord record = existingRecord.orElseGet(CheckinRecord::new);
            record.setUser(user);
            record.setConfiguration(configuration);
            record.setCheckinTime(LocalDateTime.now());
            record.setCheckinLatitude(request.getLatitude());
            record.setCheckinLongitude(request.getLongitude());
            record.setCheckinAddress(request.getAddress());
            record.setCheckinMethod(request.getCheckinMethod());
            // 将设备信息对象转换为JSON字符串存储
            if (request.getDeviceInfo() != null) {
                try {
                    String deviceInfoJson = objectMapper.writeValueAsString(request.getDeviceInfo());
                    record.setDeviceInfo(deviceInfoJson);
                } catch (Exception e) {
                    log.warn("设备信息序列化失败: {}", e.getMessage());
                    record.setDeviceInfo("设备信息序列化失败");
                }
            }
            record.setNotes(request.getNotes());
            record.setIpAddress(request.getIpAddress());
            record.setUserAgent(request.getUserAgent());
            
            // 根据签到方式设置审核状态和签到状态
            if ("MANUAL_AUDIT".equals(checkinMethod)) {
                // 管理员审核方式：设置为待审核状态
                record.setAuditStatus(CheckinRecord.AuditStatus.PENDING);
                record.setStatus(CheckinRecord.CheckinStatus.NORMAL); // 初始设为正常，审核时可修改
                record.setIsLate(false);
                record.setLateMinutes(0);
                log.info("管理员审核方式签到，记录状态设为待审核");
            } else {
                // GPS、二维码等自动验证方式：无需审核
                record.setAuditStatus(CheckinRecord.AuditStatus.NOT_REQUIRED);
                
                // 检查是否迟到：需求调整为"在配置的时间段内（开始-结束）不显示迟到"
                // 仅当签到时间晚于"结束时间"才判定迟到（跨日场景按结束日期+1处理）
                LocalDateTime sessionEnd = LocalDateTime.of(
                    (configuration.getStartTime().isAfter(configuration.getEndTime()) ? today.plusDays(1) : today),
                    configuration.getEndTime()
                );
                if (now.isAfter(sessionEnd)) {
                    record.setIsLate(true);
                    record.setLateMinutes((int) ChronoUnit.MINUTES.between(sessionEnd, now));
                    record.setStatus(CheckinRecord.CheckinStatus.LATE);
                } else {
                    record.setIsLate(false);
                    record.setLateMinutes(0);
                    record.setStatus(CheckinRecord.CheckinStatus.NORMAL);
                }
            }
            
            record = checkinRecordRepository.save(record);
            
            // 构建响应
            CheckinResponse response = new CheckinResponse();
            response.setId(record.getId());
            response.setTime(record.getCheckinTime());
            response.setLocationName(configuration.getLocationName());
            response.setSessionName(configuration.getSessionName());
            response.setStatus(record.getStatus().name());
            response.setIsLate(record.getIsLate());
            response.setLateMinutes(record.getLateMinutes());
            response.setAuditStatus(record.getAuditStatus().name());
            
            return response;
        } catch (Exception e) {
            log.error("签到失败: {}", e.getMessage(), e);
            throw new RuntimeException("签到失败: " + e.getMessage());
        }
    }

    private void claimDeviceSlot(CheckinRequest request, CheckinConfiguration configuration, User user, LocalDate date) {
        if (request.getDeviceInfo() == null || request.getDeviceInfo().getDeviceFingerprint() == null
                || request.getDeviceInfo().getDeviceFingerprint().isBlank()) {
            throw new RuntimeException("签到需要提供设备指纹");
        }
        String fingerprintHash = sha256(request.getDeviceInfo().getDeviceFingerprint());
        Optional<CheckinDeviceUsage> existing = checkinDeviceUsageRepository
                .findByConfigurationIdAndUsageDateAndDeviceFingerprintHashAndDeletedFalse(
                        configuration.getId(), date, fingerprintHash);
        if (existing.isPresent() && !existing.get().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("该设备已由其他账号在当前打卡时段使用");
        }
        if (existing.isEmpty()) {
            CheckinDeviceUsage usage = new CheckinDeviceUsage();
            usage.setConfiguration(configuration);
            usage.setUser(user);
            usage.setUsageDate(date);
            usage.setDeviceFingerprintHash(fingerprintHash);
            try {
                checkinDeviceUsageRepository.saveAndFlush(usage);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                throw new RuntimeException("该设备已由其他账号在当前打卡时段使用");
            }
        }
    }

    private String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("无法处理设备指纹", e);
        }
    }
    
    @Override
    public CheckinResponse checkout(CheckoutRequest request, Long userId) {
        // TODO: 实现签退逻辑
        throw new RuntimeException("签退功能开发中");
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTodayCheckinStatus(Long userId) {
        Map<String, Object> status = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        List<CheckinRecord> todayRecords = checkinRecordRepository.findByUser_IdAndCheckinDate(userId, today);
        List<CheckinConfiguration> authorizedConfigurations = getUserAuthorizedConfigurations(userId);
        Set<Long> leaveConfigurationIds = todayRecords.stream()
            .filter(record -> record.getStatus() == CheckinRecord.CheckinStatus.LEAVE)
            .map(record -> record.getConfiguration() != null ? record.getConfiguration().getId() : null)
            .filter(Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());

        Optional<CheckinRecord> recordOpt = todayRecords.stream()
            .filter(record -> record.getStatus() != CheckinRecord.CheckinStatus.LEAVE)
            .findFirst();
        if (recordOpt.isEmpty() && !todayRecords.isEmpty() && !authorizedConfigurations.isEmpty()
                && authorizedConfigurations.stream().allMatch(config -> leaveConfigurationIds.contains(config.getId()))) {
            recordOpt = todayRecords.stream()
                .filter(record -> record.getStatus() == CheckinRecord.CheckinStatus.LEAVE)
                .findFirst();
        }
        
        if (recordOpt.isPresent()) {
            CheckinRecord record = recordOpt.get();
            
            // 手动初始化懒加载的关联实体（在事务内）
            if (record.getConfiguration() != null) {
                record.getConfiguration().getLocationName(); // 触发初始化
            }
            
            status.put("hasCheckedIn", true);
            status.put("hasCheckedOut", record.getCheckoutTime() != null);
            status.put("checkinTime", record.getCheckinTime());
            status.put("checkoutTime", record.getCheckoutTime());
            status.put("locationName", record.getConfiguration() != null ? record.getConfiguration().getLocationName() : null);
            status.put("sessionName", record.getConfiguration() != null ? record.getConfiguration().getSessionName() : null);
            status.put("status", record.getStatus().name());
            status.put("isLate", record.getIsLate());
            status.put("lateMinutes", record.getLateMinutes());
            status.put("durationMinutes", record.getDurationMinutes());
        } else {
            status.put("hasCheckedIn", false);
            status.put("hasCheckedOut", false);
        }
        
        return status;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CheckinRecord> getUserCheckinRecords(Long userId, Pageable pageable) {
        try {
            // 直接使用JOIN FETCH查询所有数据，避免懒加载问题
            List<CheckinRecord> allRecords = checkinRecordRepository.findByUserIdAndDeletedFalseWithFetch(userId);
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allRecords.size());
            
            // 确保start和end在有效范围内
            if (start >= allRecords.size()) {
                return new PageImpl<>(new ArrayList<>(), pageable, allRecords.size());
            }
            
            List<CheckinRecord> pageContent = allRecords.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, allRecords.size());
        } catch (Exception e) {
            log.error("获取用户打卡记录失败: userId={}, pageable={}", userId, pageable, e);
            // 返回空的分页结果而不是抛出异常
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CheckinRecord> getRecentCheckinRecords(Long userId, int size) {
        List<CheckinRecord> allRecords = checkinRecordRepository.findByUserIdAndDeletedFalseWithFetch(userId);
        
        // 手动限制数量
        return allRecords.size() > size ? allRecords.subList(0, size) : allRecords;
    }
    
    @Override
    public List<CheckinRecord> getUserCheckinRecords(Long userId, LocalDate startDate, LocalDate endDate) {
        return checkinRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CheckinRecord> getAllCheckinRecords(String keyword, CheckinRecord.CheckinStatus status, 
                                                   LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            log.info("管理员查询所有打卡记录: keyword={}, status={}, startDate={}, endDate={}, pageable={}", 
                keyword, status, startDate, endDate, pageable);
            
            // 获取所有记录（带预加载）
            List<CheckinRecord> allRecords = checkinRecordRepository.findAllWithFetch();
            
            // 应用过滤条件
            List<CheckinRecord> filteredRecords = allRecords.stream()
                .filter(record -> {
                    // 关键词过滤
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String lowerKeyword = keyword.toLowerCase();
                        boolean matchKeyword = (record.getUser() != null && 
                            record.getUser().getRealName() != null && 
                            record.getUser().getRealName().toLowerCase().contains(lowerKeyword)) ||
                            (record.getConfiguration() != null && 
                            record.getConfiguration().getName() != null &&
                            record.getConfiguration().getName().toLowerCase().contains(lowerKeyword));
                        if (!matchKeyword) return false;
                    }
                    
                    // 状态过滤
                    if (status != null && !status.equals(record.getStatus())) {
                        return false;
                    }
                    
                    // 日期范围过滤
                    if (startDate != null && record.getCheckinTime().toLocalDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && record.getCheckinTime().toLocalDate().isAfter(endDate)) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 手动分页
            int total = filteredRecords.size();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), total);
            
            List<CheckinRecord> pageContent = start < total ? 
                filteredRecords.subList(start, end) : new ArrayList<>();
            
            log.info("管理员查询结果: 总记录数={}, 过滤后记录数={}, 当前页记录数={}", 
                allRecords.size(), total, pageContent.size());
            
            return new PageImpl<>(pageContent, pageable, total);
            
        } catch (Exception e) {
            log.error("管理员查询所有打卡记录失败", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
    
    @Override
    public CheckinRecord getCheckinRecordById(Long id) {
        return checkinRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("打卡记录不存在"));
    }
    
    @Override
    public CheckinRecord makeupCheckin(Long recordId, String notes, Long adminId) {
        // TODO: 实现补签逻辑
        throw new RuntimeException("补签功能开发中");
    }
    
    @Override
    public void deleteCheckinRecord(Long id) {
        CheckinRecord record = getCheckinRecordById(id);
        record.setDeleted(true);
        record.setUpdatedAt(LocalDateTime.now());
        checkinRecordRepository.save(record);
    }
    
    @Override
    public boolean validateConfiguration(Long configurationId, Double latitude, Double longitude, Long userId, String checkinMethod) {
        try {
            // 预加载考勤人员列表以便进行权限验证
            CheckinConfiguration configuration = checkinConfigurationRepository.findByIdWithUsers(configurationId)
                .orElseThrow(() -> new RuntimeException("打卡配置不存在"));
            
            if (!configuration.getIsActive()) {
                log.info("配置未启用: configId={}", configurationId);
                return false;
            }
            
            // 验证用户权限：检查当前用户是否在考勤人员列表中
            boolean isAuthorizedUser = configuration.getRequiredUsers().stream()
                .anyMatch(requiredUser -> requiredUser.getId().equals(userId));
            
            if (!isAuthorizedUser) {
                log.info("用户无权限进行此打卡配置的签到: userId={}, configId={}", userId, configurationId);
                return false;
            }
            
            // 验证时间是否在允许的打卡时间段内
            // 注意：只比较小时和分钟，忽略秒和纳秒，避免边界问题
            LocalTime currentTime = LocalTime.now().withSecond(0).withNano(0);
            LocalTime startTime = configuration.getStartTime().withSecond(0).withNano(0);
            LocalTime endTime = configuration.getEndTime().withSecond(0).withNano(0);
            
            // 获取提前和延迟分钟数，默认为0
            int earlyMinutes = configuration.getEarlyCheckinMinutes() != null ? configuration.getEarlyCheckinMinutes() : 0;
            int lateMinutes = configuration.getLateCheckinMinutes() != null ? configuration.getLateCheckinMinutes() : 0;
            
            // 计算允许的打卡时间范围
            LocalTime earliestTime = startTime.minusMinutes(earlyMinutes);
            LocalTime latestTime = endTime.plusMinutes(lateMinutes);
            
            log.info("🕐 时间验证: configId={}, currentTime={}, earliestTime={}, latestTime={}, earlyMinutes={}, lateMinutes={}", 
                configurationId, currentTime, earliestTime, latestTime, earlyMinutes, lateMinutes);
            
            // 检查当前时间是否在允许范围内
            boolean timeValid = false;
            if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
                // 同一天内的时间段（如 09:00-17:00）
                // 使用 >= earliestTime 并且 <= latestTime
                timeValid = !currentTime.isBefore(earliestTime) && !currentTime.isAfter(latestTime);
                log.info("同日时间段验证: currentTime={}, earliestTime={}, latestTime={}, valid={}", 
                    currentTime, earliestTime, latestTime, timeValid);
            } else {
                // 跨天的时间段（如 23:00-01:00）
                // 当前时间在今天的晚些时候（>=earliestTime）或明天的早些时候（<=latestTime）
                timeValid = !currentTime.isBefore(earliestTime) || !currentTime.isAfter(latestTime);
                log.info("跨日时间段验证: currentTime={}, earliestTime={}, latestTime={}, valid={}", 
                    currentTime, earliestTime, latestTime, timeValid);
            }
            
            if (!timeValid) {
                log.warn("❌ 当前时间不在允许范围内: configId={}, currentTime={}, allowedRange={}-{}", 
                    configurationId, currentTime, earliestTime, latestTime);
                return false;
            }
            
            log.info("✅ 时间验证通过: configId={}, currentTime={}", configurationId, currentTime);
            
            // GPS定位功能已移除，所有签到方式不再进行位置校验
            log.info("配置验证通过（GPS定位功能已取消）: configId={}, checkinMethod={}", configurationId, checkinMethod);
            return true;
        } catch (Exception e) {
            log.error("验证打卡配置失败", e);
            return false;
        }
    }
    
    @Override
    public String uploadCheckinPhoto(MultipartFile file, Long recordId) {
        // TODO: 实现照片上传逻辑
        throw new RuntimeException("照片上传功能开发中");
    }
    
    @Override
    public Map<String, Object> getCheckinStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 如果没有提供日期范围，默认使用最近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // 总打卡次数
        long totalCheckins = checkinRecordRepository.countByUserAndDateRange(
            userRepository.findById(userId).orElseThrow(), 
            startDate.atStartOfDay(), 
            endDate.plusDays(1).atStartOfDay()
        );
        statistics.put("totalCheckins", totalCheckins);
        
        // 迟到次数
        long lateCheckins = checkinRecordRepository.countLateRecordsByUser(
            userRepository.findById(userId).orElseThrow()
        );
        statistics.put("lateCheckins", lateCheckins);
        
        // 添加日期范围信息
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        
        return statistics;
    }
    
    @Override
    public Map<String, Object> getGlobalCheckinStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总打卡次数
        long totalCheckins = checkinRecordRepository.countByDeletedFalse();
        statistics.put("totalCheckins", totalCheckins);
        
        // 总用户数
        long totalUsers = userRepository.countByDeletedFalse();
        statistics.put("totalUsers", totalUsers);
        
        return statistics;
    }
    
    @Override
    public byte[] exportCheckinRecords(String keyword, CheckinRecord.CheckinStatus status, 
                                     LocalDate startDate, LocalDate endDate) {
        // TODO: 实现导出功能
        throw new RuntimeException("导出功能开发中");
    }
    
    @Override
    public void processExpiredRecords() {
        // TODO: 实现过期记录处理逻辑
        log.info("处理过期打卡记录...");
    }
    
    @Override
    public List<Map<String, Object>> getCheckinRanking(LocalDate startDate, LocalDate endDate, int limit) {
        // TODO: 实现排行榜逻辑
        return new ArrayList<>();
    }
    
    /**
     * 验证签到方式
     */
    private void validateCheckinMethod(CheckinRequest request, CheckinConfiguration configuration) {
        String method = request.getCheckinMethod();
        if (method == null) {
            method = "QR_CODE"; // 默认二维码
        }
        
        switch (method) {
            case "QR_CODE":
                validateQRCodeCheckin(request, configuration);
                break;
                
            case "MANUAL_AUDIT":
                // 管理员审核方式：无需验证，直接提交等待审核
                log.info("管理员审核方式签到，无需前置验证，待审核");
                break;
                
            default:
                throw new RuntimeException("不支持的签到方式: " + method);
        }
    }
    
    /**
     * 验证二维码签到（动态二维码模式）
     * 
     * 验证逻辑：
     * 1. 验证二维码内容不为空
     * 2. 解析JSON格式的二维码内容
     * 3. 验证配置ID是否匹配
     * 4. 验证二维码类型是否正确
     * 5. ⭐ 关键：验证二维码是否为最新版本（与配置中存储的二维码对比）
     * 6. 验证自定义有效期（如果设置）
     * 
     * 动态二维码机制：
     * - 管理员端每60秒自动刷新二维码，并保存到配置中
     * - 学生扫描时，后端对比扫描的二维码与配置中存储的是否一致
     * - 只有最新生成的二维码才能通过验证
     * - 旧的二维码在新二维码生成后立即失效
     */
    private void validateQRCodeCheckin(CheckinRequest request, CheckinConfiguration configuration) {
        String qrCode = request.getQrCode();
        if (qrCode == null || qrCode.trim().isEmpty()) {
            throw new RuntimeException("二维码签到需要提供二维码内容");
        }
        
        log.info("开始验证动态二维码签到: configId={}", configuration.getId());
        
        try {
            // 解析二维码JSON内容
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode qrData = objectMapper.readTree(qrCode);
            
            // 1. 验证二维码类型
            String type = qrData.has("type") ? qrData.get("type").asText() : "";
            if (!"CHECKIN".equals(type)) {
                throw new RuntimeException("二维码类型错误，这不是一个有效的签到二维码");
            }
            
            // 2. 验证配置ID是否匹配
            Long qrConfigId = qrData.has("configId") ? qrData.get("configId").asLong() : null;
            if (qrConfigId == null || !qrConfigId.equals(configuration.getId())) {
                throw new RuntimeException("二维码与当前签到配置不匹配，请扫描正确的签到二维码");
            }
            
            // ⭐ 3. 关键验证：检查是否为最新的二维码（动态二维码核心逻辑）
            String storedQRCode = configuration.getQrCode();
            if (storedQRCode == null || storedQRCode.trim().isEmpty()) {
                log.warn("配置中未存储二维码，跳过动态验证");
            } else {
                // 对比扫描的二维码与配置中存储的最新二维码
                if (!qrCode.equals(storedQRCode)) {
                    log.warn("二维码已失效，不是最新版本。配置ID: {}", configuration.getId());
                    
                    // 尝试解析存储的二维码，获取最新生成时间
                    try {
                        com.fasterxml.jackson.databind.JsonNode storedQRData = objectMapper.readTree(storedQRCode);
                        String latestGenerateTime = storedQRData.has("generateTime") 
                            ? storedQRData.get("generateTime").asText() : "未知";
                        
                        throw new RuntimeException(
                            "二维码已失效，请扫描最新的二维码。" +
                            "当前二维码已被更新的版本替代（最新生成时间: " + latestGenerateTime + "）"
                        );
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        throw new RuntimeException("二维码已失效，请扫描现场张贴的最新二维码");
                    }
                }
                log.info("✅ 动态二维码验证通过：扫描的二维码与配置中存储的最新版本一致");
            }
            
            // 4. 验证生成时间（辅助检查，避免时间倒退）
            String generateTimeStr = qrData.has("generateTime") ? qrData.get("generateTime").asText() : null;
            if (generateTimeStr != null && !generateTimeStr.isEmpty()) {
                try {
                    LocalDateTime generateTime = LocalDateTime.parse(generateTimeStr, 
                        java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime now = LocalDateTime.now();
                    
                    // 检查时间是否异常（未来时间）
                    if (generateTime.isAfter(now.plusMinutes(5))) {
                        throw new RuntimeException("二维码时间异常（生成时间在未来），请确保设备时间正确");
                    }
                    
                    long minutesDiff = java.time.Duration.between(generateTime, now).toMinutes();
                    log.info("二维码生成于 {} 分钟前", minutesDiff);
                    
                    // 警告：如果二维码时间过久，可能是缓存或网络问题
                    if (minutesDiff > 10) {
                        log.warn("二维码生成时间较久（{}分钟前），可能不是最新版本", minutesDiff);
                    }
                    
                } catch (java.time.format.DateTimeParseException e) {
                    log.warn("二维码生成时间格式错误: {}", generateTimeStr);
                }
            }
            
            // 5. 验证自定义有效期（如果设置）
            String expireTime = qrData.has("expireTime") ? qrData.get("expireTime").asText() : null;
            if (expireTime != null && !expireTime.trim().isEmpty()) {
                try {
                    LocalDateTime expireDateTime = LocalDateTime.parse(expireTime,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if (LocalDateTime.now().isAfter(expireDateTime)) {
                        throw new RuntimeException("二维码已超过设定的有效期，请联系管理员");
                    }
                    log.info("二维码有效期验证通过: expireTime={}", expireTime);
                } catch (java.time.format.DateTimeParseException e) {
                    log.warn("二维码有效期格式错误: {}", expireTime);
                }
            }
            
            log.info("✅ 二维码签到验证通过: configId={}, type={}, 动态验证=通过", 
                configuration.getId(), type);
                
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("二维码内容解析失败，不是有效的JSON格式: {}", qrCode, e);
            // 兼容旧格式：如果不是JSON，则使用简单的字符串匹配
            if (!qrCode.contains(configuration.getId().toString())) {
                throw new RuntimeException("二维码内容格式错误或与配置不匹配");
            }
            log.info("使用兼容模式验证二维码（非JSON格式）");
        } catch (RuntimeException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("二维码验证过程发生异常", e);
            throw new RuntimeException("二维码验证失败：" + e.getMessage());
        }
    }
    
    // 注意：WiFi签到方法已被移除，因为已替换为"管理员审核"方式

    @Override
    @Transactional
    public boolean deleteCheckinRecord(Long recordId, Long userId, boolean isAdmin) {
        try {
            log.info("删除打卡记录: recordId={}, userId={}, isAdmin={}", recordId, userId, isAdmin);
            
            // 查找记录
            Optional<CheckinRecord> recordOptional = checkinRecordRepository.findById(recordId);
            if (!recordOptional.isPresent()) {
                log.warn("打卡记录不存在: recordId={}", recordId);
                return false;
            }
            
            CheckinRecord record = recordOptional.get();
            
            // 检查记录是否已被删除
            if (record.getDeleted()) {
                log.warn("打卡记录已被删除: recordId={}", recordId);
                return false;
            }
            
            // 检查权限：管理员可以删除任何记录，普通用户只能删除自己的记录
            if (!isAdmin && !record.getUser().getId().equals(userId)) {
                log.warn("无权限删除他人的打卡记录: recordId={}, recordUserId={}, currentUserId={}", 
                    recordId, record.getUser().getId(), userId);
                return false;
            }
            
            // 软删除记录
            record.setDeleted(true);
            record.setUpdatedAt(LocalDateTime.now());
            checkinRecordRepository.save(record);
            
            log.info("打卡记录删除成功: recordId={}, userId={}, isAdmin={}", recordId, userId, isAdmin);
            return true;
            
        } catch (Exception e) {
            log.error("删除打卡记录失败: recordId={}, userId={}, isAdmin={}", recordId, userId, isAdmin, e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CheckinConfiguration> getUserAuthorizedConfigurations(Long userId) {
        try {
            log.info("获取用户有权限的打卡配置: userId={}", userId);
            
            // 获取所有启用的配置（包含用户列表）
            List<CheckinConfiguration> allActiveConfigurations = checkinConfigurationRepository.findAllActiveWithUsers();
            
            LocalDate today = LocalDate.now();

            // 过滤出用户有权限且今天需要打卡的配置
            List<CheckinConfiguration> authorizedConfigurations = allActiveConfigurations.stream()
                .filter(config -> CheckinWeekdayUtils.isRequiredOnDate(config.getRequiredWeekdays(), today))
                .filter(config -> config.getRequiredUsers().stream()
                    .anyMatch(requiredUser -> requiredUser.getId().equals(userId)))
                .collect(java.util.stream.Collectors.toList());
            
            log.info("用户有权限的打卡配置数量: userId={}, count={}", userId, authorizedConfigurations.size());
            
            // 在事务内初始化所有需要的懒加载关联，避免 LazyInitializationException
            for (CheckinConfiguration config : authorizedConfigurations) {
                // 初始化 requiredUsers
                if (config.getRequiredUsers() != null) {
                    config.getRequiredUsers().size(); // 触发集合初始化
                    // 初始化每个用户的基本信息
                    for (User user : config.getRequiredUsers()) {
                        if (user != null) {
                            user.getUsername(); // 触发用户初始化
                            if (user.getDepartment() != null) {
                                user.getDepartment().getName(); // 触发部门初始化
                            }
                        }
                    }
                }
                // 初始化 createdBy
                if (config.getCreatedBy() != null) {
                    config.getCreatedBy().getUsername();
                }
            }
            
            return authorizedConfigurations;
            
        } catch (Exception e) {
            log.error("获取用户有权限的打卡配置失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional
    public CheckinRecord approveCheckin(Long recordId, Long adminId, String auditNotes) {
        try {
            log.info("管理员审核签到（通过）: recordId={}, adminId={}", recordId, adminId);
            
            // 查找记录
            CheckinRecord record = checkinRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("签到记录不存在"));
            
            // 验证审核状态
            if (record.getAuditStatus() != CheckinRecord.AuditStatus.PENDING) {
                throw new RuntimeException("该记录不是待审核状态，无法审核");
            }
            
            // 查找审核人
            User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("审核管理员不存在"));
            
            // 更新审核信息
            record.setAuditStatus(CheckinRecord.AuditStatus.APPROVED);
            record.setAuditedBy(admin);
            record.setAuditTime(LocalDateTime.now());
            record.setAuditNotes(auditNotes);
            record.setStatus(CheckinRecord.CheckinStatus.NORMAL); // 审核通过，标记为正常
            
            record = checkinRecordRepository.save(record);
            
            // 初始化懒加载的关联实体
            if (record.getUser() != null) {
                record.getUser().getRealName();
                if (record.getUser().getDepartment() != null) {
                    record.getUser().getDepartment().getName(); // 初始化Department
                }
            }
            if (record.getAuditedBy() != null) {
                record.getAuditedBy().getRealName();
                if (record.getAuditedBy().getDepartment() != null) {
                    record.getAuditedBy().getDepartment().getName(); // 初始化审核人的Department
                }
            }
            if (record.getConfiguration() != null) {
                record.getConfiguration().getName();
            }
            
            log.info("✅ 签到审核通过: recordId={}, userId={}, adminId={}", 
                recordId, record.getUser().getId(), adminId);
            
            return record;
            
        } catch (Exception e) {
            log.error("审核签到失败: recordId={}", recordId, e);
            throw new RuntimeException("审核签到失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CheckinRecord rejectCheckin(Long recordId, Long adminId, String auditNotes) {
        try {
            log.info("管理员审核签到（拒绝）: recordId={}, adminId={}", recordId, adminId);
            
            // 查找记录
            CheckinRecord record = checkinRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("签到记录不存在"));
            
            // 验证审核状态
            if (record.getAuditStatus() != CheckinRecord.AuditStatus.PENDING) {
                throw new RuntimeException("该记录不是待审核状态，无法审核");
            }
            
            // 查找审核人
            User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("审核管理员不存在"));
            
            // 更新审核信息
            record.setAuditStatus(CheckinRecord.AuditStatus.REJECTED);
            record.setAuditedBy(admin);
            record.setAuditTime(LocalDateTime.now());
            record.setAuditNotes(auditNotes);
            record.setStatus(CheckinRecord.CheckinStatus.ABSENT); // 审核拒绝，标记为缺勤
            
            record = checkinRecordRepository.save(record);
            
            // 初始化懒加载的关联实体
            if (record.getUser() != null) {
                record.getUser().getRealName();
                if (record.getUser().getDepartment() != null) {
                    record.getUser().getDepartment().getName(); // 初始化Department
                }
            }
            if (record.getAuditedBy() != null) {
                record.getAuditedBy().getRealName();
                if (record.getAuditedBy().getDepartment() != null) {
                    record.getAuditedBy().getDepartment().getName(); // 初始化审核人的Department
                }
            }
            if (record.getConfiguration() != null) {
                record.getConfiguration().getName();
            }
            
            log.info("❌ 签到审核拒绝（标记缺勤）: recordId={}, userId={}, adminId={}", 
                recordId, record.getUser().getId(), adminId);
            
            return record;
            
        } catch (Exception e) {
            log.error("拒绝签到审核失败: recordId={}", recordId, e);
            throw new RuntimeException("拒绝签到审核失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CheckinRecord> getPendingAuditRecords(Pageable pageable) {
        return getAuditRecordsByStatus(CheckinRecord.AuditStatus.PENDING, pageable);
    }
    
    @Override
    public long getPendingAuditCount() {
        try {
            long count = checkinRecordRepository.countPendingAuditRecords();
            log.info("待审核记录数量: {}", count);
            return count;
        } catch (Exception e) {
            log.error("获取待审核数量失败", e);
            return 0;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CheckinRecord> getAuditRecordsByStatus(CheckinRecord.AuditStatus auditStatus, Pageable pageable) {
        try {
            log.info("查询审核状态为 {} 的签到记录: page={}, size={}", 
                auditStatus, pageable.getPageNumber(), pageable.getPageSize());
            
            // 查询指定审核状态的记录
            Page<CheckinRecord> records = checkinRecordRepository.findByAuditStatus(
                auditStatus, 
                pageable
            );
            
            // 手动初始化懒加载的关联实体（解决 no Session 问题）
            records.getContent().forEach(record -> {
                if (record.getUser() != null) {
                    record.getUser().getRealName(); // 触发User加载
                    if (record.getUser().getDepartment() != null) {
                        record.getUser().getDepartment().getName(); // 触发Department加载
                    }
                }
                if (record.getConfiguration() != null) {
                    record.getConfiguration().getName(); // 触发Configuration加载
                }
                if (record.getAuditedBy() != null) {
                    record.getAuditedBy().getRealName(); // 触发auditedBy加载
                    if (record.getAuditedBy().getDepartment() != null) {
                        record.getAuditedBy().getDepartment().getName(); // 触发审核人Department加载
                    }
                }
            });
            
            log.info("审核状态为 {} 的记录数量: {}, 已初始化关联实体", auditStatus, records.getTotalElements());
            
            return records;
            
        } catch (Exception e) {
            log.error("查询审核状态为 {} 的记录失败", auditStatus, e);
            throw new RuntimeException("查询审核记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteCheckinRecordPhysically(Long recordId) {
        try {
            log.warn("⚠️ 开始物理删除签到记录: recordId={}", recordId);
            
            // 检查记录是否存在
            CheckinRecord record = checkinRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("签到记录不存在"));
            
            // 记录删除信息（用于日志）
            String userInfo = record.getUser() != null ? record.getUser().getRealName() : "未知用户";
            String configInfo = record.getConfiguration() != null ? record.getConfiguration().getName() : "未知配置";
            LocalDateTime checkinTime = record.getCheckinTime();
            
            // 执行物理删除（直接从数据库删除，释放空间）
            checkinRecordRepository.delete(record);
            
            log.warn("🗑️ 签到记录已物理删除: recordId={}, user={}, config={}, time={}", 
                recordId, userInfo, configInfo, checkinTime);
            
        } catch (Exception e) {
            log.error("物理删除签到记录失败: recordId={}", recordId, e);
            throw new RuntimeException("删除失败: " + e.getMessage());
        }
    }
    
    // calculateDistance方法已移除（GPS定位功能已取消）
}
