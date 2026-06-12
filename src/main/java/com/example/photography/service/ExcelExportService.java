package com.example.photography.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Excel导出服务接口
 */
public interface ExcelExportService {
    
    /**
     * 导出打卡记录
     */
    byte[] exportCheckinRecords(Long userId, LocalDate startDate, LocalDate endDate, String status);
    
    /**
     * 导出借还记录
     */
    byte[] exportBorrowRecords(Long userId, String status, LocalDate startDate, LocalDate endDate);
    
    /**
     * 导出设备清单
     */
    byte[] exportEquipmentList(String category, String status);
    
    /**
     * 导出用户列表
     */
    byte[] exportUserList(String department, Boolean isActive);
    
    /**
     * 导出请假记录
     */
    byte[] exportLeaveRequests(String keyword, String status, String leaveType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 导出执勤记录
     */
    byte[] exportDutyRecords(LocalDate startDate, LocalDate endDate, String status);
    
    /**
     * 导出打卡统计报表
     */
    byte[] exportCheckinStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 通用导出方法
     */
    byte[] exportToExcel(String sheetName, List<String> headers, List<List<Object>> data);
    
    /**
     * 导出多表格Excel
     */
    byte[] exportMultipleSheets(Map<String, Object> sheetsData);
    
    /**
     * 导出模板文件
     */
    byte[] exportTemplate(String templateType);
}
