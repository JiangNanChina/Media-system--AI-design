package com.example.photography.service;

import com.example.photography.dto.request.CheckinConfigurationRequest;
import com.example.photography.dto.response.CheckinConfigurationResponse;
import com.example.photography.model.entity.CheckinConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 打卡配置服务接口
 */
public interface CheckinConfigurationService {
    
    /**
     * 创建打卡配置
     */
    CheckinConfigurationResponse createConfiguration(CheckinConfigurationRequest request);
    
    /**
     * 更新打卡配置
     */
    CheckinConfigurationResponse updateConfiguration(Long id, CheckinConfigurationRequest request);
    
    /**
     * 删除打卡配置
     */
    void deleteConfiguration(Long id);
    
    /**
     * 强制删除打卡配置（同时删除关联记录）
     */
    void forceDeleteConfiguration(Long id);
    
    /**
     * 切换配置状态
     */
    CheckinConfigurationResponse toggleConfigurationStatus(Long id);
    
    /**
     * 获取配置详情
     */
    CheckinConfigurationResponse getConfigurationById(Long id);
    
    /**
     * 获取所有配置（分页）
     */
    Page<CheckinConfigurationResponse> getAllConfigurations(Pageable pageable);
    
    /**
     * 搜索配置
     */
    Page<CheckinConfigurationResponse> searchConfigurations(String keyword, Pageable pageable);
    
    /**
     * 获取所有启用的配置
     */
    List<CheckinConfigurationResponse> getActiveConfigurations();
    
    /**
     * 根据地点名称获取配置
     */
    List<CheckinConfigurationResponse> getConfigurationsByLocationName(String locationName);
    
    /**
     * 根据时段名称获取配置
     */
    List<CheckinConfigurationResponse> getConfigurationsBySessionName(String sessionName);
    
    /**
     * 批量更新配置排序
     */
    void updateConfigurationOrder(List<Long> configurationIds);
    
    /**
     * 获取配置统计信息
     */
    Object getConfigurationStatistics();
    
    /**
     * 检查地点和时段组合是否已存在
     */
    boolean isLocationSessionCombinationExists(String locationName, String sessionName, Long excludeId);
}
