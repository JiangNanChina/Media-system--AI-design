package com.example.photography.config;

import com.example.photography.service.SiteConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时初始化默认数据
 */
@Component
@Slf4j
@Order(1) // 设置执行顺序，数字越小优先级越高
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private SiteConfigService siteConfigService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("========== 开始初始化应用数据 ==========");
        
        try {
            // 初始化站点配置
            log.info("初始化站点配置...");
            siteConfigService.initDefaultConfigs();
            log.info("站点配置初始化完成");
            
            log.info("========== 应用数据初始化完成 ==========");
        } catch (Exception e) {
            log.error("应用数据初始化失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}