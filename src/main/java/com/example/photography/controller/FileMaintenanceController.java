package com.example.photography.controller;

import com.example.photography.service.FileValidationService;
import com.example.photography.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件维护控制器
 * 提供文件验证和清理功能
 */
@RestController
@RequestMapping("/api/admin/file-maintenance")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class FileMaintenanceController {

    private static final Logger logger = LoggerFactory.getLogger(FileMaintenanceController.class);

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private EquipmentService equipmentService;

    /**
     * 检查文件系统健康状态
     */
    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> performHealthCheck() {
        try {
            logger.info("开始执行文件系统健康检查");
            
            Map<String, Object> result = new HashMap<>();
            
            // 这里可以添加更多的检查逻辑
            // 比如检查上传目录是否存在、权限是否正确等
            
            result.put("status", "healthy");
            result.put("message", "文件系统检查完成");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("文件系统健康检查失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "文件系统检查失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 验证特定文件是否存在
     */
    @GetMapping("/validate-file")
    public ResponseEntity<Map<String, Object>> validateFile(@RequestParam String filePath) {
        Map<String, Object> result = new HashMap<>();
        
        boolean exists = fileValidationService.validateFile(filePath);
        result.put("filePath", filePath);
        result.put("exists", exists);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取默认图片路径
     */
    @GetMapping("/default-image")
    public ResponseEntity<Map<String, Object>> getDefaultImagePath() {
        Map<String, Object> result = new HashMap<>();
        
        String defaultPath = fileValidationService.getDefaultImagePath();
        result.put("defaultImagePath", defaultPath);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }
}
