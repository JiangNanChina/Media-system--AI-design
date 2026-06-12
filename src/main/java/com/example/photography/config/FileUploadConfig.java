package com.example.photography.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * 文件上传配置类
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.upload.avatar-path}")
    private String avatarPath;
    
    @Value("${file.upload.equipment-path}")
    private String equipmentPath;
    
    @Value("${file.upload.return-path:}")
    private String returnPath;
    
    @PostConstruct
    public void init() {
        // 解析基础上传路径为绝对路径
        uploadPath = resolveAbsolutePath(uploadPath);
        
        // 子路径处理：如果是绝对路径直接用，否则拼接到 uploadPath
        avatarPath = resolveSubPath(avatarPath, uploadPath);
        equipmentPath = resolveSubPath(equipmentPath, uploadPath);
        
        // returnPath 可选；未配置时，默认放在 uploadPath/returns 下
        if (returnPath == null || returnPath.trim().isEmpty()) {
            returnPath = new File(uploadPath, "returns").getAbsolutePath();
        } else {
            returnPath = resolveSubPath(returnPath, uploadPath);
        }
        
        System.out.println("解析后的上传路径: " + uploadPath);
        System.out.println("解析后的头像路径: " + avatarPath);
        System.out.println("解析后的设备路径: " + equipmentPath);
        System.out.println("解析后的归还图片路径: " + returnPath);
        
        // 创建上传目录
        createDirectoryIfNotExists(uploadPath);
        createDirectoryIfNotExists(avatarPath);
        createDirectoryIfNotExists(equipmentPath);
        createDirectoryIfNotExists(returnPath);
    }
    
    private String resolveAbsolutePath(String path) {
        File file = new File(path);
        
        // 如果已经是绝对路径，直接返回（不再进行额外处理）
        if (file.isAbsolute()) {
            System.out.println("检测到绝对路径: " + path);
            return file.getAbsolutePath();
        }
        
        // 相对路径处理：根据操作系统区分
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows 开发环境：基于项目目录
            String projectRoot = System.getProperty("user.dir");
            String resolved = new File(projectRoot, path).getAbsolutePath();
            System.out.println("Windows环境，相对路径解析: " + path + " -> " + resolved);
            return resolved;
        } else {
            // Linux 生产环境：基于项目目录（不要硬编码固定路径）
            String projectRoot = System.getProperty("user.dir");
            String resolved = new File(projectRoot, path).getAbsolutePath();
            System.out.println("Linux环境，相对路径解析: " + path + " -> " + resolved);
            return resolved;
        }
    }
    
    /**
     * 解析子路径：如果已经是绝对路径则直接使用，否则拼接到基础路径
     */
    private String resolveSubPath(String subPath, String basePath) {
        if (subPath == null || subPath.trim().isEmpty()) {
            return basePath;
        }
        
        File file = new File(subPath);
        if (file.isAbsolute()) {
            // 已经是绝对路径，直接返回
            return file.getAbsolutePath();
        } else {
            // 相对路径，拼接到基础路径
            return new File(basePath, subPath).getAbsolutePath();
        }
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以分隔符结尾
        String uploadLocation = uploadPath.endsWith(File.separator) ? uploadPath : uploadPath + File.separator;
        
        System.out.println("静态资源映射路径: file:" + uploadLocation);
        
        // 配置静态资源访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadLocation)
                .setCachePeriod(3600)  // 设置缓存时间1小时
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(String resourcePath, org.springframework.core.io.Resource location) throws java.io.IOException {
                        org.springframework.core.io.Resource resource = super.getResource(resourcePath, location);
                        // 如果文件不存在，记录日志但不抛出异常
                        if (resource == null || !resource.exists()) {
                            System.err.println("请求的文件不存在: " + resourcePath);
                        }
                        return resource;
                    }
                });
        
        // 添加 /api/uploads/** 映射，兼容前端可能的API路径访问
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + uploadLocation)
                .setCachePeriod(3600)  // 设置缓存时间1小时
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(String resourcePath, org.springframework.core.io.Resource location) throws java.io.IOException {
                        org.springframework.core.io.Resource resource = super.getResource(resourcePath, location);
                        // 如果文件不存在，记录日志但不抛出异常
                        if (resource == null || !resource.exists()) {
                            System.err.println("请求的文件不存在: " + resourcePath);
                        }
                        return resource;
                    }
                });
    }
    
    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("创建目录: " + path);
            } else {
                System.err.println("无法创建目录: " + path);
            }
        }
    }
    
    public String getUploadPath() {
        return uploadPath;
    }
    
    public String getAvatarPath() {
        return avatarPath;
    }
    
    public String getEquipmentPath() {
        return equipmentPath;
    }
    
    public String getReturnPath() {
        return returnPath;
    }
}
