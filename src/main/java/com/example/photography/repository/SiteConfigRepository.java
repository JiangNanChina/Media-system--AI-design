package com.example.photography.repository;

import com.example.photography.model.entity.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 站点配置Repository
 */
@Repository
public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {
    
    /**
     * 根据配置键查找配置
     */
    Optional<SiteConfig> findByConfigKeyAndDeletedFalse(String configKey);
    
    /**
     * 查找所有启用的配置
     */
    @Query("SELECT s FROM SiteConfig s WHERE s.deleted = false AND s.enabled = true ORDER BY s.sortOrder ASC, s.configKey ASC")
    List<SiteConfig> findAllEnabledConfigs();
    
    /**
     * 根据配置类型查找配置
     */
    @Query("SELECT s FROM SiteConfig s WHERE s.deleted = false AND s.configType = :configType ORDER BY s.sortOrder ASC")
    List<SiteConfig> findByConfigType(@Param("configType") SiteConfig.ConfigType configType);
    
    /**
     * 查找所有配置（包括禁用的）
     */
    @Query("SELECT s FROM SiteConfig s WHERE s.deleted = false ORDER BY s.sortOrder ASC, s.configKey ASC")
    List<SiteConfig> findAllConfigs();
    
    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKeyAndDeletedFalse(String configKey);
    
    /**
     * 根据配置键列表批量查询
     */
    @Query("SELECT s FROM SiteConfig s WHERE s.deleted = false AND s.enabled = true AND s.configKey IN :configKeys")
    List<SiteConfig> findByConfigKeysAndEnabled(@Param("configKeys") List<String> configKeys);
}
