package com.example.photography.util;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片优化工具类
 * 提供图片压缩、缩略图生成等功能
 */
@Component
public class ImageOptimizer {
    
    private static final int DEFAULT_MAX_WIDTH = 800;
    private static final int DEFAULT_MAX_HEIGHT = 600;
    private static final int THUMBNAIL_SIZE = 200;
    private static final float DEFAULT_QUALITY = 0.8f;
    
    /**
     * 压缩图片
     * @param imageBytes 原始图片字节数组
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量 (0.0-1.0)
     * @return 压缩后的图片字节数组
     */
    public byte[] compressImage(byte[] imageBytes, int maxWidth, int maxHeight, float quality) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            BufferedImage originalImage = ImageIO.read(inputStream);
            
            if (originalImage == null) {
                throw new IOException("无法读取图片文件");
            }
            
            // 计算压缩后的尺寸
            Dimension newSize = calculateScaledSize(originalImage.getWidth(), originalImage.getHeight(), maxWidth, maxHeight);
            
            // 创建缩放后的图片
            BufferedImage scaledImage = new BufferedImage(newSize.width, newSize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaledImage.createGraphics();
            
            // 设置渲染质量
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(originalImage, 0, 0, newSize.width, newSize.height, null);
            g2d.dispose();
            
            // 输出为JPEG格式
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(scaledImage, "jpg", outputStream);
                return outputStream.toByteArray();
            }
        }
    }
    
    /**
     * 使用默认设置压缩图片
     */
    public byte[] compressImage(byte[] imageBytes) throws IOException {
        return compressImage(imageBytes, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, DEFAULT_QUALITY);
    }
    
    /**
     * 生成缩略图
     * @param imageBytes 原始图片字节数组
     * @param size 缩略图尺寸（正方形）
     * @return 缩略图字节数组
     */
    public byte[] generateThumbnail(byte[] imageBytes, int size) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            BufferedImage originalImage = ImageIO.read(inputStream);
            
            if (originalImage == null) {
                throw new IOException("无法读取图片文件");
            }
            
            // 创建正方形缩略图
            BufferedImage thumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 计算居中裁剪区域
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int cropSize = Math.min(originalWidth, originalHeight);
            int x = (originalWidth - cropSize) / 2;
            int y = (originalHeight - cropSize) / 2;
            
            g2d.drawImage(originalImage, 0, 0, size, size, x, y, x + cropSize, y + cropSize, null);
            g2d.dispose();
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(thumbnail, "jpg", outputStream);
                return outputStream.toByteArray();
            }
        }
    }
    
    /**
     * 生成默认尺寸缩略图
     */
    public byte[] generateThumbnail(byte[] imageBytes) throws IOException {
        return generateThumbnail(imageBytes, THUMBNAIL_SIZE);
    }
    
    /**
     * 计算缩放后的尺寸，保持宽高比
     */
    private Dimension calculateScaledSize(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        double scaleX = (double) maxWidth / originalWidth;
        double scaleY = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);
        
        // 如果原图尺寸已经小于最大尺寸，则不进行放大
        if (scale >= 1.0) {
            return new Dimension(originalWidth, originalHeight);
        }
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        return new Dimension(newWidth, newHeight);
    }
    
    /**
     * 检查是否需要压缩
     * @param fileSize 文件大小（字节）
     * @param maxSize 最大文件大小（字节）
     * @return 是否需要压缩
     */
    public boolean needCompression(long fileSize, long maxSize) {
        return fileSize > maxSize;
    }
    
    /**
     * 获取图片格式
     */
    public String getImageFormat(byte[] imageBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                return "jpg"; // 统一输出为JPEG格式
            }
        } catch (IOException e) {
            // 忽略异常
        }
        return "jpg";
    }
}
