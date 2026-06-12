package com.example.photography.controller;

import com.example.photography.dto.request.BorrowApprovalRequest;
import com.example.photography.dto.request.BorrowRequest;
import com.example.photography.dto.request.ReturnRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.BorrowRecordResponse;
import com.example.photography.dto.response.DeletedBorrowRecordResponse;
import com.example.photography.model.entity.BorrowRecord;
import com.example.photography.model.enums.BorrowStatus;
import com.example.photography.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 借还管理控制器
 */
@RestController
@RequestMapping("/borrows")
@Tag(name = "借还管理", description = "设备借用申请、审批、归还、记录查询等操作")
public class BorrowController {
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private com.example.photography.repository.BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private com.example.photography.service.EquipmentService equipmentService;
    
    @Autowired
    private com.example.photography.repository.EquipmentRepository equipmentRepository;
    
    /**
     * 调试API：获取设备的借用记录详情
     */
    @GetMapping("/debug/equipment/{equipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getEquipmentBorrowDebugInfo(@PathVariable Long equipmentId) {
        try {
            List<BorrowRecord> records = borrowRecordRepository.findByEquipmentIdOrderByCreatedTimeDesc(equipmentId);
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("equipmentId", equipmentId);
            debugInfo.put("totalRecords", records.size());
            
            List<Map<String, Object>> recordDetails = new ArrayList<>();
            for (BorrowRecord record : records) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("id", record.getId());
                detail.put("status", record.getStatus().name());
                detail.put("statusDescription", record.getStatus().getDescription());
                detail.put("quantity", record.getQuantity());
                detail.put("userName", record.getUser().getRealName());
                detail.put("createdTime", record.getCreatedAt());
                detail.put("approvalTime", record.getApprovalTime());
                detail.put("actualReturnTime", record.getActualReturnTime());
                recordDetails.add(detail);
            }
            debugInfo.put("records", recordDetails);
            
            return ApiResponse.success(debugInfo);
        } catch (Exception e) {
            return ApiResponse.error("获取调试信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 调试API：手动修复设备库存状态
     */
    @PostMapping("/debug/fix-stock/{equipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> fixEquipmentStock(@PathVariable Long equipmentId) {
        try {
            // 获取设备信息
            com.example.photography.model.entity.Equipment equipment = equipmentService.findById(equipmentId);
            
            // 获取设备的所有借用记录
            List<BorrowRecord> records = borrowRecordRepository.findByEquipmentIdOrderByCreatedTimeDesc(equipmentId);
            
            // 计算应有的借出数量（状态为BORROWED的记录数量总和）
            int expectedBorrowedQuantity = records.stream()
                .filter(r -> r.getStatus() == BorrowStatus.BORROWED)
                .mapToInt(BorrowRecord::getQuantity)
                .sum();
            
            // 计算应有的可用数量
            int expectedAvailableQuantity = equipment.getStockQuantity() - expectedBorrowedQuantity;
            
            Map<String, Object> result = new HashMap<>();
            result.put("equipmentId", equipmentId);
            result.put("equipmentName", equipment.getName());
            result.put("totalStock", equipment.getStockQuantity());
            result.put("currentAvailable", equipment.getAvailableQuantity());
            result.put("expectedAvailable", expectedAvailableQuantity);
            result.put("currentBorrowed", equipment.getStockQuantity() - equipment.getAvailableQuantity());
            result.put("expectedBorrowed", expectedBorrowedQuantity);
            result.put("needsFix", equipment.getAvailableQuantity() != expectedAvailableQuantity);
            
            // 如果需要修复，执行修复
            if (equipment.getAvailableQuantity() != expectedAvailableQuantity) {
                equipment.setAvailableQuantity(expectedAvailableQuantity);
                
                // 同时更新设备状态
                if (expectedAvailableQuantity == 0) {
                    equipment.setStatus("借出");
                } else if (expectedAvailableQuantity > 0) {
                    equipment.setStatus("正常");
                }
                
                equipmentRepository.save(equipment);
                
                result.put("fixed", true);
                result.put("newAvailable", equipment.getAvailableQuantity());
                result.put("newStatus", equipment.getStatus());
            } else {
                result.put("fixed", false);
            }
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("修复库存失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/request")
    @Operation(summary = "申请借用设备", description = "用户申请借用设备")
    public ApiResponse<BorrowRecord> submitBorrowRequest(@Valid @RequestBody BorrowRequest request) {
        try {
            System.out.println("===== 收到借用申请请求 =====");
            System.out.println("请求参数:");
            System.out.println("- 设备ID: " + (request != null ? request.getEquipmentId() : "null"));
            System.out.println("- 数量: " + (request != null ? request.getQuantity() : "null"));
            System.out.println("- 预期归还时间: " + (request != null ? request.getExpectedReturnTime() : "null"));
            System.out.println("- 借用原因: " + (request != null ? request.getBorrowReason() : "null"));
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + authentication);
            System.out.println("Authentication details: " + authentication.getDetails());
            System.out.println("Authentication principal: " + authentication.getPrincipal());
            
            if (authentication == null) {
                return ApiResponse.error("用户未认证");
            }
            
            Object details = authentication.getDetails();
            if (details == null) {
                return ApiResponse.error("无法获取用户ID");
            }
            
            Long userId;
            try {
                userId = (Long) details;
            } catch (ClassCastException e) {
                return ApiResponse.error("用户ID格式错误: " + details.getClass().getName());
            }
            
            System.out.println("User ID: " + userId);
            System.out.println("Request: " + request.getEquipmentId() + ", " + request.getQuantity() + ", " + request.getExpectedReturnTime() + ", " + request.getBorrowReason());
            
            BorrowRecord record = borrowService.submitBorrowRequest(userId, request);
            System.out.println("借用申请处理成功，记录ID: " + (record != null ? record.getId() : "null"));
            
            // 返回简单的成功消息而不是完整的实体对象，避免JSON序列化问题
            return ApiResponse.success("借用申请提交成功");
        } catch (Exception e) {
            System.err.println("===== 处理借用申请时发生异常 =====");
            e.printStackTrace();
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "未知错误: " + e.getClass().getSimpleName();
            }
            System.err.println("错误消息: " + errorMessage);
            return ApiResponse.error(errorMessage);
        }
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审批借用申请", description = "管理员审批借用申请（仅管理员）")
    public ApiResponse<Void> approveBorrowRequest(@PathVariable Long id, 
                                                         @Valid @RequestBody BorrowApprovalRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long approverId = (Long) authentication.getDetails();
            BorrowRecord record = borrowService.approveBorrowRequest(id, approverId, request);
            // 返回简单的成功消息而不是完整的实体对象，避免JSON序列化问题
            return ApiResponse.success("审批完成");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/return-images")
    @Operation(summary = "上传归还设备图片", description = "归还设备时上传当前状态图片")
    public ApiResponse<Map<String, String>> uploadReturnImage(@PathVariable Long id, 
                                                             @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = borrowService.uploadReturnImage(id, file);
            Map<String, String> result = new HashMap<>();
            result.put("imageUrl", imageUrl);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("图片上传失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/return")
    @Operation(summary = "归还设备", description = "用户归还设备")
    public ApiResponse<Void> returnEquipment(@PathVariable Long id, 
                                                    @RequestBody ReturnRequest request) {
        try {
            BorrowRecord record = borrowService.returnEquipment(id, request);
            // 返回简单的成功消息而不是完整的实体对象，避免JSON序列化问题
            return ApiResponse.success("设备归还成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页获取所有借还记录", description = "管理员分页获取所有借还记录（仅管理员）")
    public ApiResponse<Page<BorrowRecordResponse>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) BorrowStatus status) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BorrowRecordResponse> records;
            if (status != null) {
                // 按状态过滤
                records = borrowService.findByStatusResponse(status, pageable);
            } else {
                // 获取所有记录
                records = borrowService.findAllRecordsResponse(pageable);
            }
            
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取借用记录详情", description = "根据ID获取借用记录的详细信息")
    public ApiResponse<BorrowRecordResponse> getBorrowRecordDetail(@PathVariable Long id) {
        try {
            BorrowRecordResponse record = borrowService.getBorrowRecordDetail(id);
            return ApiResponse.success(record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除借用记录", description = "删除指定的借用记录（仅管理员）")
    public ApiResponse<Void> deleteBorrowRecord(@PathVariable Long id) {
        try {
            borrowService.deleteBorrowRecord(id);
            return ApiResponse.success("借用记录删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消借用申请", description = "用户取消自己的借用申请（仅待审核状态可取消）")
    public ApiResponse<Void> cancelBorrowRequest(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            borrowService.cancelBorrowRequest(id, username);
            return ApiResponse.success("借用申请已取消");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出借用记录", description = "导出借用记录到Excel文件（仅管理员）")
    public ResponseEntity<byte[]> exportBorrowRecords(
            @RequestParam(required = false) BorrowStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            System.out.println("===== Controller开始处理Excel导出请求 =====");
            System.out.println("请求参数 - status: " + status + ", startDate: " + startDate + ", endDate: " + endDate);
            
            byte[] excelData = borrowService.exportBorrowRecords(status, startDate, endDate);
            System.out.println("Service返回的Excel数据大小: " + (excelData != null ? excelData.length : "null") + " 字节");
            
            if (excelData == null || excelData.length == 0) {
                System.out.println("Excel数据为空，返回noContent");
                return ResponseEntity.noContent().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setCacheControl("no-cache");
            
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = "borrowRecords_" + timestamp + ".xlsx";
            headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            
            System.out.println("设置HTTP头部:");
            System.out.println("- Content-Type: " + headers.getContentType());
            System.out.println("- Content-Length: " + excelData.length);
            System.out.println("- Filename: " + filename);
            
            ResponseEntity<byte[]> response = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelData.length)
                    .body(excelData);
            
            System.out.println("===== 成功创建ResponseEntity，准备返回 =====");
            System.out.println("Response body 大小: " + (response.getBody() != null ? response.getBody().length : "null") + " 字节");
            
            return response;
        } catch (Exception e) {
            System.err.println("===== Controller处理Excel导出时发生异常 =====");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/my-records")
    @Operation(summary = "获取我的借还记录", description = "获取当前用户的借还记录")
    public ApiResponse<Page<BorrowRecordResponse>> getMyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("MyRecords - Authentication: " + authentication);
            System.out.println("MyRecords - Authentication details: " + authentication.getDetails());
            
            if (authentication == null) {
                return ApiResponse.error("用户未认证");
            }
            
            Object details = authentication.getDetails();
            if (details == null) {
                return ApiResponse.error("无法获取用户ID");
            }
            
            Long userId;
            try {
                userId = (Long) details;
            } catch (ClassCastException e) {
                return ApiResponse.error("用户ID格式错误: " + details.getClass().getName());
            }
            
            System.out.println("MyRecords - User ID: " + userId);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BorrowRecordResponse> records = borrowService.findByUserIdResponse(userId, pageable);
            
            System.out.println("MyRecords - Found records: " + records.getTotalElements());
            
            return ApiResponse.success(records);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取指定用户的借还记录", description = "管理员获取指定用户的借还记录（仅管理员）")
    public ApiResponse<Page<BorrowRecordResponse>> getUserRecords(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BorrowRecordResponse> records = borrowService.findByUserIdResponse(userId, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取指定设备的借还记录", description = "管理员获取指定设备的借还记录（仅管理员）")
    public ApiResponse<Page<BorrowRecordResponse>> getEquipmentRecords(@PathVariable Long equipmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BorrowRecordResponse> records = borrowService.findByEquipmentIdResponse(equipmentId, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "根据状态获取借还记录", description = "管理员根据状态获取借还记录（仅管理员）")
    public ApiResponse<Page<BorrowRecordResponse>> getRecordsByStatus(@PathVariable BorrowStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BorrowRecordResponse> records = borrowService.findByStatusResponse(status, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取待审核申请", description = "管理员获取所有待审核的借用申请（仅管理员）")
    public ApiResponse<List<BorrowRecordResponse>> getPendingRequests() {
        try {
            List<BorrowRecordResponse> records = borrowService.getPendingRequestsResponse();
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/my-current")
    @Operation(summary = "获取我当前借用的设备", description = "获取当前用户正在借用的设备")
    public ApiResponse<List<BorrowRecordResponse>> getMyCurrentBorrows() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            List<BorrowRecordResponse> records = borrowService.getUserCurrentBorrowsResponse(userId);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取逾期记录", description = "管理员获取所有逾期的借用记录（仅管理员）")
    public ApiResponse<Page<BorrowRecordResponse>> getOverdueRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<BorrowRecordResponse> records = borrowService.getOverdueRecordsResponse(pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    

    

    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取借还统计信息", description = "管理员获取借还统计数据（仅管理员）")
    public ApiResponse<BorrowService.BorrowStatistics> getBorrowStatistics() {
        try {
            BorrowService.BorrowStatistics statistics = borrowService.getBorrowStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/my-statistics")
    @Operation(summary = "获取个人借用统计信息", description = "获取当前用户的借用统计数据")
    public ApiResponse<BorrowService.UserBorrowStatistics> getMyBorrowStatistics() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            BorrowService.UserBorrowStatistics statistics = borrowService.getUserBorrowStatistics(userId);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // =============== 数据清理相关API ===============
    
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查看已删除的借用记录", description = "管理员查看所有已软删除的借用记录（仅管理员）")
    public ApiResponse<Page<DeletedBorrowRecordResponse>> getDeletedRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
            Page<DeletedBorrowRecordResponse> records = borrowService.findDeletedRecords(pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/cleanup/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取数据清理统计信息", description = "管理员获取已删除记录的统计信息（仅管理员）")
    public ApiResponse<Map<String, Object>> getCleanupStatistics() {
        try {
            Map<String, Object> statistics = borrowService.getDeletedRecordsStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/cleanup/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "永久删除借用记录", description = "管理员永久删除已软删除的借用记录（仅管理员）")
    public ApiResponse<String> physicalDeleteRecord(@PathVariable Long id) {
        try {
            borrowService.physicalDeleteBorrowRecord(id);
            return ApiResponse.success("记录已永久删除");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/cleanup/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量永久删除借用记录", description = "管理员批量永久删除已软删除的借用记录（仅管理员）")
    public ApiResponse<String> batchPhysicalDeleteRecords(@RequestBody List<Long> ids) {
        try {
            borrowService.physicalDeleteBorrowRecordsByIds(ids);
            return ApiResponse.success("批量删除完成");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/cleanup/auto")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "自动清理旧的删除记录", description = "管理员自动清理指定天数前的已删除记录（仅管理员）")
    public ApiResponse<String> autoCleanupRecords(@RequestParam(defaultValue = "30") int daysOld) {
        try {
            int deletedCount = borrowService.cleanupDeletedRecords(daysOld);
            return ApiResponse.success("已清理 " + deletedCount + " 条记录");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // =============== 临时调试API ===============
    
    @GetMapping("/debug/return-images/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "调试归还图片信息", description = "查看指定借用记录的归还图片信息（调试用）")
    public ApiResponse<Map<String, Object>> debugReturnImages(@PathVariable Long id) {
        try {
            Optional<BorrowRecord> recordOpt = borrowRecordRepository.findById(id);
            
            Map<String, Object> debugInfo = new HashMap<>();
            
            if (recordOpt.isEmpty()) {
                debugInfo.put("status", "记录不存在");
                debugInfo.put("recordId", id);
                return ApiResponse.success(debugInfo);
            }
            
            BorrowRecord record = recordOpt.get();
            debugInfo.put("recordId", record.getId());
            debugInfo.put("deleted", record.getDeleted());
            debugInfo.put("status", record.getStatus());
            debugInfo.put("returnImages", record.getReturnImages());
            debugInfo.put("hasReturnImages", record.getReturnImages() != null && !record.getReturnImages().trim().isEmpty());
            
            // 如果有归还图片，尝试解析
            if (record.getReturnImages() != null && !record.getReturnImages().trim().isEmpty()) {
                try {
                    List<String> imageUrls = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        record.getReturnImages(), 
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                    );
                    debugInfo.put("parsedImageUrls", imageUrls);
                    debugInfo.put("imageCount", imageUrls.size());
                    
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
                    debugInfo.put("fileStatus", fileStatus);
                    
                } catch (Exception e) {
                    debugInfo.put("parseError", e.getMessage());
                }
            }
            
            return ApiResponse.success(debugInfo);
            
        } catch (Exception e) {
            return ApiResponse.error("调试失败: " + e.getMessage());
        }
    }
}
