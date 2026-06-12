package com.example.photography.service.impl;

import com.example.photography.dto.request.CheckinConfigurationRequest;
import com.example.photography.dto.response.CheckinConfigurationResponse;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.model.entity.User;
import com.example.photography.repository.CheckinConfigurationRepository;
import com.example.photography.repository.CheckinRecordRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.CheckinConfigurationService;
import com.example.photography.utils.CheckinWeekdayUtils;
import com.example.photography.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 打卡配置服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheckinConfigurationServiceImpl implements CheckinConfigurationService {
    
    private final CheckinConfigurationRepository configurationRepository;
    private final CheckinRecordRepository checkinRecordRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public CheckinConfigurationResponse createConfiguration(CheckinConfigurationRequest request) {
        log.info("创建打卡配置: {}", request.getName());
        
        // 验证地点和时段组合是否已存在
        if (isLocationSessionCombinationExists(request.getLocationName(), request.getSessionName(), null)) {
            throw new RuntimeException("该地点和时段的配置组合已存在");
        }
        
        // 检查名称是否已存在
        if (configurationRepository.existsByNameAndNotDeleted(request.getName(), null)) {
            throw new RuntimeException("配置名称已存在");
        }
        
        // 创建配置
        CheckinConfiguration configuration = new CheckinConfiguration();
        configuration.setName(request.getName());
        configuration.setDescription(request.getDescription());
        
        // 地点信息
        configuration.setLocationName(request.getLocationName());
        configuration.setLocationAddress(request.getLocationAddress());
        configuration.setLocationDescription(request.getLocationDescription());
        configuration.setLongitude(request.getLongitude());
        configuration.setLatitude(request.getLatitude());
        
        // 时间信息
        configuration.setSessionName(request.getSessionName());
        configuration.setStartTime(request.getStartTime());
        configuration.setEndTime(request.getEndTime());
        configuration.setSessionDescription(request.getSessionDescription());
        configuration.setRequiredWeekdays(CheckinWeekdayUtils.serializeRequiredWeekdays(request.getRequiredWeekdays()));
        
        // 配置选项
        configuration.setIsActive(request.getIsActive());
        configuration.setSortOrder(request.getSortOrder());
        configuration.setEarlyCheckinMinutes(request.getEarlyCheckinMinutes());
        configuration.setLateCheckinMinutes(request.getLateCheckinMinutes());
        configuration.setQrCode(request.getQrCode());
        configuration.setWifiSsid(request.getWifiSsid());
        
        // 设置创建者
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            configuration.setCreatedBy(currentUser);
        }
        
        // 设置需要打卡的用户
        if (request.getRequiredUserIds() != null && !request.getRequiredUserIds().isEmpty()) {
            List<User> requiredUsers = userRepository.findAllById(request.getRequiredUserIds());
            configuration.getRequiredUsers().addAll(requiredUsers);
            log.info("设置需要打卡的用户数量: {}", requiredUsers.size());
        }
        
        configuration = configurationRepository.save(configuration);
        
        log.info("打卡配置创建成功: {}", configuration.getId());
        return CheckinConfigurationResponse.fromEntity(configuration);
    }
    
    @Override
    @Transactional
    public CheckinConfigurationResponse updateConfiguration(Long id, CheckinConfigurationRequest request) {
        log.info("更新打卡配置: {}", id);
        
        CheckinConfiguration configuration = configurationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        if (configuration.getDeleted()) {
            throw new RuntimeException("无法更新已删除的配置");
        }
        
        // 验证地点和时段组合是否已存在（排除当前配置）
        if (isLocationSessionCombinationExists(request.getLocationName(), request.getSessionName(), id)) {
            throw new RuntimeException("该地点和时段的配置组合已存在");
        }
        
        // 检查名称是否已存在（排除当前配置）
        if (configurationRepository.existsByNameAndNotDeleted(request.getName(), id)) {
            throw new RuntimeException("配置名称已存在");
        }
        
        // 更新配置
        configuration.setName(request.getName());
        configuration.setDescription(request.getDescription());
        
        // 地点信息
        configuration.setLocationName(request.getLocationName());
        configuration.setLocationAddress(request.getLocationAddress());
        configuration.setLocationDescription(request.getLocationDescription());
        configuration.setLongitude(request.getLongitude());
        configuration.setLatitude(request.getLatitude());
        
        // 时间信息
        configuration.setSessionName(request.getSessionName());
        configuration.setStartTime(request.getStartTime());
        configuration.setEndTime(request.getEndTime());
        configuration.setSessionDescription(request.getSessionDescription());
        configuration.setRequiredWeekdays(CheckinWeekdayUtils.serializeRequiredWeekdays(request.getRequiredWeekdays()));
        
        // 配置选项
        configuration.setIsActive(request.getIsActive());
        configuration.setSortOrder(request.getSortOrder());
        configuration.setEarlyCheckinMinutes(request.getEarlyCheckinMinutes());
        configuration.setLateCheckinMinutes(request.getLateCheckinMinutes());
        configuration.setQrCode(request.getQrCode());
        configuration.setWifiSsid(request.getWifiSsid());
        configuration.setUpdatedAt(LocalDateTime.now());
        
        // 更新需要打卡的用户
        configuration.getRequiredUsers().clear(); // 先清空现有用户
        if (request.getRequiredUserIds() != null && !request.getRequiredUserIds().isEmpty()) {
            List<User> requiredUsers = userRepository.findAllById(request.getRequiredUserIds());
            configuration.getRequiredUsers().addAll(requiredUsers);
            log.info("更新需要打卡的用户数量: {}", requiredUsers.size());
        }
        
        configuration = configurationRepository.save(configuration);
        
        log.info("打卡配置更新成功: {}", configuration.getId());
        return CheckinConfigurationResponse.fromEntity(configuration);
    }
    
    @Override
    @Transactional
    public void deleteConfiguration(Long id) {
        log.info("删除打卡配置: {}", id);
        
        CheckinConfiguration configuration = configurationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        if (configuration.getDeleted()) {
            throw new RuntimeException("配置已被删除");
        }
        
        // 检查是否有关联的打卡记录
        long recordCount = checkinRecordRepository.countByConfigurationId(id);
        if (recordCount > 0) {
            throw new RuntimeException(String.format("无法删除配置，存在 %d 条关联的打卡记录。如需强制删除，请使用强制删除功能。", recordCount));
        }
        
        // 物理删除配置
        configurationRepository.delete(configuration);
        
        log.info("打卡配置物理删除成功: {}", id);
    }
    
    @Override
    @Transactional
    public void forceDeleteConfiguration(Long id) {
        log.info("强制删除打卡配置: {}", id);
        
        CheckinConfiguration configuration = configurationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        if (configuration.getDeleted()) {
            throw new RuntimeException("配置已被删除");
        }
        
        // 统计关联记录数量
        long recordCount = checkinRecordRepository.countByConfigurationId(id);
        log.warn("强制删除配置，将同时删除 {} 条关联的打卡记录", recordCount);
        
        // 删除所有关联的打卡记录
        if (recordCount > 0) {
            checkinRecordRepository.deleteByConfigurationId(id);
            log.info("已删除 {} 条关联的打卡记录", recordCount);
        }
        
        // 删除配置
        configurationRepository.delete(configuration);
        
        log.info("打卡配置强制删除成功: {}，同时删除了 {} 条关联记录", id, recordCount);
    }
    
    @Override
    @Transactional
    public CheckinConfigurationResponse toggleConfigurationStatus(Long id) {
        log.info("切换打卡配置状态: {}", id);
        
        CheckinConfiguration configuration = configurationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        if (configuration.getDeleted()) {
            throw new RuntimeException("无法操作已删除的配置");
        }
        
        // 切换状态
        configuration.setIsActive(!configuration.getIsActive());
        configuration.setUpdatedAt(LocalDateTime.now());
        configuration = configurationRepository.save(configuration);
        
        log.info("打卡配置状态切换成功: {} -> {}", id, configuration.getIsActive());
        return CheckinConfigurationResponse.fromEntity(configuration);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CheckinConfigurationResponse getConfigurationById(Long id) {
        CheckinConfiguration configuration = configurationRepository.findByIdWithCreatedBy(id)
            .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        if (configuration.getDeleted()) {
            throw new RuntimeException("配置已被删除");
        }
        
        return CheckinConfigurationResponse.fromEntity(configuration);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CheckinConfigurationResponse> getAllConfigurations(Pageable pageable) {
        log.info("查询所有配置，分页参数: {}", pageable);
        Page<CheckinConfiguration> configurations = configurationRepository.findByDeletedFalse(pageable);
        log.info("查询到 {} 条配置记录", configurations.getTotalElements());
        return configurations.map(CheckinConfigurationResponse::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CheckinConfigurationResponse> searchConfigurations(String keyword, Pageable pageable) {
        Page<CheckinConfiguration> configurations = configurationRepository.findByKeyword(keyword, pageable);
        return configurations.map(CheckinConfigurationResponse::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CheckinConfigurationResponse> getActiveConfigurations() {
        List<CheckinConfiguration> configurations = configurationRepository.findByIsActiveTrueAndDeletedFalseOrderBySortOrderAsc();
        return configurations.stream()
            .map(CheckinConfigurationResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CheckinConfigurationResponse> getConfigurationsByLocationName(String locationName) {
        List<CheckinConfiguration> configurations = configurationRepository.findByLocationName(locationName);
        return configurations.stream()
            .map(CheckinConfigurationResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckinConfigurationResponse> getConfigurationsBySessionName(String sessionName) {
        List<CheckinConfiguration> configurations = configurationRepository.findBySessionName(sessionName);
        return configurations.stream()
            .map(CheckinConfigurationResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateConfigurationOrder(List<Long> configurationIds) {
        log.info("更新配置排序: {}", configurationIds);
        
        for (int i = 0; i < configurationIds.size(); i++) {
            Long configId = configurationIds.get(i);
            CheckinConfiguration configuration = configurationRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + configId));
            
            configuration.setSortOrder(i);
            configuration.setUpdatedAt(LocalDateTime.now());
            configurationRepository.save(configuration);
        }
        
        log.info("配置排序更新成功");
    }
    
    @Override
    public Object getConfigurationStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总配置数
        long totalConfigurations = configurationRepository.countByDeletedFalse();
        statistics.put("totalConfigurations", totalConfigurations);
        
        // 启用的配置数
        long activeConfigurations = configurationRepository.countByIsActiveTrueAndDeletedFalse();
        statistics.put("activeConfigurations", activeConfigurations);
        
        // 禁用的配置数
        statistics.put("inactiveConfigurations", totalConfigurations - activeConfigurations);
        
        return statistics;
    }
    
    @Override
    public boolean isLocationSessionCombinationExists(String locationName, String sessionName, Long excludeId) {
        return configurationRepository.findByLocationNameAndSessionName(locationName, sessionName)
            .filter(config -> !config.getDeleted())
            .filter(config -> excludeId == null || !config.getId().equals(excludeId))
            .isPresent();
    }
}
