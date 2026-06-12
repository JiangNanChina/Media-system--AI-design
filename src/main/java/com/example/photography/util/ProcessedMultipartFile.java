package com.example.photography.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 处理后的MultipartFile实现
 * 用于包装压缩后的图片数据
 */
public class ProcessedMultipartFile implements MultipartFile {
    
    private final String originalFilename;
    private final byte[] content;
    private final String contentType;
    
    public ProcessedMultipartFile(String originalFilename, byte[] content) {
        this.originalFilename = originalFilename;
        this.content = content;
        this.contentType = "image/jpeg"; // 压缩后统一为JPEG格式
    }
    
    @Override
    public String getName() {
        return "file";
    }
    
    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }
    
    @Override
    public long getSize() {
        return content.length;
    }
    
    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }
    
    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("transferTo not supported");
    }
}
