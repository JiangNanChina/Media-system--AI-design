package com.example.photography.service;

import com.example.photography.config.FileUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件验证服务
 * 用于检查和清理无效的文件引用
 */
@Service
public class FileValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileValidationService.class);
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    /**
     * 验证文件是否存在
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    public boolean validateFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 处理相对路径
            String actualPath = filePath.startsWith("/uploads/") ? 
                filePath.substring("/uploads/".length()) : filePath;
            
            Path fullPath = Paths.get(fileUploadConfig.getUploadPath()).resolve(actualPath);
            boolean exists = Files.exists(fullPath) && Files.isReadable(fullPath);
            
            if (!exists) {
                logger.warn("文件不存在或不可读: {}", fullPath.toString());
            }
            
            return exists;
        } catch (Exception e) {
            logger.error("验证文件时发生错误: " + filePath, e);
            return false;
        }
    }
    
    /**
     * 验证文件列表，返回有效的文件路径
     * @param filePaths 文件路径列表
     * @return 有效的文件路径列表
     */
    public List<String> validateFiles(List<String> filePaths) {
        List<String> validFiles = new ArrayList<>();
        
        if (filePaths != null) {
            for (String filePath : filePaths) {
                if (validateFile(filePath)) {
                    validFiles.add(filePath);
                } else {
                    logger.info("移除无效文件引用: {}", filePath);
                }
            }
        }
        
        return validFiles;
    }
    
    /**
     * 清理目录中的孤立文件（文件存在但数据库中没有引用）
     * @param directory 目录路径
     * @return 清理的文件数量
     */
    public int cleanOrphanFiles(String directory) {
        int cleanedCount = 0;
        try {
            Path dirPath = Paths.get(directory);
            if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                // 这里可以实现更复杂的清理逻辑
                logger.info("开始清理目录中的孤立文件: {}", directory);
                // 实际的清理逻辑需要根据具体的业务需求来实现
            }
        } catch (Exception e) {
            logger.error("清理孤立文件时发生错误", e);
        }
        return cleanedCount;
    }
    
    /**
     * 获取默认占位符图片路径
     * @return 默认图片路径
     */
    public String getDefaultImagePath() {
        return "/uploads/default/no-image.jpg";
    }
}
