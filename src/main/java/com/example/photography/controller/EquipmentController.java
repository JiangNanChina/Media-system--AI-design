package com.example.photography.controller;

import com.example.photography.dto.request.EquipmentCreateRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.model.entity.Equipment;
import com.example.photography.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/equipment")
@Tag(name = "设备管理", description = "摄影器材的增删改查、图片上传、库存管理等操作")
public class EquipmentController {
    
    @Autowired
    private EquipmentService equipmentService;
    
    @Autowired
    private com.example.photography.repository.EquipmentRepository equipmentRepository;
    
    @Autowired
    private com.example.photography.repository.BorrowRecordRepository borrowRecordRepository;
    
    /**
     * 强制修复单个设备的库存状态
     */
    @PostMapping("/{id}/fix-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "强制修复设备库存状态", description = "根据借用记录重新计算并修复设备的库存和状态")
    public ApiResponse<Map<String, Object>> forceFixEquipmentStock(@PathVariable Long id) {
        try {
            Equipment equipment = equipmentService.findById(id);
            
            // 记录修复前的状态
            int oldAvailable = equipment.getAvailableQuantity();
            String oldStatus = equipment.getStatus();
            
            // 强制修复
            ((com.example.photography.service.impl.EquipmentServiceImpl) equipmentService)
                    .fixEquipmentStockAndStatus(equipment);
            
            // 重新获取修复后的数据
            Equipment fixedEquipment = equipmentRepository.findById(id).orElse(equipment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("equipmentId", id);
            result.put("equipmentName", fixedEquipment.getName());
            result.put("oldAvailable", oldAvailable);
            result.put("newAvailable", fixedEquipment.getAvailableQuantity());
            result.put("oldStatus", oldStatus);
            result.put("newStatus", fixedEquipment.getStatus());
            result.put("fixed", oldAvailable != fixedEquipment.getAvailableQuantity() || !oldStatus.equals(fixedEquipment.getStatus()));
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("修复失败: " + e.getMessage());
        }
    }
    
    @GetMapping
    @Operation(summary = "分页获取设备列表", description = "分页获取所有设备")
    public ApiResponse<Page<Equipment>> getEquipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Equipment> equipments = equipmentService.findEquipments(pageable);
            // 强制校正每个设备的库存和状态
            equipments.forEach(equipment -> {
                try {
                    ((com.example.photography.service.impl.EquipmentServiceImpl) equipmentService)
                            .fixEquipmentStockAndStatus(equipment);
                } catch (Exception e) {
                    System.err.println("校正设备失败：" + e.getMessage());
                }
            });
            return ApiResponse.success(equipments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索设备", description = "根据关键字搜索设备")
    public ApiResponse<Page<Equipment>> searchEquipments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Equipment> equipments = equipmentService.searchEquipments(keyword, pageable);
            return ApiResponse.success(equipments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "根据分类获取设备", description = "根据设备分类获取设备列表")
    public ApiResponse<Page<Equipment>> getEquipmentsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Equipment> equipments = equipmentService.findByCategory(category, pageable);
            return ApiResponse.success(equipments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/category/{category}/search")
    @Operation(summary = "在分类中搜索设备", description = "在指定分类中根据关键字搜索设备")
    public ApiResponse<Page<Equipment>> searchEquipmentsByCategory(
            @PathVariable String category,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Equipment> equipments = equipmentService.searchEquipmentsByCategory(category, keyword, pageable);
            return ApiResponse.success(equipments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/categories")
    @Operation(summary = "获取所有分类", description = "获取系统中所有的设备分类")
    public ApiResponse<List<String>> getAllCategories() {
        try {
            List<String> categories = equipmentService.getAllCategories();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/available")
    @Operation(summary = "获取可用设备", description = "获取所有可用库存大于0的设备")
    public ApiResponse<List<Equipment>> getAvailableEquipments() {
        try {
            List<Equipment> equipments = equipmentService.getAvailableEquipments();
            return ApiResponse.success(equipments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取设备", description = "根据设备ID获取设备详细信息")
    public ApiResponse<Equipment> getEquipmentById(@PathVariable Long id) {
        try {
            Equipment equipment = equipmentService.findById(id);
            return ApiResponse.success(equipment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建设备", description = "创建新的设备（仅管理员）")
    public ApiResponse<Equipment> createEquipment(@Valid @RequestBody EquipmentCreateRequest request) {
        try {
            Equipment equipment = equipmentService.createEquipment(request);
            return ApiResponse.success("设备创建成功", equipment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新设备信息", description = "更新设备信息（仅管理员）")
    public ApiResponse<Equipment> updateEquipment(@PathVariable Long id, 
                                                 @Valid @RequestBody EquipmentCreateRequest request) {
        try {
            Equipment equipment = equipmentService.updateEquipment(id, request);
            return ApiResponse.success("设备信息更新成功", equipment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除设备", description = "删除指定设备（仅管理员）")
    public ApiResponse<Void> deleteEquipment(@PathVariable Long id) {
        try {
            equipmentService.deleteEquipment(id);
            return ApiResponse.success("设备删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "上传设备图片", description = "上传单个设备图片（仅管理员）")
    public ApiResponse<String> uploadEquipmentImage(@PathVariable Long id, 
                                                  @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = equipmentService.uploadEquipmentImage(id, file);
            return ApiResponse.success("图片上传成功", imageUrl);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量上传设备图片", description = "批量上传设备图片（仅管理员）")
    public ApiResponse<String> uploadEquipmentImages(@PathVariable Long id, 
                                                   @RequestParam("files") MultipartFile[] files) {
        try {
            String imageUrls = equipmentService.uploadEquipmentImages(id, files);
            return ApiResponse.success("图片上传成功", imageUrls);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新设备库存", description = "更新设备库存数量（仅管理员）")
    public ApiResponse<Void> updateStock(@PathVariable Long id, 
                                       @RequestBody Map<String, Integer> request) {
        try {
            Integer stockQuantity = request.get("stockQuantity");
            Integer availableQuantity = request.get("availableQuantity");
            
            if (stockQuantity == null || availableQuantity == null) {
                return ApiResponse.error("库存数量和可用数量不能为空");
            }
            
            equipmentService.updateStock(id, stockQuantity, availableQuantity);
            return ApiResponse.success("库存更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取设备统计信息", description = "获取设备统计数据（仅管理员）")
    public ApiResponse<EquipmentService.EquipmentStatistics> getEquipmentStatistics() {
        try {
            EquipmentService.EquipmentStatistics statistics = equipmentService.getEquipmentStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/available-count")
    @Operation(summary = "获取可用设备数量", description = "获取当前可借用的设备数量（排除维修中、报废等状态）")
    public ApiResponse<Long> getAvailableEquipmentCount() {
        try {
            long availableCount = equipmentService.getAvailableEquipmentCount();
            return ApiResponse.success(availableCount);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/debug-count")
    @Operation(summary = "调试设备数量", description = "获取设备数量的详细调试信息")
    public ApiResponse<Map<String, Object>> getDebugEquipmentCount() {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // 获取基本统计
            var stats = equipmentService.getEquipmentStatistics();
            debugInfo.put("totalEquipments", stats.getTotalEquipments());
            debugInfo.put("normalEquipments", stats.getNormalEquipments());
            debugInfo.put("totalStock", stats.getTotalStock());
            debugInfo.put("availableStock", stats.getAvailableStock());
            
            // 获取可用设备数
            long availableCount = equipmentService.getAvailableEquipmentCount();
            debugInfo.put("calculatedAvailableCount", availableCount);
            
            // 获取所有设备的详细信息
            var allEquipments = equipmentService.getAllEquipments(PageRequest.of(0, 100));
            List<Map<String, Object>> equipmentDetails = new ArrayList<>();
            
            for (var equipment : allEquipments.getContent()) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("id", equipment.getId());
                detail.put("name", equipment.getName());
                detail.put("status", equipment.getStatus());
                detail.put("stockQuantity", equipment.getStockQuantity());
                detail.put("availableQuantity", equipment.getAvailableQuantity());
                detail.put("deleted", equipment.getDeleted());
                equipmentDetails.add(detail);
            }
            
            debugInfo.put("equipments", equipmentDetails);
            debugInfo.put("equipmentCount", equipmentDetails.size());
            
            return ApiResponse.success(debugInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/force-init")
    @Operation(summary = "强制初始化设备数据", description = "强制重新创建示例设备数据")
    public ApiResponse<String> forceInitEquipment() {
        try {
            // 强制创建示例设备，不检查现有数据
            String[] equipments = {
                "佳能EOS R5,单反相机,CANON-R5-002,5,5,专业级全画幅无反相机",
                "索尼A7M4,单反相机,SONY-A7M4-002,3,3,专业级全画幅无反相机",
                "尼康D850,单反相机,NIKON-D850-002,2,2,专业级全画幅单反相机",
                "大疆Mini 3,无人机,DJI-MINI3-002,4,4,便携式航拍无人机",
                "苹果MacBook Pro,笔记本电脑,APPLE-MBP-002,6,6,M2芯片笔记本电脑"
            };
            
            int created = 0;
            for (String equipmentData : equipments) {
                String[] parts = equipmentData.split(",");
                
                EquipmentCreateRequest request = new EquipmentCreateRequest();
                request.setName(parts[0]);
                request.setCategory(parts[1]);
                request.setSerialNumber(parts[2]);
                request.setStockQuantity(Integer.parseInt(parts[3]));
                request.setAvailableQuantity(Integer.parseInt(parts[4]));
                request.setDescription(parts[5]);
                request.setStatus("正常");
                
                try {
                    equipmentService.createEquipment(request);
                    created++;
                } catch (Exception e) {
                    System.err.println("创建设备失败 [" + parts[0] + "]: " + e.getMessage());
                }
            }
            
            return ApiResponse.success("成功创建 " + created + " 个示例设备");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/status-summary")
    @Operation(summary = "获取设备状态汇总", description = "获取各种状态设备的数量统计")
    public ApiResponse<Map<String, Object>> getEquipmentStatusSummary() {
        try {
            var stats = equipmentService.getEquipmentStatistics();
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalEquipments", stats.getTotalEquipments());
            summary.put("normalEquipments", stats.getNormalEquipments());
            summary.put("maintenanceEquipments", stats.getMaintenanceEquipments());
            summary.put("scrapedEquipments", stats.getScrapedEquipments());
            summary.put("totalStock", stats.getTotalStock());
            summary.put("availableStock", stats.getAvailableStock());
            summary.put("borrowedStock", stats.getBorrowedStock());
            summary.put("realAvailableCount", equipmentService.getAvailableEquipmentCount());
            
            return ApiResponse.success(summary);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/list-all")
    @Operation(summary = "查看所有设备", description = "查看数据库中所有设备的详细信息")
    public ApiResponse<Page<Equipment>> getAllEquipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Equipment> equipments = equipmentService.getAllEquipments(pageable);
            return ApiResponse.success(equipments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/init-sample-data")
    @Operation(summary = "初始化示例数据", description = "创建示例设备数据")
    public ApiResponse<String> initSampleData() {
        try {
            System.out.println("=== 手动初始化示例设备数据 ===");
            
            // 创建多个示例设备
            String[] equipments = {
                "佳能EOS R5,单反相机,CANON-R5-001,5,5,专业级全画幅无反相机",
                "索尼A7M4,单反相机,SONY-A7M4-001,3,3,专业级全画幅无反相机",
                "尼康D850,单反相机,NIKON-D850-001,2,2,专业级全画幅单反相机",
                "大疆Mini 3,无人机,DJI-MINI3-001,4,4,便携式航拍无人机",
                "苹果MacBook Pro,笔记本电脑,APPLE-MBP-001,6,6,M2芯片笔记本电脑"
            };
            
            int created = 0;
            for (String equipmentData : equipments) {
                String[] parts = equipmentData.split(",");
                
                EquipmentCreateRequest request = new EquipmentCreateRequest();
                request.setName(parts[0]);
                request.setCategory(parts[1]);
                request.setSerialNumber(parts[2]);
                request.setStockQuantity(Integer.parseInt(parts[3]));
                request.setAvailableQuantity(Integer.parseInt(parts[4]));
                request.setDescription(parts[5]);
                request.setStatus("正常");
                
                try {
                    equipmentService.createEquipment(request);
                    created++;
                    System.out.println("创建设备成功: " + parts[0]);
                } catch (Exception e) {
                    System.err.println("创建设备失败 [" + parts[0] + "]: " + e.getMessage());
                }
            }
            
            System.out.println("成功创建 " + created + " 个示例设备");
            return ApiResponse.success("成功创建 " + created + " 个示例设备");
        } catch (Exception e) {
            System.err.println("创建测试设备失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/check-serial/{serialNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "检查设备编号", description = "检查设备编号是否已存在（仅管理员）")
    public ApiResponse<Boolean> checkSerialNumber(@PathVariable String serialNumber) {
        try {
            boolean exists = equipmentService.existsBySerialNumber(serialNumber);
            return ApiResponse.success("编号检查完成", exists);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // =============== 数据清理相关API ===============
    
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查看已删除的设备", description = "管理员查看所有已软删除的设备（仅管理员）")
    public ApiResponse<Page<Equipment>> getDeletedEquipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
            Page<Equipment> equipments = equipmentService.findDeletedEquipments(pageable);
            return ApiResponse.success(equipments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/cleanup/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取设备清理统计信息", description = "管理员获取已删除设备的统计信息（仅管理员）")
    public ApiResponse<Map<String, Object>> getCleanupStatistics() {
        try {
            Map<String, Object> statistics = equipmentService.getDeletedEquipmentStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/cleanup/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "永久删除设备", description = "管理员永久删除已软删除的设备（仅管理员）")
    public ApiResponse<String> physicalDeleteEquipment(@PathVariable Long id) {
        try {
            equipmentService.physicalDeleteEquipment(id);
            return ApiResponse.success("设备已永久删除");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/cleanup/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量永久删除设备", description = "管理员批量永久删除已软删除的设备（仅管理员）")
    public ApiResponse<String> batchPhysicalDeleteEquipments(@RequestBody List<Long> ids) {
        try {
            equipmentService.physicalDeleteEquipmentsByIds(ids);
            return ApiResponse.success("批量删除完成");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/cleanup/auto")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "自动清理旧的删除设备", description = "管理员自动清理指定天数前的已删除设备（仅管理员）")
    public ApiResponse<String> autoCleanupEquipments(@RequestParam(defaultValue = "30") int daysOld) {
        try {
            int deletedCount = equipmentService.cleanupDeletedEquipments(daysOld);
            return ApiResponse.success("已清理 " + deletedCount + " 个设备");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除设备图片", description = "管理员删除设备的图片文件（仅管理员）")
    public ApiResponse<String> deleteEquipmentImage(@PathVariable Long id) {
        try {
            equipmentService.deleteEquipmentImage(id);
            return ApiResponse.success("设备图片已删除");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/cleanup/orphaned-images")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清理孤立图片文件", description = "管理员清理数据库中没有引用但在文件系统中存在的设备图片文件（仅管理员）")
    public ApiResponse<String> cleanupOrphanedImages() {
        try {
            int cleanedCount = equipmentService.cleanupOrphanedImages();
            return ApiResponse.success("已清理 " + cleanedCount + " 个孤立图片文件");
        } catch (Exception e) {
            return ApiResponse.error("清理失败: " + e.getMessage());
        }
    }
    
    // =============== 临时调试API ===============
    
    @GetMapping("/debug/related-images/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "调试设备相关图片信息", description = "查看指定设备及其相关借用记录的图片信息（调试用）")
    public ApiResponse<Map<String, Object>> debugEquipmentRelatedImages(@PathVariable Long id) {
        try {
            Optional<Equipment> equipmentOpt = equipmentRepository.findById(id);
            
            Map<String, Object> debugInfo = new HashMap<>();
            
            if (equipmentOpt.isEmpty()) {
                debugInfo.put("status", "设备不存在");
                debugInfo.put("equipmentId", id);
                return ApiResponse.success(debugInfo);
            }
            
            Equipment equipment = equipmentOpt.get();
            debugInfo.put("equipmentId", equipment.getId());
            debugInfo.put("equipmentName", equipment.getName());
            debugInfo.put("deleted", equipment.getDeleted());
            debugInfo.put("equipmentImageUrl", equipment.getImageUrl());
            debugInfo.put("hasEquipmentImage", equipment.getImageUrl() != null && !equipment.getImageUrl().trim().isEmpty());
            
            // 查找相关的借用记录
            List<com.example.photography.model.entity.BorrowRecord> relatedRecords = 
                borrowRecordRepository.findByEquipmentId(equipment.getId());
            
            debugInfo.put("relatedBorrowRecordsCount", relatedRecords.size());
            
            List<Map<String, Object>> recordsWithImages = new ArrayList<>();
            for (com.example.photography.model.entity.BorrowRecord record : relatedRecords) {
                if (record.getReturnImages() != null && !record.getReturnImages().trim().isEmpty()) {
                    Map<String, Object> recordInfo = new HashMap<>();
                    recordInfo.put("recordId", record.getId());
                    recordInfo.put("recordDeleted", record.getDeleted());
                    recordInfo.put("status", record.getStatus());
                    recordInfo.put("returnImages", record.getReturnImages());
                    
                    try {
                        List<String> imageUrls = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            record.getReturnImages(), 
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                        );
                        recordInfo.put("parsedImageUrls", imageUrls);
                        recordInfo.put("imageCount", imageUrls.size());
                        
                        // 检查文件是否存在
                        List<Map<String, Object>> fileStatus = new ArrayList<>();
                        for (String imageUrl : imageUrls) {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("url", imageUrl);
                            
                            String relativePath = imageUrl;
                            if (relativePath.startsWith("/")) {
                                relativePath = relativePath.substring(1);
                            }
                            
                            java.nio.file.Path filePath = java.nio.file.Paths.get(relativePath);
                            fileInfo.put("relativePath", relativePath);
                            fileInfo.put("absolutePath", filePath.toAbsolutePath().toString());
                            fileInfo.put("exists", java.nio.file.Files.exists(filePath));
                            
                            if (java.nio.file.Files.exists(filePath)) {
                                try {
                                    long size = java.nio.file.Files.size(filePath);
                                    fileInfo.put("sizeBytes", size);
                                    fileInfo.put("sizeKB", size / 1024);
                                } catch (Exception e) {
                                    fileInfo.put("sizeError", e.getMessage());
                                }
                            }
                            
                            fileStatus.add(fileInfo);
                        }
                        recordInfo.put("fileStatus", fileStatus);
                        
                    } catch (Exception e) {
                        recordInfo.put("parseError", e.getMessage());
                    }
                    
                    recordsWithImages.add(recordInfo);
                }
            }
            
            debugInfo.put("recordsWithReturnImages", recordsWithImages);
            debugInfo.put("recordsWithReturnImagesCount", recordsWithImages.size());
            
            return ApiResponse.success(debugInfo);
            
        } catch (Exception e) {
            return ApiResponse.error("调试失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/fix-consistency")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修复设备状态一致性", description = "修复设备状态与库存不一致的问题（仅管理员）")
    public ApiResponse<String> fixEquipmentConsistency(@PathVariable Long id) {
        try {
            equipmentService.fixEquipmentStatusConsistency(id);
            return ApiResponse.success("设备状态一致性已修复");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/fix-all-consistency")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量修复所有设备状态一致性", description = "批量修复所有设备状态与库存不一致的问题（仅管理员）")
    public ApiResponse<String> fixAllEquipmentConsistency() {
        try {
            int fixedCount = equipmentService.fixAllEquipmentStatusConsistency();
            return ApiResponse.success("已修复 " + fixedCount + " 个设备的状态一致性问题");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/debug/consistency-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "调试设备状态一致性", description = "查看所有设备的状态与库存一致性情况（调试用）")
    public ApiResponse<Map<String, Object>> debugEquipmentConsistency() {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // 获取所有未删除的设备
            List<Equipment> allEquipments = equipmentRepository.findByDeletedFalse();
            
            List<Map<String, Object>> inconsistentEquipments = new ArrayList<>();
            int totalEquipments = allEquipments.size();
            int inconsistentCount = 0;
            
            for (Equipment equipment : allEquipments) {
                String status = equipment.getStatus();
                int availableQuantity = equipment.getAvailableQuantity();
                
                // 检查是否不一致
                boolean isStatusUnavailable = "损坏".equals(status) || "维修中".equals(status) || "报废".equals(status);
                boolean isInconsistent = isStatusUnavailable && availableQuantity > 0;
                
                if (isInconsistent) {
                    Map<String, Object> equipmentInfo = new HashMap<>();
                    equipmentInfo.put("id", equipment.getId());
                    equipmentInfo.put("name", equipment.getName());
                    equipmentInfo.put("status", status);
                    equipmentInfo.put("availableQuantity", availableQuantity);
                    equipmentInfo.put("stockQuantity", equipment.getStockQuantity());
                    inconsistentEquipments.add(equipmentInfo);
                    inconsistentCount++;
                }
            }
            
            debugInfo.put("totalEquipments", totalEquipments);
            debugInfo.put("inconsistentCount", inconsistentCount);
            debugInfo.put("inconsistentEquipments", inconsistentEquipments);
            debugInfo.put("isConsistent", inconsistentCount == 0);
            
            return ApiResponse.success(debugInfo);
            
        } catch (Exception e) {
            return ApiResponse.error("调试失败: " + e.getMessage());
        }
    }
}