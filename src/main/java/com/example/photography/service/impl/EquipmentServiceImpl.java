package com.example.photography.service.impl;

import com.example.photography.config.FileUploadConfig;
import com.example.photography.dto.request.EquipmentCreateRequest;
import com.example.photography.model.entity.Equipment;
import com.example.photography.repository.EquipmentRepository;
import com.example.photography.service.EquipmentService;
import com.example.photography.service.FileValidationService;
import com.example.photography.util.ImageOptimizer;
import com.example.photography.util.ProcessedMultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

/**
 * 设备服务实现类 - 简化版本，专注于解决文件引用问题
 */
@Service
@Transactional
public class EquipmentServiceImpl implements EquipmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(EquipmentServiceImpl.class);
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    @Autowired
    private FileValidationService fileValidationService;
    
    @Autowired
    private ImageOptimizer imageOptimizer;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 验证和修复设备的图片URL
     * 如果文件不存在，将从imageUrl中移除无效的引用
     */
    private void validateAndFixImageUrls(Equipment equipment) {
        if (equipment.getImageUrl() == null || equipment.getImageUrl().trim().isEmpty()) {
            return;
        }
        
        try {
            String imageUrl = equipment.getImageUrl().trim();
            boolean needsUpdate = false;
            
            // 处理JSON数组格式的多个图片
            if (imageUrl.startsWith("[")) {
                List<String> imageUrls = objectMapper.readValue(imageUrl, new TypeReference<List<String>>() {});
                List<String> validUrls = fileValidationService.validateFiles(imageUrls);
                
                if (validUrls.size() != imageUrls.size()) {
                    // 有无效文件被移除
                    needsUpdate = true;
                    if (validUrls.isEmpty()) {
                        equipment.setImageUrl(null);
                    } else {
                        equipment.setImageUrl(objectMapper.writeValueAsString(validUrls));
                    }
                    logger.info("设备ID={} 移除了 {} 个无效的图片引用", 
                        equipment.getId(), imageUrls.size() - validUrls.size());
                }
            }
            // 处理单个图片URL
            else if (!fileValidationService.validateFile(imageUrl)) {
                equipment.setImageUrl(null);
                needsUpdate = true;
                logger.info("设备ID={} 移除了无效的图片引用: {}", equipment.getId(), imageUrl);
            }
            
            // 如果需要更新，保存到数据库
            if (needsUpdate) {
                equipmentRepository.save(equipment);
            }
            
        } catch (Exception e) {
            logger.error("验证设备图片URL时发生错误，设备ID={}", equipment.getId(), e);
        }
    }

    @Override
    public Equipment createEquipment(EquipmentCreateRequest request) {
        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setCategoryName(request.getCategory());  // 使用setCategoryName而不是setCategory
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setDescription(request.getDescription());
        equipment.setStockQuantity(request.getStockQuantity());
        equipment.setAvailableQuantity(request.getAvailableQuantity());
        // 如果损坏数量为null，设置为0
        equipment.setDamagedQuantity(request.getDamagedQuantity() != null ? request.getDamagedQuantity() : 0);
        equipment.setStatus(request.getStatus());
        equipment.setSpecifications(request.getSpecifications());
        equipment.setDeleted(false);
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setUpdatedAt(LocalDateTime.now());
        
        return equipmentRepository.save(equipment);
    }

    @Override
    public Equipment updateEquipment(Long id, EquipmentCreateRequest request) {
        Equipment equipment = findById(id);
        equipment.setName(request.getName());
        equipment.setCategoryName(request.getCategory());  // 使用setCategoryName而不是setCategory
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setDescription(request.getDescription());
        equipment.setStockQuantity(request.getStockQuantity());
        equipment.setAvailableQuantity(request.getAvailableQuantity());
        // 如果损坏数量为null，设置为0
        equipment.setDamagedQuantity(request.getDamagedQuantity() != null ? request.getDamagedQuantity() : 0);
        equipment.setStatus(request.getStatus());
        equipment.setSpecifications(request.getSpecifications());
        equipment.setUpdatedAt(LocalDateTime.now());
        
        return equipmentRepository.save(equipment);
    }

    @Override
    public void deleteEquipment(Long id) {
        Equipment equipment = findById(id);
        equipment.setDeleted(true);
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Equipment findById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        
        // 验证和修复图片URL
        validateAndFixImageUrls(equipment);
        
        return equipment;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Equipment> findEquipments(Pageable pageable) {
        Page<Equipment> equipments = equipmentRepository.findByDeletedFalse(pageable);
        // 验证和修复图片URL
        equipments.getContent().forEach(this::validateAndFixImageUrls);
        return equipments;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Equipment> searchEquipments(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return equipmentRepository.findByDeletedFalse(pageable);
        }
        return equipmentRepository.searchEquipment(keyword.trim(), pageable);
    }

    @Override
    public Page<Equipment> findByCategory(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty()) {
            return equipmentRepository.findByDeletedFalse(pageable);
        }
        return equipmentRepository.findByCategoryAndDeletedFalse(category.trim(), pageable);
    }

    @Override
    public Page<Equipment> searchEquipmentsByCategory(String category, String keyword, Pageable pageable) {
        if (category == null || category.trim().isEmpty()) {
            return searchEquipments(keyword, pageable);
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            return findByCategory(category, pageable);
        }
        return equipmentRepository.searchEquipmentByCategory(category.trim(), keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return equipmentRepository.findAllCategories();
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return equipmentRepository.existsBySerialNumberAndDeletedFalse(serialNumber);
    }

    @Override
    @Transactional
    public String uploadEquipmentImage(Long equipmentId, MultipartFile file) {
        try {
            // 验证设备是否存在
            Equipment equipment = findById(equipmentId);
            
            // 验证文件
            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("只能上传图片文件");
            }
            
            // 验证文件大小（最大10MB）
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                throw new RuntimeException("文件大小不能超过10MB");
            }
            
            // 生成文件名和保存路径
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = java.util.UUID.randomUUID().toString() + fileExtension;
            
            // 确保上传目录存在
            java.nio.file.Path equipmentDir = java.nio.file.Paths.get(fileUploadConfig.getEquipmentPath());
            java.nio.file.Files.createDirectories(equipmentDir);
            
            // 图片压缩处理
            byte[] compressedImageBytes;
            try {
                // 读取原始文件字节
                byte[] originalBytes = file.getBytes();
                
                // 如果文件大于1MB或尺寸过大，进行压缩
                if (file.getSize() > 1024 * 1024 || imageOptimizer.needCompression(file.getSize(), 2 * 1024 * 1024)) {
                    logger.info("压缩图片，原大小: {}KB", file.getSize() / 1024);
                    compressedImageBytes = imageOptimizer.compressImage(originalBytes);
                    logger.info("压缩后大小: {}KB", compressedImageBytes.length / 1024);
                } else {
                    compressedImageBytes = originalBytes;
                }
            } catch (Exception e) {
                logger.warn("图片压缩失败，使用原图: {}", e.getMessage());
                compressedImageBytes = file.getBytes();
            }
            
            // 保存处理后的文件
            java.nio.file.Path filePath = equipmentDir.resolve(filename);
            java.nio.file.Files.write(filePath, compressedImageBytes);
            
            // 构建访问URL
            String imageUrl = "/uploads/equipment/" + filename;
            
            // 更新设备记录
            equipment.setImageUrl(imageUrl);
            equipment.setUpdatedAt(java.time.LocalDateTime.now());
            equipmentRepository.save(equipment);
            
            logger.info("设备图片上传成功，设备ID: {}, 文件: {}", equipmentId, filename);
            return imageUrl;
            
        } catch (Exception e) {
            logger.error("设备图片上传失败，设备ID: " + equipmentId, e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String uploadEquipmentImages(Long equipmentId, MultipartFile[] files) {
        try {
            // 验证设备是否存在
            Equipment equipment = findById(equipmentId);
            
            if (files == null || files.length == 0) {
                throw new RuntimeException("请选择要上传的文件");
            }
            
            java.util.List<String> imageUrls = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                
                // 验证文件类型
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new RuntimeException("只能上传图片文件: " + file.getOriginalFilename());
                }
                
                // 验证文件大小
                long maxSize = 10 * 1024 * 1024; // 10MB
                if (file.getSize() > maxSize) {
                    throw new RuntimeException("文件大小不能超过10MB: " + file.getOriginalFilename());
                }
                
                // 生成文件名和保存路径
                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = java.util.UUID.randomUUID().toString() + fileExtension;
                
                // 确保上传目录存在
                java.nio.file.Path equipmentDir = java.nio.file.Paths.get(fileUploadConfig.getEquipmentPath());
                java.nio.file.Files.createDirectories(equipmentDir);
                
                // 图片压缩处理
                byte[] compressedImageBytes;
                try {
                    // 读取原始文件字节
                    byte[] originalBytes = file.getBytes();
                    
                    // 如果文件大于1MB或尺寸过大，进行压缩
                    if (file.getSize() > 1024 * 1024 || imageOptimizer.needCompression(file.getSize(), 2 * 1024 * 1024)) {
                        logger.info("压缩图片，原大小: {}KB", file.getSize() / 1024);
                        compressedImageBytes = imageOptimizer.compressImage(originalBytes);
                        logger.info("压缩后大小: {}KB", compressedImageBytes.length / 1024);
                    } else {
                        compressedImageBytes = originalBytes;
                    }
                } catch (Exception e) {
                    logger.warn("图片压缩失败，使用原图: {}", e.getMessage());
                    compressedImageBytes = file.getBytes();
                }
                
                // 保存处理后的文件
                java.nio.file.Path filePath = equipmentDir.resolve(filename);
                java.nio.file.Files.write(filePath, compressedImageBytes);
                
                // 添加到URL列表
                String imageUrl = "/uploads/equipment/" + filename;
                imageUrls.add(imageUrl);
            }
            
            if (imageUrls.isEmpty()) {
                throw new RuntimeException("没有有效的图片文件");
            }
            
            // 更新设备记录 - 保存为JSON数组
            String imageUrlsJson;
            if (imageUrls.size() == 1) {
                imageUrlsJson = imageUrls.get(0);
            } else {
                imageUrlsJson = objectMapper.writeValueAsString(imageUrls);
            }
            
            equipment.setImageUrl(imageUrlsJson);
            equipment.setUpdatedAt(java.time.LocalDateTime.now());
            equipmentRepository.save(equipment);
            
            logger.info("设备图片批量上传成功，设备ID: {}, 文件数: {}", equipmentId, imageUrls.size());
            return imageUrlsJson;
            
        } catch (Exception e) {
            logger.error("设备图片批量上传失败，设备ID: " + equipmentId, e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    @Override
    public void updateStock(Long equipmentId, Integer stockQuantity, Integer availableQuantity) {
        Equipment equipment = findById(equipmentId);
        equipment.setStockQuantity(stockQuantity);
        equipment.setAvailableQuantity(availableQuantity);
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getAvailableEquipments() {
        // 简化实现，获取所有未删除的设备
        return equipmentRepository.findAll().stream()
                .filter(e -> !e.getDeleted())
                .toList();
    }

    @Override
    public void decreaseAvailableStock(Long equipmentId, Integer quantity) {
        Equipment equipment = findById(equipmentId);
        int newAvailable = equipment.getAvailableQuantity() - quantity;
        equipment.setAvailableQuantity(Math.max(0, newAvailable));
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    @Override
    public void increaseAvailableStock(Long equipmentId, Integer quantity) {
        Equipment equipment = findById(equipmentId);
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + quantity);
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    @Override
    public void increaseDamagedQuantity(Long equipmentId, Integer quantity) {
        Equipment equipment = findById(equipmentId);
        equipment.setDamagedQuantity(equipment.getDamagedQuantity() + quantity);
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    @Override
    public EquipmentStatistics getEquipmentStatistics() {
        logger.info("开始计算设备统计信息...");
        
        // 获取所有未删除的设备
        List<Equipment> allEquipments = equipmentRepository.findByDeletedFalse();
        long totalEquipments = allEquipments.size();
        logger.info("总设备数: {}", totalEquipments);
        
        // 计算总库存和可用库存
        long totalStock = allEquipments.stream()
            .mapToLong(e -> e.getStockQuantity() != null ? e.getStockQuantity() : 0)
            .sum();
        long availableStock = allEquipments.stream()
            .mapToLong(e -> e.getAvailableQuantity() != null ? e.getAvailableQuantity() : 0)
            .sum();
        long borrowedStock = totalStock - availableStock;
        
        logger.info("总库存: {}, 可用库存: {}, 已借出库存: {}", totalStock, availableStock, borrowedStock);
        
        // 按状态统计设备数量
        long normalEquipments = allEquipments.stream()
            .filter(e -> "正常".equals(e.getStatus()) || "normal".equalsIgnoreCase(e.getStatus()))
            .count();
        long maintenanceEquipments = allEquipments.stream()
            .filter(e -> "维护中".equals(e.getStatus()) || "maintenance".equalsIgnoreCase(e.getStatus()))
            .count();
        long scrapedEquipments = allEquipments.stream()
            .filter(e -> "报废".equals(e.getStatus()) || "scrapped".equalsIgnoreCase(e.getStatus()))
            .count();
        
        logger.info("正常设备: {}, 维护中设备: {}, 报废设备: {}", normalEquipments, maintenanceEquipments, scrapedEquipments);
        
        // 统计分类数量
        long categoryCount = equipmentRepository.findAllCategories().size();
        logger.info("设备分类数: {}", categoryCount);
        
        EquipmentStatistics statistics = new EquipmentStatistics(
            totalEquipments, 
            totalStock, 
            availableStock, 
            borrowedStock, 
            normalEquipments, 
            maintenanceEquipments, 
            scrapedEquipments, 
            categoryCount
        );
        
        logger.info("设备统计信息计算完成: {}", statistics);
        return statistics;
    }

    @Override
    public long getAvailableEquipmentCount() {
        // 🔧 修复：计算实际可用库存数量，而不是设备记录总数
        List<Equipment> allEquipments = equipmentRepository.findByDeletedFalse();
        long availableCount = allEquipments.stream()
            .mapToLong(e -> e.getAvailableQuantity() != null ? e.getAvailableQuantity() : 0)
            .sum();
        
        logger.info("计算可用设备数量: {} 台设备记录，总可用库存: {} 件", allEquipments.size(), availableCount);
        return availableCount;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Equipment> getAllEquipments(Pageable pageable) {
        Page<Equipment> equipments = equipmentRepository.findByDeletedFalse(pageable);
        // 验证和修复图片URL
        equipments.getContent().forEach(this::validateAndFixImageUrls);
        return equipments;
    }

    @Override
    public void physicalDeleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    @Override
    public void physicalDeleteEquipmentsByIds(List<Long> ids) {
        equipmentRepository.deleteAllById(ids);
    }

    @Override
    public Map<String, Object> getDeletedEquipmentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        // TODO: 实现已删除设备统计
        return stats;
    }

    @Override
    public Page<Equipment> findDeletedEquipments(Pageable pageable) {
        return equipmentRepository.findByDeletedTrue(pageable);
    }

    @Override
    public int cleanupDeletedEquipments(int daysOld) {
        // TODO: 实现清理逻辑
        return 0;
    }

    @Override
    public void deleteEquipmentImage(Long equipmentId) {
        Equipment equipment = findById(equipmentId);
        
        // 如果设备有图片，先删除物理文件
        if (equipment.getImageUrl() != null && !equipment.getImageUrl().trim().isEmpty()) {
            try {
                deleteImageFile(equipment.getImageUrl());
                logger.info("成功删除设备 {} 的图片文件: {}", equipmentId, equipment.getImageUrl());
            } catch (Exception e) {
                logger.warn("删除设备 {} 的图片文件失败: {}, 错误: {}", 
                    equipmentId, equipment.getImageUrl(), e.getMessage());
                // 不抛出异常，继续清空数据库记录
            }
        }
        
        // 清空数据库中的图片URL
        equipment.setImageUrl(null);
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }
    
    /**
     * 删除单个图片文件
     * @param imageUrl 图片URL路径
     */
    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }
        
        try {
            logger.info("准备删除图片文件: {}", imageUrl);
            
            // 处理相对路径（如 "/uploads/equipment/xxx.jpg" 或 "uploads/equipment/xxx.jpg"）
            String relativePath = imageUrl;
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1); // 移除开头的斜杠
            }
            
            // 尝试多种可能的文件路径
            String[] possiblePaths = {
                relativePath,  // 直接相对路径
                "./" + relativePath,  // 当前目录 + 相对路径
                fileUploadConfig.getEquipmentPath() + "/" + java.nio.file.Paths.get(relativePath).getFileName().toString(),  // 配置路径 + 文件名
                "/www/photography/" + relativePath  // 生产环境绝对路径
            };
            
            boolean deleted = false;
            for (String pathStr : possiblePaths) {
                java.nio.file.Path filePath = java.nio.file.Paths.get(pathStr);
                logger.debug("尝试删除路径: {}", filePath.toAbsolutePath());
                
                if (java.nio.file.Files.exists(filePath)) {
                    try {
                        java.nio.file.Files.delete(filePath);
                        logger.info("✓ 成功删除图片文件: {}", filePath.toAbsolutePath());
                        deleted = true;
                        break;
                    } catch (Exception e) {
                        logger.warn("删除文件失败 {}: {}", filePath.toAbsolutePath(), e.getMessage());
                    }
                }
            }
            
            if (!deleted) {
                logger.warn("✗ 未找到要删除的图片文件: {}", imageUrl);
            }
            
        } catch (Exception e) {
            logger.error("删除图片文件异常: {}, 错误: {}", imageUrl, e.getMessage(), e);
        }
    }

    @Override
    public void updateEquipmentStatus(Long equipmentId, String status) {
        Equipment equipment = findById(equipmentId);
        equipment.setStatus(status);
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }
    
    @Override
    public int cleanupOrphanedImages() {
        int cleanedCount = 0;
        
        try {
            logger.info("开始清理设备图片孤立文件...");
            
            // 获取数据库中所有有效的图片URL
            List<Equipment> equipmentsWithImages = equipmentRepository.findByImageUrlIsNotNull();
            Set<String> validImageUrls = equipmentsWithImages.stream()
                    .map(Equipment::getImageUrl)
                    .filter(url -> url != null && !url.trim().isEmpty())
                    .map(url -> {
                        // 标准化URL，提取文件名
                        String fileName = java.nio.file.Paths.get(url).getFileName().toString();
                        return fileName;
                    })
                    .collect(java.util.stream.Collectors.toSet());
            
            logger.info("数据库中有效的图片文件数量: {}", validImageUrls.size());
            
            // 检查设备图片目录中的所有文件
            String[] possibleDirs = {
                fileUploadConfig.getEquipmentPath(),
                "./uploads/equipment",
                "/www/photography/uploads/equipment"
            };
            
            for (String dirPath : possibleDirs) {
                java.nio.file.Path equipmentDir = java.nio.file.Paths.get(dirPath);
                if (java.nio.file.Files.exists(equipmentDir) && java.nio.file.Files.isDirectory(equipmentDir)) {
                    logger.info("检查目录: {}", equipmentDir.toAbsolutePath());
                    
                    try (java.util.stream.Stream<java.nio.file.Path> files = 
                            java.nio.file.Files.list(equipmentDir)) {
                        
                        java.util.List<java.nio.file.Path> filesToDelete = files
                                .filter(java.nio.file.Files::isRegularFile)
                                .filter(file -> {
                                    String fileName = file.getFileName().toString();
                                    // 检查是否是图片文件
                                    return fileName.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp|webp)$") 
                                           && !validImageUrls.contains(fileName);
                                })
                                .collect(java.util.stream.Collectors.toList());
                        
                        for (java.nio.file.Path file : filesToDelete) {
                            try {
                                long fileSize = java.nio.file.Files.size(file);
                                java.nio.file.Files.delete(file);
                                cleanedCount++;
                                logger.info("✓ 删除孤立图片文件: {} ({}KB)", 
                                    file.getFileName(), fileSize / 1024);
                            } catch (Exception e) {
                                logger.warn("删除孤立文件失败 {}: {}", 
                                    file.getFileName(), e.getMessage());
                            }
                        }
                    }
                }
            }
            
            logger.info("设备图片孤立文件清理完成，共清理 {} 个文件", cleanedCount);
            
        } catch (Exception e) {
            logger.error("清理设备图片孤立文件时发生异常: {}", e.getMessage(), e);
        }
        
        return cleanedCount;
    }

    @Override
    public void fixEquipmentStatusConsistency(Long equipmentId) {
        // TODO: 实现状态一致性修复
    }

    @Override
    public int fixAllEquipmentStatusConsistency() {
        // TODO: 实现批量状态一致性修复
        return 0;
    }
    
    /**
     * 修复设备库存和状态 - Controller调用的方法
     */
    @Transactional
    public void fixEquipmentStockAndStatus(Equipment equipment) {
        // 简单的修复逻辑：确保状态一致
        if (equipment.getAvailableQuantity() <= 0 && !"全部借出".equals(equipment.getStatus())) {
            equipment.setStatus("全部借出");
        } else if (equipment.getAvailableQuantity() > 0 && equipment.getAvailableQuantity() < equipment.getStockQuantity()) {
            equipment.setStatus("部分借出");
        } else if (equipment.getAvailableQuantity() >= equipment.getStockQuantity()) {
            equipment.setStatus("可借用");
        }
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    @PostConstruct
    public void init() {
        logger.info("EquipmentService initialized");
    }
}
