package com.example.photography.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 * 提供通用的文件上传功能
 */
@Component
@Slf4j
public class FileUploadUtil {
    
    @Autowired
    private com.example.photography.config.FileUploadConfig fileUploadConfig;
    
    @Autowired
    private ImageOptimizer imageOptimizer;
    
    /**
     * 支持的图片格式
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp", "image/svg+xml"
    );
    
    /**
     * 支持的图片扩展名
     */
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    );
    
    /**
     * 默认最大文件大小（10MB）
     */
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 上传文件到指定目录
     * 
     * @param file 上传的文件
     * @param subDir 子目录名称（相对于uploads目录）
     * @return 上传成功后的文件访问路径
     */
    public String uploadFile(MultipartFile file, String subDir) {
        return uploadFile(file, subDir, DEFAULT_MAX_FILE_SIZE, true, false);
    }
    
    /**
     * 上传文件到指定目录（完整参数版本）
     * 
     * @param file 上传的文件
     * @param subDir 子目录名称（相对于uploads目录）
     * @param maxFileSize 最大文件大小（字节）
     * @param imageOnly 是否仅允许图片文件
     * @return 上传成功后的文件访问路径
     */
    public String uploadFile(MultipartFile file, String subDir, long maxFileSize, boolean imageOnly) {
        return uploadFile(file, subDir, maxFileSize, imageOnly, false);
    }
    
    /**
     * 上传文件到指定目录（完整参数版本，支持压缩）
     * 
     * @param file 上传的文件
     * @param subDir 子目录名称（相对于uploads目录）
     * @param maxFileSize 最大文件大小（字节）
     * @param imageOnly 是否仅允许图片文件
     * @param compressImage 是否压缩图片（仅对图片文件有效）
     * @return 上传成功后的文件访问路径
     */
    public String uploadFile(MultipartFile file, String subDir, long maxFileSize, boolean imageOnly, boolean compressImage) {
        // 验证文件
        validateFile(file, maxFileSize, imageOnly);
        
        // 生成唯一文件名
        String filename = generateUniqueFilename(file.getOriginalFilename());
        
        try {
            // 确保上传目录存在
            Path uploadDir = ensureUploadDirectory(subDir);
            Path filePath = uploadDir.resolve(filename);
            
            // 如果需要压缩图片且是图片文件
            if (compressImage && imageOnly && isImageFile(file)) {
                // 读取原始图片
                byte[] originalBytes = file.getBytes();
                long originalSize = originalBytes.length;
                
                log.info("开始压缩图片: {} (原始大小: {})", file.getOriginalFilename(), formatFileSize(originalSize));
                
                // 使用注入的ImageOptimizer压缩图片
                byte[] compressedBytes = imageOptimizer.compressImage(originalBytes);
                long compressedSize = compressedBytes.length;
                
                // 保存压缩后的图片
                Files.write(filePath, compressedBytes);
                
                double compressionRatio = (1 - (double) compressedSize / originalSize) * 100;
                log.info("图片压缩完成: {} -> {} (压缩率: {:.1f}%)", 
                    formatFileSize(originalSize), 
                    formatFileSize(compressedSize),
                    compressionRatio);
            } else {
                // 直接保存文件
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // 构建访问URL
            String fileUrl = buildFileUrl(subDir, filename);
            
            log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file, long maxFileSize, boolean imageOnly) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            long maxSizeMB = maxFileSize / (1024 * 1024);
            throw new RuntimeException("文件大小不能超过 " + maxSizeMB + "MB");
        }
        
        // 检查文件类型（如果需要）
        if (imageOnly) {
            validateImageFile(file);
        }
    }
    
    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        // 检查MIME类型
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("不支持的文件类型，仅支持图片文件");
        }
        
        // 检查文件扩展名
        if (originalFilename != null) {
            String extension = StringUtils.getFilenameExtension(originalFilename);
            if (extension == null || !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new RuntimeException("不支持的文件扩展名，仅支持: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
            }
        }
    }
    
    /**
     * 检查是否为图片文件
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }
    
    /**
     * 生成唯一文件名
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        
        if (extension != null && !extension.isEmpty()) {
            return uuid + "." + extension.toLowerCase();
        } else {
            return uuid;
        }
    }
    
    /**
     * 确保上传目录存在
     */
    private Path ensureUploadDirectory(String subDir) throws IOException {
        // 使用配置的上传路径，避免硬编码
        Path baseUploadDir = Paths.get(fileUploadConfig.getUploadPath());
        
        if (!Files.exists(baseUploadDir)) {
            Files.createDirectories(baseUploadDir);
            log.info("创建上传目录: {}", baseUploadDir.toAbsolutePath());
        }
        
        // 子目录
        Path uploadDir = baseUploadDir.resolve(subDir);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.info("创建子目录: {}", uploadDir.toAbsolutePath());
        }
        
        return uploadDir;
    }
    
    /**
     * 获取基础上传目录路径
     */
    private Path getBaseUploadDirectory() {
        // 使用配置的上传路径，避免硬编码
        return Paths.get(fileUploadConfig.getUploadPath());
    }
    
    /**
     * 构建文件访问URL
     */
    private String buildFileUrl(String subDir, String filename) {
        return "/uploads/" + subDir + "/" + filename;
    }
    
    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            // 移除URL前缀，获取实际文件路径
            String actualPath = filePath.startsWith("/uploads/") ? 
                filePath.substring("/uploads/".length()) : filePath;
            
            Path file = getBaseUploadDirectory().resolve(actualPath);
            if (Files.exists(file)) {
                Files.delete(file);
                log.info("文件删除成功: {}", filePath);
                return true;
            } else {
                log.warn("要删除的文件不存在: {}", filePath);
                return false;
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            String actualPath = filePath.startsWith("/uploads/") ? 
                filePath.substring("/uploads/".length()) : filePath;
            
            Path file = getBaseUploadDirectory().resolve(actualPath);
            return Files.exists(file);
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取文件大小（字节）
     */
    public long getFileSize(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return 0;
        }
        
        try {
            String actualPath = filePath.startsWith("/uploads/") ? 
                filePath.substring("/uploads/".length()) : filePath;
            
            Path file = getBaseUploadDirectory().resolve(actualPath);
            if (Files.exists(file)) {
                return Files.size(file);
            }
        } catch (IOException e) {
            log.error("获取文件大小失败: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * 格式化文件大小为可读字符串
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", 
            size / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }
}
