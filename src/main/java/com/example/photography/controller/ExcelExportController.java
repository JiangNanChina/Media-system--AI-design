package com.example.photography.controller;

import com.example.photography.service.ExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Excel导出控制器
 */
@RestController
@RequestMapping("/export")
@Tag(name = "Excel导出", description = "各种数据的Excel导出功能")
public class ExcelExportController {
    
    @Autowired
    private ExcelExportService excelExportService;
    
    /**
     * 导出打卡记录
     */
    @GetMapping("/checkin-records")
    @Operation(summary = "导出打卡记录", description = "导出用户的打卡记录到Excel")
    public ResponseEntity<byte[]> exportCheckinRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) {
        
        try {
            // 如果没有指定用户ID，则导出当前用户的记录（非管理员）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && userId == null) {
                userId = (Long) authentication.getDetails();
            }
            
            // 默认导出最近30天的记录
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            byte[] excelData = excelExportService.exportCheckinRecords(userId, startDate, endDate, status);
            
            String filename = String.format("打卡记录_%s_%s.xlsx", 
                    startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            return createExcelResponse(excelData, filename);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出借还记录
     */
    @GetMapping("/borrow-records")
    @Operation(summary = "导出借还记录", description = "导出借还记录到Excel")
    public ResponseEntity<byte[]> exportBorrowRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            // 非管理员只能导出自己的记录
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                userId = (Long) authentication.getDetails();
            }
            
            byte[] excelData = excelExportService.exportBorrowRecords(userId, status, startDate, endDate);
            
            String filename = String.format("借还记录_%s.xlsx", 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            return createExcelResponse(excelData, filename);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出设备清单
     */
    @GetMapping("/equipment-list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出设备清单", description = "导出设备清单到Excel（仅管理员）")
    public ResponseEntity<byte[]> exportEquipmentList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        
        try {
            byte[] excelData = excelExportService.exportEquipmentList(category, status);
            
            String filename = String.format("设备清单_%s.xlsx", 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            return createExcelResponse(excelData, filename);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出用户列表
     */
    @GetMapping("/user-list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出用户列表", description = "导出用户列表到Excel（仅管理员）")
    public ResponseEntity<byte[]> exportUserList(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean isActive) {
        
        try {
            byte[] excelData = excelExportService.exportUserList(department, isActive);
            
            String filename = String.format("用户列表_%s.xlsx", 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            return createExcelResponse(excelData, filename);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出请假记录
     */
    @GetMapping("/leave-requests")
    @Operation(summary = "导出请假记录", description = "导出请假记录到Excel")
    public ResponseEntity<byte[]> exportLeaveRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            byte[] excelData = excelExportService.exportLeaveRequests(keyword, status, leaveType, startDate, endDate);
            
            String filename = String.format("请假记录_%s.xlsx", 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            return createExcelResponse(excelData, filename);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出执勤记录
     */
    @GetMapping("/duty-records")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出执勤记录", description = "导出执勤记录到Excel（仅管理员）")
    public ResponseEntity<byte[]> exportDutyRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) {
        
        try {
            // 默认导出最近30天的记录
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            byte[] excelData = excelExportService.exportDutyRecords(startDate, endDate, status);
            
            String filename = String.format("执勤记录_%s_%s.xlsx", 
                    startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            return createExcelResponse(excelData, filename);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出打卡统计报表
     */
    @GetMapping("/checkin-statistics")
    @Operation(summary = "导出打卡统计", description = "导出打卡统计报表到Excel")
    public ResponseEntity<byte[]> exportCheckinStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            // 非管理员只能导出自己的统计
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && userId == null) {
                userId = (Long) authentication.getDetails();
            }
            
            // 默认统计最近30天
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            byte[] excelData = excelExportService.exportCheckinStatistics(userId, startDate, endDate);
            
            String filename = String.format("打卡统计_%s_%s.xlsx", 
                    startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            return createExcelResponse(excelData, filename);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出数据导入模板
     */
    @GetMapping("/template/{templateType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出导入模板", description = "导出数据导入模板（仅管理员）")
    public ResponseEntity<byte[]> exportTemplate(@PathVariable String templateType) {
        
        try {
            byte[] excelData = excelExportService.exportTemplate(templateType);
            
            String filename = String.format("%s导入模板.xlsx", templateType);
            
            return createExcelResponse(excelData, filename);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 创建Excel响应
     */
    private ResponseEntity<byte[]> createExcelResponse(byte[] excelData, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelData.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
