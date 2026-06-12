package com.example.photography.service.impl;

import com.example.photography.dto.request.BorrowApprovalRequest;
import com.example.photography.dto.request.BorrowRequest;
import com.example.photography.dto.request.ReturnRequest;
import com.example.photography.dto.response.BorrowRecordResponse;
import com.example.photography.dto.response.DeletedBorrowRecordResponse;
import com.example.photography.model.entity.BorrowRecord;
import com.example.photography.model.entity.Equipment;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.BorrowStatus;
import com.example.photography.repository.BorrowRecordRepository;
import com.example.photography.repository.EquipmentRepository;
import com.example.photography.service.AnnouncementService;
import com.example.photography.service.BorrowService;
import com.example.photography.service.EquipmentService;
import com.example.photography.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.photography.util.FileUploadUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 借还管理服务实现类
 */
@Service
@Transactional
public class BorrowServiceImpl implements BorrowService {
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EquipmentService equipmentService;
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private AnnouncementService announcementService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.photography.config.FileUploadConfig fileUploadConfig;
    
    @Autowired
    private com.example.photography.util.FileUploadUtil fileUploadUtil;
    
    @Autowired
    private com.example.photography.util.ImageOptimizer imageOptimizer;
    
    @Override
    public BorrowRecord submitBorrowRequest(Long userId, BorrowRequest request) {
        User user = userService.findById(userId);
        Equipment equipment = equipmentService.findById(request.getEquipmentId());
        
        // 检查设备状态，损坏的设备不能借用
        if ("损坏".equals(equipment.getStatus())) {
            throw new RuntimeException("该设备当前状态为损坏，暂时无法借用，请联系管理员处理");
        }
        
        // 检查设备可用库存
        if (equipment.getAvailableQuantity() < request.getQuantity()) {
            throw new RuntimeException("设备可用库存不足，当前可用数量：" + equipment.getAvailableQuantity());
        }
        
        // 检查预计归还时间是否合理
        if (request.getExpectedReturnTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("预计归还时间不能早于当前时间");
        }
        
        // 创建借用记录
        BorrowRecord record = new BorrowRecord(user, equipment, request.getQuantity(), request.getExpectedReturnTime());
        record.setBorrowReason(request.getBorrowReason());
        record.setStatus(BorrowStatus.PENDING);
        
        return borrowRecordRepository.save(record);
    }
    
    @Override
    @Transactional
    public BorrowRecord approveBorrowRequest(Long recordId, Long approverId, BorrowApprovalRequest request) {
        BorrowRecord record = findById(recordId);
        User approver = userService.findById(approverId);
        
        // 检查记录状态
        if (record.getStatus() != BorrowStatus.PENDING) {
            throw new RuntimeException("该申请已经处理过了，当前状态：" + record.getStatus().getDescription());
        }
        
        record.setApprovedBy(approver);
        record.setApprovalTime(LocalDateTime.now());
        record.setApprovalNotes(request.getApprovalNotes());
        
        if (request.getApproved()) {
            // 批准申请
            Equipment equipment = record.getEquipment();
            // 确保设备数据是最新的
            equipment = equipmentService.findById(equipment.getId());
            
            // 再次检查库存（防止并发问题）
            if (equipment.getAvailableQuantity() < record.getQuantity()) {
                throw new RuntimeException("设备可用库存不足，无法批准申请");
            }
            
            // 减少可用库存
            System.out.println(String.format("准备减少库存。设备ID:%d, 减少数量:%d, 当前可用:%d", 
                equipment.getId(), record.getQuantity(), equipment.getAvailableQuantity()));
            equipmentService.decreaseAvailableStock(equipment.getId(), record.getQuantity());
            
            record.setStatus(BorrowStatus.BORROWED);
            System.out.println(String.format("借用申请已批准。记录ID:%d, 状态:%s", record.getId(), record.getStatus()));
        } else {
            // 拒绝申请
            record.setStatus(BorrowStatus.REJECTED);
        }
        
        return borrowRecordRepository.save(record);
    }
    
    @Override
    @Transactional
    public String uploadReturnImage(Long recordId, MultipartFile file) {
        // 检查借还记录是否存在
        BorrowRecord record = borrowRecordRepository.findByIdAndDeletedFalse(recordId)
                .orElseThrow(() -> new RuntimeException("借还记录不存在"));
        
        // 使用注入的文件上传工具，支持最大20MB，启用图片压缩
        String imageUrl = fileUploadUtil.uploadFile(file, "returns", 20 * 1024 * 1024, true, true);
        
        System.out.println("归还图片上传成功: " + imageUrl);
        return imageUrl;
    }
    
    @Override
    public BorrowRecord returnEquipment(Long recordId, ReturnRequest request) {
        BorrowRecord record = findById(recordId);
        
        // 检查记录状态
        if (record.getStatus() != BorrowStatus.BORROWED) {
            throw new RuntimeException("该记录不是借用状态，无法归还");
        }
        
        // 根据设备状态处理库存
        if ("damaged".equals(request.getCondition())) {
            // 损坏归还：增加损坏数量，不增加可用库存
            equipmentService.increaseDamagedQuantity(record.getEquipment().getId(), record.getQuantity());
            System.out.println("设备损坏归还，损坏数量增加：" + record.getQuantity());
        } else {
            // 正常归还：增加可用库存
            equipmentService.increaseAvailableStock(record.getEquipment().getId(), record.getQuantity());
            System.out.println("设备正常归还，可用库存增加：" + record.getQuantity());
        }
        
        // 更新记录信息
        record.setActualReturnTime(LocalDateTime.now());
        record.setReturnNotes(request.getReturnNotes());
        record.setDamageDescription(request.getDamageDescription());
        record.setStatus(BorrowStatus.RETURNED);
        
        // 保存归还图片信息
        if (request.getReturnImages() != null && !request.getReturnImages().isEmpty()) {
            try {
                String imagesJson = objectMapper.writeValueAsString(request.getReturnImages());
                record.setReturnImages(imagesJson);
            } catch (JsonProcessingException e) {
                System.err.println("归还图片信息序列化失败: " + e.getMessage());
                // 不阻止归还流程，只记录错误
            }
        }
        
        // 先保存归还记录
        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        
        // 检查是否有设备损坏，如果有则创建维修通知公告并更新设备状态（在单独的事务中）
        if (isEquipmentDamaged(request.getCondition(), request.getReturnNotes(), request.getDamageDescription())) {
            // 更新设备状态为"损坏"
            updateEquipmentStatusToDamaged(savedRecord.getEquipment().getId());
            // 创建维修通知公告
            createMaintenanceNotificationAsync(savedRecord);
        }
        
        return savedRecord;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BorrowRecord findById(Long id) {
        return borrowRecordRepository.findById(id)
                .filter(record -> !record.getDeleted())
                .orElseThrow(() -> new RuntimeException("借还记录不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public BorrowRecordResponse getBorrowRecordDetail(Long id) {
        // 使用预加载查询避免懒加载问题
        BorrowRecord record = borrowRecordRepository.findByIdAndDeletedFalseWithDetails(id)
                .orElseThrow(() -> new RuntimeException("借还记录不存在"));
        return convertToBorrowRecordResponse(record);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecord> findAllRecords(Pageable pageable) {
        return borrowRecordRepository.findByDeletedFalse(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> findAllRecordsResponse(Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByDeletedFalseWithDetails(pageable);
        return records.map(this::convertToBorrowRecordResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecord> findByUserId(Long userId, Pageable pageable) {
        return borrowRecordRepository.findByUser_IdAndDeletedFalse(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> findByUserIdResponse(Long userId, Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByUser_IdAndDeletedFalseWithDetails(userId, pageable);
        return records.map(this::convertToBorrowRecordResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecord> findByEquipmentId(Long equipmentId, Pageable pageable) {
        return borrowRecordRepository.findByEquipment_IdAndDeletedFalse(equipmentId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> findByEquipmentIdResponse(Long equipmentId, Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByEquipment_IdAndDeletedFalseWithDetails(equipmentId, pageable);
        return records.map(this::convertToBorrowRecordResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable) {
        return borrowRecordRepository.findByStatusAndDeletedFalse(status, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> findByStatusResponse(BorrowStatus status, Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByStatusAndDeletedFalseWithDetails(status, pageable);
        return records.map(this::convertToBorrowRecordResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecord> getPendingRequests() {
        return borrowRecordRepository.findByStatusAndDeletedFalseOrderByCreatedAtAsc(BorrowStatus.PENDING);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getPendingRequestsResponse() {
        List<BorrowRecord> records = borrowRecordRepository.findByStatusAndDeletedFalseOrderByCreatedAtAsc(BorrowStatus.PENDING);
        return records.stream()
                .map(this::convertToBorrowRecordResponse)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecord> getUserCurrentBorrows(Long userId) {
        return borrowRecordRepository.findByUser_IdAndStatusAndDeletedFalse(userId, BorrowStatus.BORROWED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getUserCurrentBorrowsResponse(Long userId) {
        List<BorrowRecord> records = borrowRecordRepository.findByUser_IdAndStatusAndDeletedFalse(userId, BorrowStatus.BORROWED);
        return records.stream()
                .map(this::convertToBorrowRecordResponse)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteBorrowRecord(Long id) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
                
        // 只有管理员可以删除，且只能删除已完成或已拒绝的记录
        if (record.getStatus() == BorrowStatus.PENDING || record.getStatus() == BorrowStatus.APPROVED || 
            record.getStatus() == BorrowStatus.BORROWED) {
            throw new RuntimeException("无法删除进行中的借用记录");
        }
        
        record.setDeleted(true);
        borrowRecordRepository.save(record);
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] exportBorrowRecords(BorrowStatus status, String startDate, String endDate) {
        try {
            System.out.println("开始导出Excel文件...");
            System.out.println("参数 - status: " + status + ", startDate: " + startDate + ", endDate: " + endDate);
            
            List<BorrowRecord> records = getRecordsForExport(status, startDate, endDate);
            System.out.println("获取到记录数量: " + (records != null ? records.size() : "null"));
            
            if (records == null || records.isEmpty()) {
                System.out.println("没有找到符合条件的记录，返回空Excel");
                return generateEmptyExcelFile();
            }
            
            byte[] result = generateSimpleExcelFile(records);
            System.out.println("Excel文件生成成功，大小: " + (result != null ? result.length : "null") + " 字节");
            return result;
        } catch (Exception e) {
            System.err.println("导出Excel文件时发生异常:");
            e.printStackTrace();
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "未知错误: " + e.getClass().getSimpleName();
            }
            throw new RuntimeException("导出Excel文件失败: " + errorMessage, e);
        }
    }
    
    private List<BorrowRecord> getRecordsForExport(BorrowStatus status, String startDate, String endDate) {
        try {
            System.out.println("开始获取导出数据...");
            // 使用预加载查询获取数据，避免懒加载问题
            List<BorrowRecord> allRecords;
            
            // 先获取基础数据（带预加载）
            if (status != null) {
                System.out.println("按状态查询: " + status);
                // 按状态查询（使用分页但获取所有数据）
                Page<BorrowRecord> page = borrowRecordRepository.findByStatusAndDeletedFalseWithDetails(
                    status, org.springframework.data.domain.Pageable.unpaged());
                allRecords = new java.util.ArrayList<>(page.getContent()); // 创建可修改的副本
                System.out.println("状态查询结果数量: " + allRecords.size());
            } else {
                System.out.println("查询所有记录...");
                // 获取所有数据（使用分页但获取所有数据）
                Page<BorrowRecord> page = borrowRecordRepository.findByDeletedFalseWithDetails(
                    org.springframework.data.domain.Pageable.unpaged());
                allRecords = new java.util.ArrayList<>(page.getContent()); // 创建可修改的副本
                System.out.println("全量查询结果数量: " + allRecords.size());
            }
            
            // 在内存中过滤时间范围（如果需要）
            if (startDate != null && endDate != null) {
                System.out.println("应用时间过滤: " + startDate + " 到 " + endDate);
                LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                
                int originalSize = allRecords.size();
                allRecords = allRecords.stream()
                    .filter(record -> record.getCreatedAt() != null && 
                                   !record.getCreatedAt().isBefore(start) && 
                                   !record.getCreatedAt().isAfter(end))
                    .collect(java.util.stream.Collectors.toList());
                System.out.println("时间过滤后数量: " + allRecords.size() + " (原始: " + originalSize + ")");
            }
            
            // 按创建时间倒序排序
            allRecords.sort((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            
            System.out.println("最终返回记录数量: " + allRecords.size());
            return allRecords;
        } catch (Exception e) {
            System.err.println("获取导出数据时发生异常:");
            e.printStackTrace();
            throw e;
        }
    }
    
    private byte[] generateSimpleExcelFile(List<BorrowRecord> records) throws Exception {
        System.out.println("===== 开始生成Excel文件 =====");
        System.out.println("记录数量: " + records.size());
        
        if (records.isEmpty()) {
            System.out.println("记录为空，生成空Excel文件");
            return generateEmptyExcelFile();
        }
        
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            System.out.println("创建工作簿和工作表...");
            Sheet sheet = workbook.createSheet("BorrowRecords");
            
            // 创建标题行
            System.out.println("创建标题行...");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("User");
            headerRow.createCell(2).setCellValue("Equipment");
            headerRow.createCell(3).setCellValue("Quantity");
            headerRow.createCell(4).setCellValue("Status");
            headerRow.createCell(5).setCellValue("CreatedAt");
            
            // 填充数据
            System.out.println("开始填充数据...");
            int rowNum = 1;
            for (int i = 0; i < records.size(); i++) {
                BorrowRecord record = records.get(i);
                System.out.println("处理第 " + (i + 1) + " 条记录，ID: " + (record != null ? record.getId() : "null"));
                
                try {
                    Row row = sheet.createRow(rowNum++);
                    
                    // 安全地设置每个单元格
                    String id = record.getId() != null ? record.getId().toString() : "N/A";
                    row.createCell(0).setCellValue(id);
                    
                    String userName = "N/A";
                    try {
                        if (record.getUser() != null && record.getUser().getRealName() != null) {
                            userName = record.getUser().getRealName();
                        }
                    } catch (Exception e) {
                        System.out.println("获取用户名失败: " + e.getMessage());
                    }
                    row.createCell(1).setCellValue(userName);
                    
                    String equipmentName = "N/A";
                    try {
                        if (record.getEquipment() != null && record.getEquipment().getName() != null) {
                            equipmentName = record.getEquipment().getName();
                        }
                    } catch (Exception e) {
                        System.out.println("获取设备名失败: " + e.getMessage());
                    }
                    row.createCell(2).setCellValue(equipmentName);
                    
                    String quantity = record.getQuantity() != null ? record.getQuantity().toString() : "0";
                    row.createCell(3).setCellValue(quantity);
                    
                    String status = "N/A";
                    try {
                        status = getStatusText(record.getStatus());
                    } catch (Exception e) {
                        System.out.println("获取状态失败: " + e.getMessage());
                    }
                    row.createCell(4).setCellValue(status);
                    
                    String createdAt = record.getCreatedAt() != null ? record.getCreatedAt().toString() : "N/A";
                    row.createCell(5).setCellValue(createdAt);
                    
                    System.out.println("第 " + (i + 1) + " 条记录处理完成");
                } catch (Exception e) {
                    System.err.println("处理第 " + (i + 1) + " 条记录时出错: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理下一条记录
                }
            }
            
            System.out.println("数据填充完成，调整列宽...");
            // 自动调整列宽
            for (int i = 0; i < 6; i++) {
                try {
                    sheet.autoSizeColumn(i);
                } catch (Exception e) {
                    System.out.println("调整第 " + i + " 列宽度失败: " + e.getMessage());
                }
            }
            
            System.out.println("写入输出流...");
            workbook.write(outputStream);
            outputStream.flush();
            
            byte[] result = outputStream.toByteArray();
            System.out.println("===== Excel文件生成完成 =====");
            System.out.println("最终文件大小: " + result.length + " 字节");
            
            if (result.length < 1000) {
                System.err.println("警告：文件大小异常小，可能生成失败！");
                // 尝试生成一个最简单的Excel文件
                return generateBasicExcelFile();
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("生成Excel文件时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private byte[] generateExcelFile(List<BorrowRecord> records) throws Exception {
        System.out.println("开始生成Excel文件，记录数量: " + records.size());
        
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("借用记录");
            
            // 创建样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"记录ID", "申请人", "部门", "设备名称", "设备编号", "借用数量", 
                               "申请时间", "预期归还时间", "状态", "借用原因", "审批人", "审批意见", 
                               "实际归还时间", "归还备注", "损坏描述"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 时间格式化
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            // 填充数据
            int rowNum = 1;
            for (BorrowRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                
                // 安全地设置单元格值
                setCellValue(row, 0, record.getId());
                setCellValue(row, 1, record.getUser() != null ? record.getUser().getRealName() : "");
                setCellValue(row, 2, record.getUser() != null && record.getUser().getDepartment() != null ? 
                    record.getUser().getDepartment().getName() : "");
                setCellValue(row, 3, record.getEquipment() != null ? record.getEquipment().getName() : "");
                setCellValue(row, 4, record.getEquipment() != null ? 
                    (record.getEquipment().getSerialNumber() != null ? record.getEquipment().getSerialNumber() : "") : "");
                setCellValue(row, 5, record.getQuantity() != null ? record.getQuantity() : 0);
                setCellValue(row, 6, record.getCreatedAt() != null ? record.getCreatedAt().format(formatter) : "");
                setCellValue(row, 7, record.getExpectedReturnTime() != null ? record.getExpectedReturnTime().format(formatter) : "");
                setCellValue(row, 8, getStatusText(record.getStatus()));
                setCellValue(row, 9, record.getBorrowReason() != null ? record.getBorrowReason() : "");
                setCellValue(row, 10, record.getApprovedBy() != null ? record.getApprovedBy().getRealName() : "");
                setCellValue(row, 11, record.getApprovalNotes() != null ? record.getApprovalNotes() : "");
                setCellValue(row, 12, record.getActualReturnTime() != null ? record.getActualReturnTime().format(formatter) : "");
                setCellValue(row, 13, record.getReturnNotes() != null ? record.getReturnNotes() : "");
                setCellValue(row, 14, record.getDamageDescription() != null ? record.getDamageDescription() : "");
            }
            
            // 自动调整列宽（限制最大宽度）
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最大列宽，避免过宽
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth > 6000) {
                    sheet.setColumnWidth(i, 6000);
                }
            }
            
            workbook.write(outputStream);
            outputStream.flush();
            
            byte[] result = outputStream.toByteArray();
            System.out.println("Excel文件生成完成，字节数: " + result.length);
            return result;
        }
    }
    
    private void setCellValue(Row row, int columnIndex, Object value) {
        Cell cell = row.createCell(columnIndex);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    private byte[] generateBasicExcelFile() throws Exception {
        System.out.println("生成最基本的Excel文件...");
        
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Test");
            
            // 只创建一个简单的标题行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Test");
            headerRow.createCell(1).setCellValue("Data");
            
            // 添加一行测试数据
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Sample");
            dataRow.createCell(1).setCellValue("Value");
            
            workbook.write(outputStream);
            
            byte[] result = outputStream.toByteArray();
            System.out.println("基本Excel文件生成完成，字节数: " + result.length);
            return result;
        }
    }
    
    private byte[] generateEmptyExcelFile() throws Exception {
        System.out.println("生成空Excel文件...");
        
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("BorrowRecords");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "User", "Equipment", "Quantity", "Status", "CreatedAt"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 添加一行提示信息
            Row noDataRow = sheet.createRow(1);
            Cell noDataCell = noDataRow.createCell(0);
            noDataCell.setCellValue("No records found");
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            outputStream.flush();
            
            byte[] result = outputStream.toByteArray();
            System.out.println("空Excel文件生成完成，字节数: " + result.length);
            return result;
        }
    }
    
    private String getStatusText(BorrowStatus status) {
        switch (status) {
            case PENDING: return "待审核";
            case APPROVED: return "已批准";
            case REJECTED: return "已拒绝";
            case BORROWED: return "已借出";
            case RETURNED: return "已归还";
            default: return status.toString();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecord> getOverdueRecords() {
        return borrowRecordRepository.findOverdueRecords(LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> getOverdueRecordsResponse(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        // 使用 findOverdueRecords 的结果并转换为分页
        List<BorrowRecord> allOverdueRecords = borrowRecordRepository.findOverdueRecords(now);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allOverdueRecords.size());
        List<BorrowRecord> pageContent = allOverdueRecords.subList(start, end);
        
        Page<BorrowRecord> page = new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            allOverdueRecords.size()
        );
        
        return page.map(this::convertToBorrowRecordResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecord> findByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return borrowRecordRepository.findByDateRange(startTime, endTime);
    }
    

    
    @Override
    @Transactional(readOnly = true)
    public BorrowStatistics getBorrowStatistics() {
        long totalRecords = borrowRecordRepository.countByDeletedFalse();
        long pendingRequests = borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.PENDING);
        long approvedRecords = borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.APPROVED);
        long rejectedRecords = borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.REJECTED);
        long borrowedRecords = borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.BORROWED);
        long returnedRecords = borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.RETURNED);
        long overdueRecords = getOverdueRecords().size();
        
        return new BorrowStatistics(
            totalRecords, pendingRequests, approvedRecords, rejectedRecords,
            borrowedRecords, returnedRecords, overdueRecords
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserBorrowStatistics getUserBorrowStatistics(Long userId) {
        long totalBorrows = borrowRecordRepository.countByUser_IdAndDeletedFalse(userId);
        long currentBorrows = borrowRecordRepository.countByUser_IdAndStatusAndDeletedFalse(userId, BorrowStatus.BORROWED);
        long returnedBorrows = borrowRecordRepository.countByUser_IdAndStatusAndDeletedFalse(userId, BorrowStatus.RETURNED);
        long rejectedRequests = borrowRecordRepository.countByUser_IdAndStatusAndDeletedFalse(userId, BorrowStatus.REJECTED);
        
        return new UserBorrowStatistics(totalBorrows, currentBorrows, returnedBorrows, rejectedRequests);
    }
    
    /**
     * 将BorrowRecord转换为BorrowRecordResponse
     */
    private BorrowRecordResponse convertToBorrowRecordResponse(BorrowRecord record) {
        BorrowRecordResponse response = new BorrowRecordResponse();
        
        response.setId(record.getId());
        response.setQuantity(record.getQuantity());
        response.setExpectedReturnTime(record.getExpectedReturnTime());
        response.setActualReturnTime(record.getActualReturnTime());
        response.setStatus(record.getStatus());
        response.setPurpose(record.getBorrowReason()); // 映射borrowReason到purpose
        response.setBorrowReason(record.getBorrowReason());
        response.setApprovalNotes(record.getApprovalNotes());
        response.setApprovalTime(record.getApprovalTime());
        response.setReturnNotes(record.getReturnNotes());
        response.setDamageDescription(record.getDamageDescription());
        response.setReturnImages(record.getReturnImages());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        
        // 创建用户信息对象
        if (record.getUser() != null) {
            BorrowRecordResponse.UserInfo userInfo = new BorrowRecordResponse.UserInfo(
                record.getUser().getId(),
                record.getUser().getUsername(),
                record.getUser().getRealName(),
                record.getUser().getDepartment() != null ? record.getUser().getDepartment().getName() : null
            );
            response.setUser(userInfo);
        }
        
        // 创建设备信息对象
        if (record.getEquipment() != null) {
            BorrowRecordResponse.EquipmentInfo equipmentInfo = new BorrowRecordResponse.EquipmentInfo(
                record.getEquipment().getId(),
                record.getEquipment().getName(),
                record.getEquipment().getCategoryDisplayName(),
                record.getEquipment().getSerialNumber(),
                record.getEquipment().getImageUrl(),
                record.getEquipment().getSpecifications()
            );
            response.setEquipment(equipmentInfo);
        }
        
        // 安全获取审核人信息
        if (record.getApprovedBy() != null) {
            response.setApprovedByName(record.getApprovedBy().getRealName());
        }
        
        return response;
    }
    
    /**
     * 检查设备是否损坏
     * 优先检查用户明确选择的状态，然后通过检查归还备注和损坏描述中的关键词来判断
     */
    private boolean isEquipmentDamaged(String condition, String returnNotes, String damageDescription) {
        // 首先检查用户明确选择的设备状态
        if ("damaged".equals(condition)) {
            System.out.println("✓ 用户明确选择了设备有损坏，condition: " + condition);
            return true;
        }
        
        System.out.println(">>> 开始关键词检测，condition: " + condition + 
                         ", returnNotes: " + returnNotes + 
                         ", damageDescription: " + damageDescription);
        
        // 损坏相关关键词
        String[] damageKeywords = {
            "损坏", "坏", "破损", "故障", "异常", "问题", "不正常", "失灵", "无法使用",
            "维修", "修理", "报修", "送修", "需要修", "要修", "修复",
            "裂", "摔", "碎", "断", "卡", "不能", "无法", "打不开", "关不了"
        };
        
        // 检查归还备注
        if (returnNotes != null && !returnNotes.trim().isEmpty()) {
            String notes = returnNotes.toLowerCase();
            System.out.println("检查归还备注中的关键词: " + notes);
            for (String keyword : damageKeywords) {
                if (notes.contains(keyword)) {
                    System.out.println("✓ 在归还备注中发现损坏关键词: " + keyword);
                    return true;
                }
            }
        }
        
        // 检查损坏描述
        if (damageDescription != null && !damageDescription.trim().isEmpty()) {
            String description = damageDescription.toLowerCase();
            System.out.println("检查损坏描述中的关键词: " + description);
            for (String keyword : damageKeywords) {
                if (description.contains(keyword)) {
                    System.out.println("✓ 在损坏描述中发现损坏关键词: " + keyword);
                    return true;
                }
            }
        }
        
        System.out.println("✗ 未检测到设备损坏（既没有明确选择损坏状态，也没有发现损坏关键词）");
        return false;
    }
    
    /**
     * 创建设备维修通知公告（异步）
     */
    private void createMaintenanceNotificationAsync(BorrowRecord record) {
        // 在新的线程中执行，避免影响主事务
        new Thread(() -> {
            try {
                // 短暂延迟，确保主事务已提交
                Thread.sleep(500);
                createMaintenanceNotification(record);
            } catch (Exception e) {
                System.err.println("异步创建维修通知公告失败: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * 创建设备维修通知公告
     */
    private void createMaintenanceNotification(BorrowRecord record) {
        try {
            String title = "设备维修通知 - " + record.getEquipment().getName();
            
            StringBuilder content = new StringBuilder();
            content.append("系统检测到设备可能需要维修，详情如下：\n\n");
            content.append("设备信息：\n");
            content.append("- 设备名称：").append(record.getEquipment().getName()).append("\n");
            content.append("- 设备分类：").append(record.getEquipment().getCategoryDisplayName()).append("\n");
            content.append("- 设备编号：").append(record.getEquipment().getSerialNumber()).append("\n\n");
            
            content.append("归还信息：\n");
            content.append("- 归还用户：").append(record.getUser().getRealName()).append("\n");
            content.append("- 归还时间：").append(record.getActualReturnTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
            
            if (record.getReturnNotes() != null && !record.getReturnNotes().trim().isEmpty()) {
                content.append("- 归还备注：").append(record.getReturnNotes()).append("\n");
            }
            
            if (record.getDamageDescription() != null && !record.getDamageDescription().trim().isEmpty()) {
                content.append("- 损坏描述：").append(record.getDamageDescription()).append("\n");
            }
            
            content.append("\n请管理员及时检查设备状态并安排维修。");
            
            // 创建公告
            announcementService.createSystemAnnouncement(title, content.toString());
            
        } catch (Exception e) {
            // 记录错误但不影响主流程
            System.err.println("创建维修通知公告失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    @Transactional
    public void cancelBorrowRequest(Long id, String username) {
        BorrowRecord record = borrowRecordRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        
        // 检查是否为当前用户的申请
        if (!record.getUser().getUsername().equals(username)) {
            throw new RuntimeException("只能取消自己的借用申请");
        }
        
        // 只有待审核状态的申请可以取消
        if (record.getStatus() != BorrowStatus.PENDING) {
            throw new RuntimeException("只有待审核状态的申请可以取消");
        }
        
        // 恢复设备库存（申请时已经预留）
        if (record.getEquipment() != null) {
            Equipment equipment = record.getEquipment();
            equipment.setAvailableQuantity(equipment.getAvailableQuantity() + record.getQuantity());
            equipmentRepository.save(equipment);
        }
        
        // 软删除记录
        record.setDeleted(true);
        borrowRecordRepository.save(record);
    }
    
    @Override
    @Transactional
    public void physicalDeleteBorrowRecord(Long id) {
        System.out.println(">>> 开始物理删除借用记录 ID: " + id);
        
        Optional<BorrowRecord> recordOpt = borrowRecordRepository.findById(id);
        
        if (recordOpt.isEmpty()) {
            System.err.println("借用记录 " + id + " 不存在，可能已经被删除");
            return; // 记录不存在，可能已经被删除，直接返回
        }
        
        BorrowRecord record = recordOpt.get();
        System.out.println("找到借用记录: " + record.getId() + ", 设备: " + (record.getEquipment() != null ? record.getEquipment().getName() : "未知"));
        
        // 只能物理删除已软删除的记录
        if (!record.getDeleted()) {
            throw new RuntimeException("只能永久删除已软删除的记录");
        }
        
        System.out.println("记录已确认为软删除状态，开始删除归还图片文件...");
        
        // 删除归还图片文件
        deleteReturnImageFiles(record);
        
        System.out.println("归还图片文件处理完成，开始物理删除数据库记录...");
        
        // 物理删除记录
        borrowRecordRepository.delete(record);
        
        System.out.println("<<< 完成物理删除借用记录 ID: " + id);
    }
    
    @Override
    @Transactional
    public void physicalDeleteBorrowRecordsByIds(List<Long> ids) {
        for (Long id : ids) {
            try {
                physicalDeleteBorrowRecord(id);
            } catch (Exception e) {
                // 记录错误但继续处理其他记录
                System.err.println("删除记录 " + id + " 失败: " + e.getMessage());
            }
        }
    }
    
    @Override
    public Map<String, Object> getDeletedRecordsStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalDeleted = borrowRecordRepository.countByDeletedTrue();
        stats.put("totalDeletedRecords", totalDeleted);
        
        // 计算不同时间段的已删除记录数量
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1);
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime threeMonthsAgo = now.minusMonths(3);
        
        long deletedLastWeek = borrowRecordRepository.countDeletedRecordsOlderThan(oneWeekAgo);
        long deletedLastMonth = borrowRecordRepository.countDeletedRecordsOlderThan(oneMonthAgo);
        long deletedThreeMonthsAgo = borrowRecordRepository.countDeletedRecordsOlderThan(threeMonthsAgo);
        
        stats.put("deletedLastWeek", deletedLastWeek);
        stats.put("deletedLastMonth", deletedLastMonth);
        stats.put("deletedThreeMonthsAgo", deletedThreeMonthsAgo);
        
        // 估算可释放的存储空间（包括归还图片）
        long estimatedSize = calculateEstimatedStorageSize(totalDeleted);
        stats.put("estimatedSizeKB", estimatedSize);
        
        return stats;
    }
    
    @Override
    public Page<DeletedBorrowRecordResponse> findDeletedRecords(Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByDeletedTrue(pageable);
        return records.map(this::convertToDeletedRecordResponse);
    }
    
    @Override
    @Transactional
    public int cleanupDeletedRecords(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<BorrowRecord> oldDeletedRecords = borrowRecordRepository.findDeletedRecordsOlderThan(cutoffDate);
        
        int deletedCount = 0;
        for (BorrowRecord record : oldDeletedRecords) {
            try {
                // 删除归还图片文件
                deleteReturnImageFiles(record);
                
                // 物理删除记录
                borrowRecordRepository.delete(record);
                deletedCount++;
            } catch (Exception e) {
                System.err.println("清理记录 " + record.getId() + " 失败: " + e.getMessage());
            }
        }
        
        return deletedCount;
    }
    
    /**
     * 转换为删除记录响应DTO
     * 避免Hibernate懒加载代理序列化问题
     */
    private DeletedBorrowRecordResponse convertToDeletedRecordResponse(BorrowRecord record) {
        // 安全获取设备信息
        String equipmentName = null;
        String equipmentSerial = null;
        if (record.getEquipment() != null) {
            equipmentName = record.getEquipment().getName();
            equipmentSerial = record.getEquipment().getSerialNumber();
        }
        
        // 安全获取用户信息
        String username = null;
        String realName = null;
        if (record.getUser() != null) {
            username = record.getUser().getUsername();
            realName = record.getUser().getRealName();
        }
        
        // 获取状态信息
        String status = record.getStatus() != null ? record.getStatus().toString() : null;
        
        return new DeletedBorrowRecordResponse(
            record.getId(),
            equipmentName,
            equipmentSerial,
            username,
            realName,
            status,
            record.getUpdatedAt()
        );
    }
    
    /**
     * 删除借用记录相关的归还图片文件
     * @param record 借用记录
     */
    private void deleteReturnImageFiles(BorrowRecord record) {
        System.out.println("=== 开始删除借用记录 " + record.getId() + " 的归还图片文件 ===");
        
        if (record.getReturnImages() == null || record.getReturnImages().trim().isEmpty()) {
            System.out.println("借用记录 " + record.getId() + " 没有归还图片，跳过删除");
            return; // 没有归还图片，直接返回
        }
        
        System.out.println("归还图片JSON数据: " + record.getReturnImages());
        
        try {
            // 解析归还图片JSON字符串
            List<String> imageUrls = objectMapper.readValue(
                record.getReturnImages(), 
                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
            );
            
            System.out.println("解析到 " + imageUrls.size() + " 个图片URL: " + imageUrls);
            
            for (String imageUrl : imageUrls) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    System.out.println("准备删除图片文件: " + imageUrl);
                    deleteImageFile(imageUrl);
                } else {
                    System.out.println("跳过空的图片URL");
                }
            }
            
            System.out.println("成功处理借用记录 " + record.getId() + " 的 " + imageUrls.size() + " 个归还图片文件");
            
        } catch (Exception e) {
            System.err.println("删除借用记录 " + record.getId() + " 的归还图片文件失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 结束删除借用记录 " + record.getId() + " 的归还图片文件 ===");
    }
    
    /**
     * 删除单个图片文件
     * @param imageUrl 图片URL路径
     */
    private void deleteImageFile(String imageUrl) {
        try {
            System.out.println("处理图片URL: " + imageUrl);
            
            // 处理相对路径（如 "/uploads/returns/return_123_456.jpg"）
            String relativePath = imageUrl;
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1); // 移除开头的斜杠
            }
            
            System.out.println("转换后的相对路径: " + relativePath);
            
            // 构建完整的文件路径
            java.nio.file.Path filePath = java.nio.file.Paths.get(relativePath);
            System.out.println("完整文件路径: " + filePath.toAbsolutePath());
            
            if (java.nio.file.Files.exists(filePath)) {
                java.nio.file.Files.delete(filePath);
                System.out.println("✓ 成功删除归还图片文件: " + filePath);
            } else {
                System.out.println("✗ 归还图片文件不存在，跳过删除: " + filePath.toAbsolutePath());
                
                // 尝试其他可能的路径
                java.nio.file.Path alternativePath = java.nio.file.Paths.get("." + imageUrl);
                System.out.println("尝试备选路径: " + alternativePath.toAbsolutePath());
                if (java.nio.file.Files.exists(alternativePath)) {
                    java.nio.file.Files.delete(alternativePath);
                    System.out.println("✓ 使用备选路径成功删除文件: " + alternativePath);
                } else {
                    System.out.println("✗ 备选路径也不存在: " + alternativePath.toAbsolutePath());
                }
            }
            
        } catch (Exception e) {
            System.err.println("删除归还图片文件失败: " + imageUrl + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 计算已删除记录的预估存储空间（包括归还图片）
     * @param totalDeletedRecords 已删除记录总数
     * @return 预估存储空间大小（KB）
     */
    private long calculateEstimatedStorageSize(long totalDeletedRecords) {
        // 基础记录数据大小：假设每条记录平均2KB
        long recordDataSize = totalDeletedRecords * 2;
        
        // 计算归还图片文件大小
        long imageFileSize = 0;
        try {
            List<BorrowRecord> deletedRecords = borrowRecordRepository.findByDeletedTrue(
                org.springframework.data.domain.PageRequest.of(0, (int)totalDeletedRecords)
            ).getContent();
            
            for (BorrowRecord record : deletedRecords) {
                imageFileSize += estimateReturnImageSize(record);
            }
            
        } catch (Exception e) {
            System.err.println("计算归还图片大小失败: " + e.getMessage());
            // 如果计算失败，使用平均估算：假设30%的记录有归还图片，每张图片平均500KB
            imageFileSize = (long)(totalDeletedRecords * 0.3 * 2 * 500); // 30%记录，平均2张图片，每张500KB
        }
        
        return recordDataSize + imageFileSize;
    }
    
    /**
     * 估算单个记录的归还图片大小
     * @param record 借用记录
     * @return 图片文件大小（KB）
     */
    private long estimateReturnImageSize(BorrowRecord record) {
        if (record.getReturnImages() == null || record.getReturnImages().trim().isEmpty()) {
            return 0;
        }
        
        try {
            List<String> imageUrls = objectMapper.readValue(
                record.getReturnImages(), 
                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
            );
            
            long totalSize = 0;
            for (String imageUrl : imageUrls) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    long fileSize = getImageFileSize(imageUrl);
                    if (fileSize > 0) {
                        totalSize += fileSize;
                    } else {
                        // 如果无法获取实际大小，使用平均估算
                        totalSize += 500; // 500KB per image
                    }
                }
            }
            
            return totalSize;
            
        } catch (Exception e) {
            // 如果解析失败，使用默认估算
            return 1000; // 假设有归还图片的记录平均1MB
        }
    }
    
    /**
     * 获取图片文件实际大小
     * @param imageUrl 图片URL
     * @return 文件大小（KB），如果文件不存在或获取失败返回0
     */
    private long getImageFileSize(String imageUrl) {
        try {
            String relativePath = imageUrl;
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            
            java.nio.file.Path filePath = java.nio.file.Paths.get(relativePath);
            
            if (java.nio.file.Files.exists(filePath)) {
                long sizeBytes = java.nio.file.Files.size(filePath);
                return sizeBytes / 1024; // 转换为KB
            }
            
        } catch (Exception e) {
            // 忽略错误，返回0
        }
        
        return 0;
    }
    
    /**
     * 更新设备状态为"损坏"
     * @param equipmentId 设备ID
     */
    private void updateEquipmentStatusToDamaged(Long equipmentId) {
        try {
            System.out.println(">>> 开始更新设备状态为损坏，设备ID: " + equipmentId);
            
            // 通过设备服务更新设备状态
            equipmentService.updateEquipmentStatus(equipmentId, "损坏");
            
            System.out.println("✓ 设备状态已更新为损坏，设备ID: " + equipmentId);
            
        } catch (Exception e) {
            System.err.println("✗ 更新设备状态失败，设备ID: " + equipmentId + "，错误: " + e.getMessage());
            // 不抛出异常，避免影响归还流程
        }
    }
}
