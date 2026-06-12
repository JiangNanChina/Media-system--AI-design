package com.example.photography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.photography.config.FileUploadConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * 图片服务控制器
 * 提供优化的图片访问和缓存控制
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    /**
     * 获取设备图片
     * @param filename 图片文件名
     * @return 图片资源，带缓存控制
     */
    @GetMapping("/equipment/{filename:.+}")
    public ResponseEntity<Resource> getEquipmentImage(@PathVariable String filename) {
        try {
            // 优先使用配置的设备目录
            Path primary = Paths.get(fileUploadConfig.getEquipmentPath()).resolve(filename);

            // 兼容历史目录与多环境目录（按优先级尝试）
            Path[] fallbackPaths = new Path[] {
                primary,
                // 基于upload根路径拼接 equipment 子目录
                Paths.get(fileUploadConfig.getUploadPath()).resolve("equipment").resolve(filename),
                // 生产机历史部署路径
                Paths.get("/www/photography/uploads/equipment").resolve(filename),
                // 另一种常见部署结构
                Paths.get("/www/photography/backend/uploads/equipment").resolve(filename),
                // 相对路径（开发或容器内工作目录）
                Paths.get("./uploads/equipment").resolve(filename)
            };

            Resource found = null;
            Path usedPath = null;
            for (Path path : fallbackPaths) {
                try {
                    Resource res = new UrlResource(path.toUri());
                    if (res.exists() && res.isReadable()) {
                        found = res;
                        usedPath = path;
                        break;
                    }
                } catch (Exception ignore) {
                    // 忽略单个路径的异常，继续尝试下一个
                }
            }

            if (found == null) {
                // 未找到可读文件
                return ResponseEntity.notFound().build();
            }

            // 获取文件的MIME类型
            String contentType = getContentType(usedPath);

            // 设置缓存控制头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
            headers.setExpires(System.currentTimeMillis() + Duration.ofDays(30).toMillis());

            // 设置ETag用于协商缓存
            String etag = generateETag(found);
            headers.setETag(etag);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(found);

        } catch (Exception e) {
            // 捕获所有异常，避免返回500，提升健壮性
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取头像图片
     */
    @GetMapping("/avatars/{filename:.+}")
    public ResponseEntity<Resource> getAvatarImage(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get(fileUploadConfig.getAvatarPath()).resolve(filename);
            Resource resource = new UrlResource(imagePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = getContentType(imagePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
            
            String etag = generateETag(resource);
            headers.setETag(etag);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取归还图片
     */
    @GetMapping("/returns/{filename:.+}")
    public ResponseEntity<Resource> getReturnImage(@PathVariable String filename) {
        try {
            // 支持多个fallback路径
            Path[] fallbackPaths = new Path[] {
                // 配置的return路径
                Paths.get(fileUploadConfig.getReturnPath()).resolve(filename),
                // 基于upload根路径拼接 returns 子目录
                Paths.get(fileUploadConfig.getUploadPath()).resolve("returns").resolve(filename),
                // 生产机历史部署路径
                Paths.get("/www/photography/uploads/returns").resolve(filename),
                // 另一种常见部署结构
                Paths.get("/www/photography/backend/uploads/returns").resolve(filename),
                // 相对路径（开发或容器内工作目录）
                Paths.get("./uploads/returns").resolve(filename)
            };

            Resource found = null;
            Path usedPath = null;
            for (Path path : fallbackPaths) {
                try {
                    Resource res = new UrlResource(path.toUri());
                    if (res.exists() && res.isReadable()) {
                        found = res;
                        usedPath = path;
                        break;
                    }
                } catch (Exception ignore) {
                    // 忽略单个路径的异常，继续尝试下一个
                }
            }

            if (found == null) {
                // 未找到可读文件
                return ResponseEntity.notFound().build();
            }

            String contentType = getContentType(usedPath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl(CacheControl.maxAge(Duration.ofHours(24)).cachePublic());
            
            String etag = generateETag(found);
            headers.setETag(etag);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(found);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取站点图片（如登录背景、站点Logo）
     */
    @GetMapping("/site/{filename:.+}")
    public ResponseEntity<Resource> getSiteImage(@PathVariable String filename) {
        try {
            Path[] fallbackPaths = new Path[] {
                // uploadPath/site
                Paths.get(fileUploadConfig.getUploadPath()).resolve("site").resolve(filename),
                // 常见生产路径
                Paths.get("/www/photography/uploads/site").resolve(filename),
                Paths.get("/www/photography/backend/uploads/site").resolve(filename),
                // 相对路径（开发/容器）
                Paths.get("./uploads/site").resolve(filename)
            };

            Resource found = null;
            Path usedPath = null;
            for (Path path : fallbackPaths) {
                try {
                    Resource res = new UrlResource(path.toUri());
                    if (res.exists() && res.isReadable()) {
                        found = res;
                        usedPath = path;
                        break;
                    }
                } catch (Exception ignore) {
                }
            }

            if (found == null) {
                return ResponseEntity.notFound().build();
            }

            String contentType = getContentType(usedPath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
            headers.setETag(generateETag(found));

            return ResponseEntity.ok().headers(headers).body(found);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取文件的MIME类型
     */
    private String getContentType(Path path) {
        try {
            String contentType = Files.probeContentType(path);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    /**
     * 生成ETag
     */
    private String generateETag(Resource resource) {
        try {
            long lastModified = resource.lastModified();
            long contentLength = resource.contentLength();
            return "\"" + Long.toHexString(lastModified) + "-" + Long.toHexString(contentLength) + "\"";
        } catch (IOException e) {
            return "\"default-etag\"";
        }
    }
}
