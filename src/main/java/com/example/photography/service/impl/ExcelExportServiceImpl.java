package com.example.photography.service.impl;

import com.example.photography.model.entity.*;
import com.example.photography.repository.*;
import com.example.photography.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel导出服务实现类
 */
@Service
@Transactional(readOnly = true)
public class ExcelExportServiceImpl implements ExcelExportService {
    
    @Autowired
    private CheckinRecordRepository checkinRecordRepository;
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private DutyRecordRepository dutyRecordRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public byte[] exportCheckinRecords(Long userId, LocalDate startDate, LocalDate endDate, String status) {
        List<CheckinRecord> records;
        
        if (userId != null) {
            records = checkinRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            records = checkinRecordRepository.findByDateRange(startDate, endDate);
        }
        
        if (status != null && !status.isEmpty()) {
            records = records.stream()
                    .filter(record -> status.equals(record.getStatus()))
                    .collect(Collectors.toList());
        }
        
        List<String> headers = Arrays.asList(
                "序号", "姓名", "所属部门", "打卡地点", "时间段", "签到时间", "签退时间", 
                "状态", "学习时长(分钟)", "签到地址", "备注"
        );
        
        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        for (CheckinRecord record : records) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(record.getUser() != null ? record.getUser().getRealName() : "");
            row.add(record.getUser() != null && record.getUser().getDepartment() != null ? 
                    record.getUser().getDepartment().getName() : "");
            row.add(record.getConfiguration() != null ? record.getConfiguration().getLocationName() : "");
            row.add(record.getConfiguration() != null ? record.getConfiguration().getSessionName() : "");
            row.add(record.getCheckinTime() != null ? 
                    record.getCheckinTime().format(DATETIME_FORMATTER) : "");
            row.add(record.getCheckoutTime() != null ? 
                    record.getCheckoutTime().format(DATETIME_FORMATTER) : "");
            row.add(record.getStatus() != null ? record.getStatus() : "");
            row.add(record.getDurationMinutes() != null ? record.getDurationMinutes() : 0);
            row.add(record.getCheckinAddress() != null ? record.getCheckinAddress() : "");
            row.add(record.getNotes() != null ? record.getNotes() : "");
            data.add(row);
        }
        
        return exportToExcel("打卡记录", headers, data);
    }
    
    @Override
    public byte[] exportBorrowRecords(Long userId, String status, LocalDate startDate, LocalDate endDate) {
        List<BorrowRecord> records = borrowRecordRepository.findByDeletedFalse(
                Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 过滤条件
        if (userId != null) {
            records = records.stream()
                    .filter(record -> record.getUser() != null && userId.equals(record.getUser().getId()))
                    .collect(Collectors.toList());
        }
        
        if (status != null && !status.isEmpty()) {
            records = records.stream()
                    .filter(record -> status.equals(record.getStatus()))
                    .collect(Collectors.toList());
        }
        
        if (startDate != null && endDate != null) {
            records = records.stream()
                    .filter(record -> {
                        LocalDate createdDate = record.getCreatedAt().toLocalDate();
                        return !createdDate.isBefore(startDate) && !createdDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());
        }
        
        List<String> headers = Arrays.asList(
                "序号", "借用类型", "代办成员", "所属部门", "外部对象类型", "外部单位", "联系人", "手机号", "QQ邮箱",
                "设备名称", "设备序列号", "数量",
                "借用时间", "预期归还时间", "实际归还时间", "状态", "借用原因", "归还备注"
        );
        
        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        for (BorrowRecord record : records) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(record.getBorrowerType() == com.example.photography.model.enums.BorrowerType.EXTERNAL ? "外部借用" : "内部借用");
            row.add(record.getUser() != null ? record.getUser().getRealName() : "");
            row.add(record.getUser() != null && record.getUser().getDepartment() != null ? 
                    record.getUser().getDepartment().getName() : "");
            row.add(externalBorrowerTypeName(record.getExternalBorrowerType()));
            row.add(Objects.toString(record.getExternalOrganization(), ""));
            row.add(Objects.toString(record.getExternalContactName(), ""));
            row.add(Objects.toString(record.getExternalPhone(), ""));
            row.add(Objects.toString(record.getExternalEmail(), ""));
            row.add(record.getEquipment() != null ? record.getEquipment().getName() : "");
            row.add(record.getEquipment() != null ? record.getEquipment().getSerialNumber() : "");
            row.add(record.getQuantity() != null ? record.getQuantity() : 0);
            row.add(record.getCreatedAt() != null ? 
                    record.getCreatedAt().format(DATETIME_FORMATTER) : "");
            row.add(record.getExpectedReturnTime() != null ? 
                    record.getExpectedReturnTime().format(DATETIME_FORMATTER) : "");
            row.add(record.getActualReturnTime() != null ? 
                    record.getActualReturnTime().format(DATETIME_FORMATTER) : "");
            row.add(record.getStatus() != null ? record.getStatus() : "");
            row.add(record.getBorrowReason() != null ? record.getBorrowReason() : "");
            row.add(record.getReturnNotes() != null ? record.getReturnNotes() : "");
            data.add(row);
        }
        
        return exportToExcel("借还记录", headers, data);
    }

    private String externalBorrowerTypeName(com.example.photography.model.enums.ExternalBorrowerType type) {
        if (type == null) return "";
        return switch (type) {
            case COLLEGE -> "学院";
            case DEPARTMENT -> "部门";
            case TEACHER -> "老师";
        };
    }
    
    @Override
    public byte[] exportEquipmentList(String category, String status) {
        List<Equipment> equipments = equipmentRepository.findByDeletedFalse(
                Sort.by(Sort.Direction.ASC, "name"));
        
        // 过滤条件
        if (category != null && !category.isEmpty()) {
            equipments = equipments.stream()
                    .filter(equipment -> equipment.getCategory() != null && 
                            category.equals(equipment.getCategory().getName()))
                    .collect(Collectors.toList());
        }
        
        if (status != null && !status.isEmpty()) {
            equipments = equipments.stream()
                    .filter(equipment -> status.equals(equipment.getStatus()))
                    .collect(Collectors.toList());
        }
        
        List<String> headers = Arrays.asList(
                "序号", "设备名称", "设备分类", "序列号", "品牌", "型号", "状态", 
                "总数量", "可用数量", "采购日期", "采购价格", "位置", "描述"
        );
        
        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        for (Equipment equipment : equipments) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(equipment.getName() != null ? equipment.getName() : "");
            row.add(equipment.getCategory() != null ? equipment.getCategory().getName() : "");
            row.add(equipment.getSerialNumber() != null ? equipment.getSerialNumber() : "");
            row.add(""); // 品牌 - 暂无此字段
            row.add(""); // 型号 - 暂无此字段
            row.add(equipment.getStatus() != null ? equipment.getStatus() : "");
            row.add(equipment.getStockQuantity() != null ? equipment.getStockQuantity() : 0); // 总数量使用库存数量
            row.add(equipment.getAvailableQuantity() != null ? equipment.getAvailableQuantity() : 0);
            row.add(""); // 采购日期 - 暂无此字段
            row.add(0); // 采购价格 - 暂无此字段
            row.add(""); // 位置 - 暂无此字段
            row.add(equipment.getDescription() != null ? equipment.getDescription() : "");
            data.add(row);
        }
        
        return exportToExcel("设备清单", headers, data);
    }
    
    @Override
    public byte[] exportUserList(String department, Boolean isActive) {
        List<User> users = userRepository.findByDeletedFalse(
                Sort.by(Sort.Direction.ASC, "realName"));
        
        // 过滤条件
        if (department != null && !department.isEmpty()) {
            users = users.stream()
                    .filter(user -> user.getDepartment() != null && 
                            department.equals(user.getDepartment().getName()))
                    .collect(Collectors.toList());
        }
        
        if (isActive != null) {
            users = users.stream()
                    .filter(user -> isActive.equals(user.getEnabled()))
                    .collect(Collectors.toList());
        }
        
        List<String> headers = Arrays.asList(
                "序号", "用户名", "真实姓名", "邮箱", "手机号", "所属部门", "角色", 
                "状态", "注册时间", "最后登录时间"
        );
        
        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        for (User user : users) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(user.getUsername() != null ? user.getUsername() : "");
            row.add(user.getRealName() != null ? user.getRealName() : "");
            row.add(user.getEmail() != null ? user.getEmail() : "");
            row.add(user.getPhone() != null ? user.getPhone() : "");
            row.add(user.getDepartment() != null ? user.getDepartment().getName() : "");
            row.add(user.getRole() != null ? user.getRole() : "");
            row.add(user.getEnabled() != null && user.getEnabled() ? "启用" : "禁用");
            row.add(user.getCreatedAt() != null ? 
                    user.getCreatedAt().format(DATETIME_FORMATTER) : "");
            row.add(""); // 最后登录时间 - 暂无此字段
            data.add(row);
        }
        
        return exportToExcel("用户列表", headers, data);
    }
    
    @Override
    public byte[] exportLeaveRequests(String keyword, String status, String leaveType, 
                                     LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest> requests = leaveRequestRepository.findByDeletedFalse(
                Sort.by(Sort.Direction.DESC, "applyTime"));
        
        // 过滤条件
        if (keyword != null && !keyword.isEmpty()) {
            requests = requests.stream()
                    .filter(request -> (request.getUser() != null && 
                            request.getUser().getRealName().contains(keyword)) ||
                            (request.getReason() != null && request.getReason().contains(keyword)))
                    .collect(Collectors.toList());
        }
        
        if (status != null && !status.isEmpty()) {
            requests = requests.stream()
                    .filter(request -> status.equals(request.getStatus().name()))
                    .collect(Collectors.toList());
        }
        
        if (leaveType != null && !leaveType.isEmpty()) {
            requests = requests.stream()
                    .filter(request -> leaveType.equals(request.getLeaveType().name()))
                    .collect(Collectors.toList());
        }
        
        if (startDate != null && endDate != null) {
            requests = requests.stream()
                    .filter(request -> {
                        LocalDate applyDate = request.getApplyTime().toLocalDate();
                        return !applyDate.isBefore(startDate) && !applyDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());
        }
        
        List<String> headers = Arrays.asList(
                "序号", "申请人", "所属部门", "请假类型", "开始日期", "结束日期", "请假天数", 
                "请假原因", "申请时间", "状态", "审批人", "审批时间", "审批备注", "紧急程度"
        );
        
        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        for (LeaveRequest request : requests) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(request.getUser() != null ? request.getUser().getRealName() : "");
            row.add(request.getUser() != null && request.getUser().getDepartment() != null ? 
                    request.getUser().getDepartment().getName() : "");
            row.add(getLeaveTypeName(request.getLeaveType()));
            row.add(request.getStartDate() != null ? 
                    request.getStartDate().format(DATE_FORMATTER) : "");
            row.add(request.getEndDate() != null ? 
                    request.getEndDate().format(DATE_FORMATTER) : "");
            row.add(request.getDaysCount() != null ? request.getDaysCount() : 0);
            row.add(request.getReason() != null ? request.getReason() : "");
            row.add(request.getApplyTime() != null ? 
                    request.getApplyTime().format(DATETIME_FORMATTER) : "");
            row.add(getStatusName(request.getStatus()));
            row.add(request.getApprover() != null ? request.getApprover().getRealName() : "");
            row.add(request.getApproveTime() != null ? 
                    request.getApproveTime().format(DATETIME_FORMATTER) : "");
            row.add(request.getApproveNotes() != null ? request.getApproveNotes() : "");
            row.add(request.getEmergency() != null && request.getEmergency() ? "紧急" : "普通");
            data.add(row);
        }
        
        return exportToExcel("请假记录", headers, data);
    }
    
    @Override
    public byte[] exportDutyRecords(LocalDate startDate, LocalDate endDate, String status) {
        List<DutyRecord> records = dutyRecordRepository.findByDateRange(startDate, endDate);
        
        if (status != null && !status.isEmpty()) {
            records = records.stream()
                    .filter(record -> status.equals(record.getStatus()))
                    .collect(Collectors.toList());
        }
        
        List<String> headers = Arrays.asList(
                "序号", "执勤人员", "所属部门", "执勤日期", "星期", "计划开始时间", "计划结束时间", 
                "签到时间", "签退时间", "状态", "备注"
        );
        
        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        String[] weekDays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        
        for (DutyRecord record : records) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(record.getUser() != null ? record.getUser().getRealName() : "");
            row.add(record.getUser() != null && record.getUser().getDepartment() != null ? 
                    record.getUser().getDepartment().getName() : "");
            row.add(record.getDutyDate() != null ? 
                    record.getDutyDate().format(DATE_FORMATTER) : "");
            row.add(record.getDutySchedule() != null ? 
                    weekDays[record.getDutySchedule().getDayOfWeek()] : "");
            row.add(record.getDutySchedule() != null ? record.getDutySchedule().getStartTime() : "");
            row.add(record.getDutySchedule() != null ? record.getDutySchedule().getEndTime() : "");
            row.add(record.getCheckinTime() != null ? 
                    record.getCheckinTime().format(DATETIME_FORMATTER) : "");
            row.add(record.getCheckoutTime() != null ? 
                    record.getCheckoutTime().format(DATETIME_FORMATTER) : "");
            row.add(record.getStatus() != null ? record.getStatus() : "");
            row.add(record.getNotes() != null ? record.getNotes() : "");
            data.add(row);
        }
        
        return exportToExcel("执勤记录", headers, data);
    }
    
    @Override
    public byte[] exportCheckinStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        // 这里实现打卡统计报表的导出
        // 包含多个工作表：总体统计、每日统计、时长统计等
        Map<String, Object> sheetsData = new HashMap<>();
        
        // 总体统计
        List<String> summaryHeaders = Arrays.asList("统计项", "数值", "说明");
        List<List<Object>> summaryData = Arrays.asList(
                Arrays.asList("统计期间", startDate + " 至 " + endDate, ""),
                Arrays.asList("总打卡天数", "0", "在统计期间内的打卡天数"),
                Arrays.asList("正常签到天数", "0", "按时签到的天数"),
                Arrays.asList("迟到天数", "0", "迟到签到的天数"),
                Arrays.asList("缺勤天数", "0", "未签到的天数"),
                Arrays.asList("平均学习时长", "0分钟", "每天平均学习时长"),
                Arrays.asList("总学习时长", "0分钟", "累计学习时长")
        );
        sheetsData.put("总体统计", Map.of("headers", summaryHeaders, "data", summaryData));
        
        return exportMultipleSheets(sheetsData);
    }
    
    @Override
    public byte[] exportToExcel(String sheetName, List<String> headers, List<List<Object>> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                List<Object> rowData = data.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = rowData.get(j);
                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                    cell.setCellStyle(dataStyle);
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                // 设置最大列宽
                int columnWidth = sheet.getColumnWidth(i);
                if (columnWidth > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }
            
            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] exportMultipleSheets(Map<String, Object> sheetsData) {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            for (Map.Entry<String, Object> entry : sheetsData.entrySet()) {
                String sheetName = entry.getKey();
                Map<String, Object> sheetData = (Map<String, Object>) entry.getValue();
                
                List<String> headers = (List<String>) sheetData.get("headers");
                List<List<Object>> data = (List<List<Object>>) sheetData.get("data");
                
                Sheet sheet = workbook.createSheet(sheetName);
                
                // 创建表头
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(headerStyle);
                }
                
                // 填充数据
                for (int i = 0; i < data.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    List<Object> rowData = data.get(i);
                    for (int j = 0; j < rowData.size(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = rowData.get(j);
                        if (value != null) {
                            if (value instanceof Number) {
                                cell.setCellValue(((Number) value).doubleValue());
                            } else {
                                cell.setCellValue(value.toString());
                            }
                        }
                        cell.setCellStyle(dataStyle);
                    }
                }
                
                // 自动调整列宽
                for (int i = 0; i < headers.size(); i++) {
                    sheet.autoSizeColumn(i);
                    int columnWidth = sheet.getColumnWidth(i);
                    if (columnWidth > 15000) {
                        sheet.setColumnWidth(i, 15000);
                    }
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] exportTemplate(String templateType) {
        Map<String, List<String>> templates = new HashMap<>();
        templates.put("user", Arrays.asList("用户名*", "真实姓名*", "邮箱", "手机号", "所属部门", "角色", "备注"));
        templates.put("equipment", Arrays.asList("设备名称*", "设备分类*", "序列号", "品牌", "型号", "总数量*", "采购价格", "位置", "描述"));
        templates.put("location", Arrays.asList("地点名称*", "描述", "纬度*", "经度*", "有效半径*", "详细地址", "是否启用"));
        
        List<String> headers = templates.getOrDefault(templateType, Arrays.asList("模板类型不存在"));
        List<List<Object>> data = new ArrayList<>();
        
        // 添加示例数据行
        List<Object> exampleRow = new ArrayList<>();
        for (String header : headers) {
            exampleRow.add("示例数据");
        }
        data.add(exampleRow);
        
        return exportToExcel(templateType + "导入模板", headers, data);
    }
    
    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // 背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 对齐
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 获取请假类型名称
     */
    private String getLeaveTypeName(LeaveRequest.LeaveType leaveType) {
        if (leaveType == null) return "";
        switch (leaveType) {
            case DUTY_LEAVE: return "执勤请假";
            case CHECKIN_LEAVE: return "打卡请假";
            case OTHER: return "其他";
            default: return leaveType.name();
        }
    }
    
    /**
     * 获取状态名称
     */
    private String getStatusName(LeaveRequest.RequestStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING: return "待审批";
            case APPROVED: return "已批准";
            case REJECTED: return "已拒绝";
            case CANCELLED: return "已取消";
            default: return status.name();
        }
    }
}
