package com.example.photography.service.impl;

import com.example.photography.dto.request.DutyCheckinRequest;
import com.example.photography.dto.request.DutyScheduleRequest;
import com.example.photography.dto.request.DutySwapRequestCreateRequest;
import com.example.photography.dto.request.DutySwapRequestDecisionRequest;
import com.example.photography.model.entity.DutyRecord;
import com.example.photography.model.entity.DutySchedule;
import com.example.photography.model.entity.DutySwapRequest;
import com.example.photography.model.entity.User;
import com.example.photography.repository.DutyRecordRepository;
import com.example.photography.repository.DutyScheduleRepository;
import com.example.photography.repository.DutySwapRequestRepository;
import com.example.photography.service.DutyService;
import com.example.photography.service.LeaveService;
import com.example.photography.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 办公室执勤服务实现类
 */
@Service
@Transactional
public class DutyServiceImpl implements DutyService {
    
    @Autowired
    private DutyScheduleRepository dutyScheduleRepository;
    
    @Autowired
    private DutyRecordRepository dutyRecordRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LeaveService leaveService;

    @Autowired
    private DutySwapRequestRepository dutySwapRequestRepository;
    
    // ========== 执勤排班管理 ==========
    
    @Override
    public DutySchedule createDutySchedule(DutyScheduleRequest request) {
        User user = userService.findById(request.getUserId());
        
        // 验证时间合理性
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        
        // 检查时间冲突
        List<DutySchedule> conflictingSchedules = dutyScheduleRepository.findConflictingSchedules(
            request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), 0L);
        
        if (!conflictingSchedules.isEmpty()) {
            throw new RuntimeException("该时间段已有其他排班，请选择其他时间");
        }
        
        DutySchedule schedule = new DutySchedule(user, request.getDayOfWeek(), 
            request.getStartTime(), request.getEndTime());
        schedule.setActive(request.getActive());
        schedule.setNotes(request.getNotes());
        schedule.setEarlyCheckinMinutes(request.getEarlyCheckinMinutes());
        schedule.setLateCheckinMinutes(request.getLateCheckinMinutes());
        
        DutySchedule savedSchedule = dutyScheduleRepository.save(schedule);
        
        // 重新查询以获得急切加载的用户和部门信息，避免JSON序列化时的懒加载问题
        return dutyScheduleRepository.findByIdWithUser(savedSchedule.getId())
                .orElse(savedSchedule);
    }
    
    @Override
    public DutySchedule updateDutySchedule(Long id, DutyScheduleRequest request) {
        DutySchedule schedule = findDutyScheduleById(id);
        User user = userService.findById(request.getUserId());
        
        // 验证时间合理性
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        
        // 检查时间冲突（排除当前记录）
        List<DutySchedule> conflictingSchedules = dutyScheduleRepository.findConflictingSchedules(
            request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), id);
        
        if (!conflictingSchedules.isEmpty()) {
            throw new RuntimeException("该时间段已有其他排班，请选择其他时间");
        }
        
        schedule.setUser(user);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setActive(request.getActive());
        schedule.setNotes(request.getNotes());
        schedule.setEarlyCheckinMinutes(request.getEarlyCheckinMinutes());
        schedule.setLateCheckinMinutes(request.getLateCheckinMinutes());
        
        DutySchedule savedSchedule = dutyScheduleRepository.save(schedule);
        
        // 重新查询以获得急切加载的用户和部门信息，避免JSON序列化时的懒加载问题
        return dutyScheduleRepository.findByIdWithUser(savedSchedule.getId())
                .orElse(savedSchedule);
    }
    
    @Override
    public void deleteDutySchedule(Long id) {
        DutySchedule schedule = findDutyScheduleById(id);

        // 先删除与该排班相关的调换申请，避免残留脏数据
        List<DutySwapRequest> relatedSwaps = dutySwapRequestRepository
                .findByRequesterSchedule_IdOrTargetSchedule_Id(id, id);
        if (!relatedSwaps.isEmpty()) {
            dutySwapRequestRepository.deleteAll(relatedSwaps);
        }

        // 先删除与该排班相关的执勤记录，避免外键约束问题
        List<DutyRecord> dutyRecords = dutyRecordRepository.findByDutyScheduleIdAndDeletedFalse(id);
        if (!dutyRecords.isEmpty()) {
            dutyRecordRepository.deleteAll(dutyRecords);
        }
        // 再物理删除排班本身
        dutyScheduleRepository.delete(schedule);
    }
    
    @Override
    public void toggleDutyScheduleStatus(Long id, boolean active) {
        DutySchedule schedule = findDutyScheduleById(id);
        schedule.setActive(active);
        dutyScheduleRepository.save(schedule);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DutySchedule findDutyScheduleById(Long id) {
        return dutyScheduleRepository.findById(id)
                .filter(schedule -> !schedule.getDeleted())
                .orElseThrow(() -> new RuntimeException("执勤排班不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DutySchedule> getAllDutySchedules() {
        return dutyScheduleRepository.findByDeletedFalseOrderByDayOfWeekAscStartTimeAsc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DutySchedule> searchDutySchedules(String keyword, Integer dayOfWeek) {
        List<DutySchedule> allSchedules = dutyScheduleRepository.findByDeletedFalseOrderByDayOfWeekAscStartTimeAsc();
        
        return allSchedules.stream()
            .filter(schedule -> {
                boolean matchesKeyword = true;
                boolean matchesDayOfWeek = true;
                
                // 关键字搜索：用户姓名或部门名称
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String lowerKeyword = keyword.toLowerCase().trim();
                    String userName = schedule.getUser().getRealName() != null ? 
                        schedule.getUser().getRealName().toLowerCase() : "";
                    String userUsername = schedule.getUser().getUsername() != null ? 
                        schedule.getUser().getUsername().toLowerCase() : "";
                    String departmentName = schedule.getUser().getDepartment() != null && 
                        schedule.getUser().getDepartment().getName() != null ? 
                        schedule.getUser().getDepartment().getName().toLowerCase() : "";
                    
                    matchesKeyword = userName.contains(lowerKeyword) || 
                                   userUsername.contains(lowerKeyword) || 
                                   departmentName.contains(lowerKeyword);
                }
                
                // 星期几搜索
                if (dayOfWeek != null) {
                    matchesDayOfWeek = schedule.getDayOfWeek().equals(dayOfWeek);
                }
                
                return matchesKeyword && matchesDayOfWeek;
            })
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DutySchedule> getActiveDutySchedules() {
        return dutyScheduleRepository.findByActiveTrueAndDeletedFalseOrderByDayOfWeekAscStartTimeAsc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DutySchedule> getUserDutySchedules(Long userId) {
        return dutyScheduleRepository.findByUser_IdAndActiveTrueAndDeletedFalse(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DutySchedule> getDutySchedulesByDayOfWeek(Integer dayOfWeek) {
        return dutyScheduleRepository.findByDayOfWeekAndActiveTrueAndDeletedFalse(dayOfWeek);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DutySchedule getCurrentDutySchedule(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalDate today = now.toLocalDate();

        // 优先根据当天已生成的执勤记录来判断（包括调换生成的记录）
        DutyRecord todayRecord = dutyRecordRepository
                .findByUser_IdAndDutyDateAndDeletedFalse(userId, today)
                .orElse(null);
        if (todayRecord != null && todayRecord.getDutySchedule() != null) {
            boolean staleLeaveRecord = "已请假".equals(todayRecord.getStatus())
                    && !leaveService.hasApprovedDutyLeave(userId, today, todayRecord.getDutySchedule().getId());
            if (!staleLeaveRecord) {
                return todayRecord.getDutySchedule();
            }
        }

        List<DutySchedule> todaySchedules = getEffectiveSchedulesForUserOnDate(userId, now.toLocalDate());

        for (DutySchedule schedule : todaySchedules) {
            // 允许签到时间段：以“排班开始时间”为中心，向前 earlyCheckinMinutes，向后 lateCheckinMinutes
            LocalTime allowedStartTime = schedule.getStartTime()
                    .minusMinutes(schedule.getEarlyCheckinMinutes());
            LocalTime allowedEndTime = schedule.getStartTime()
                    .plusMinutes(schedule.getLateCheckinMinutes());

            boolean withinWindow;
            if (allowedStartTime.isAfter(allowedEndTime)) {
                // 跨日排班
                withinWindow = currentTime.isAfter(allowedStartTime) || currentTime.isBefore(allowedEndTime);
            } else {
                withinWindow = !currentTime.isBefore(allowedStartTime) && !currentTime.isAfter(allowedEndTime);
            }

            if (withinWindow) {
                return schedule;
            }
        }

        return null;
    }
    
    // ========== 执勤记录管理 ==========
    
    @Override
    public DutyRecord checkin(Long userId, DutyCheckinRequest request) {
        if (!canDutyCheckin(userId)) {
            throw new RuntimeException("当前不能进行执勤打卡，请检查执勤安排");
        }
        
        LocalDate today = LocalDate.now();
        User user = userService.findById(userId);

        DutySchedule schedule = getCurrentDutySchedule(userId);
        if (schedule == null) {
            throw new RuntimeException("今日没有执勤安排");
        }
        
        // 检查当前排班是否有请假，避免其他日期/其他排班的请假误挡当前签到
        if (leaveService.hasApprovedDutyLeave(userId, today, schedule.getId())) {
            throw new RuntimeException("今日已请假，无需执勤打卡");
        }
        
        // 获取或创建当前排班的今日执勤记录
        DutyRecord record = dutyRecordRepository
                .findByUser_IdAndDutyDateAndDutySchedule_IdAndDeletedFalse(userId, today, schedule.getId())
                .orElse(null);
        if (record == null) {
            record = new DutyRecord(user, schedule, today);
        }
        
        if (record.getCheckinTime() != null) {
            throw new RuntimeException("今日已经签到过了");
        }
        
        record.setCheckinTime(LocalDateTime.now());
        record.setStatus("执勤中");
        record.setNotes(request.getNotes());
        
        return dutyRecordRepository.save(record);
    }
    
    @Override
    public DutyRecord checkout(Long userId, DutyCheckinRequest request) {
        LocalDate today = LocalDate.now();
        DutyRecord record = getUserDutyRecordByDate(userId, today);
        
        if (record == null || record.getCheckinTime() == null) {
            throw new RuntimeException("请先签到");
        }
        
        if (record.getCheckoutTime() != null) {
            throw new RuntimeException("今日已经签退过了");
        }
        
        record.setCheckoutTime(LocalDateTime.now());
        record.setStatus("已完成");
        if (request.getNotes() != null) {
            record.setNotes(record.getNotes() + "\n签退备注：" + request.getNotes());
        }
        
        return dutyRecordRepository.save(record);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canDutyCheckin(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalDate today = now.toLocalDate();
        int dayOfWeek = now.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        
        System.out.println("🔍 检查执勤签到权限 - 用户ID: " + userId);
        System.out.println("📅 当前时间: " + now + " (星期" + dayOfWeek + ")");
        
        // 1. 如果今天已经有执勤记录（包括调换生成的记录），优先根据记录对应的排班判断签到窗口
        DutyRecord todayRecord = dutyRecordRepository
                .findByUser_IdAndDutyDateAndDeletedFalse(userId, today)
                .orElse(null);
        if (todayRecord != null && todayRecord.getDutySchedule() != null) {
            boolean staleLeaveRecord = "已请假".equals(todayRecord.getStatus())
                    && !leaveService.hasApprovedDutyLeave(userId, today, todayRecord.getDutySchedule().getId());
            if (staleLeaveRecord) {
                System.out.println("⚠️ 忽略无对应已批准请假的执勤请假占位记录，继续检查实际排班");
            } else {
            DutySchedule schedule = todayRecord.getDutySchedule();
            LocalTime allowedStartTime = schedule.getStartTime()
                    .minusMinutes(schedule.getEarlyCheckinMinutes());
            LocalTime allowedEndTime = schedule.getStartTime()
                    .plusMinutes(schedule.getLateCheckinMinutes());

            System.out.println("📝 基于执勤记录检查签到窗口");
            System.out.println("⏰ 排班时间: " + schedule.getStartTime() + " - " + schedule.getEndTime());
            System.out.println("⏰ 允许签到开始时间: " + allowedStartTime);
            System.out.println("⏰ 允许签到结束时间: " + allowedEndTime);
            System.out.println("⏰ 当前时间: " + currentTime);

            if (allowedStartTime.isAfter(allowedEndTime)) {
                // 跨日情况
                if (currentTime.isAfter(allowedStartTime) || currentTime.isBefore(allowedEndTime)) {
                    System.out.println("✅ 当前时间在允许签到范围内（记录-跨日）");
                    return true;
                }
            } else {
                if (!currentTime.isBefore(allowedStartTime) && !currentTime.isAfter(allowedEndTime)) {
                    System.out.println("✅ 当前时间在允许签到范围内（记录-同日）");
                    return true;
                }
            }
            System.out.println("❌ 基于执勤记录判断：当前时间不在允许签到范围内");
            return false;
            }
        }

        // 2. 否则，查找今天的执勤安排（自动应用已同意的单日调换）
        List<DutySchedule> todaySchedules = getEffectiveSchedulesForUserOnDate(userId, today);
        
        System.out.println("📋 找到 " + todaySchedules.size() + " 个执勤排班");
        
        if (todaySchedules.isEmpty()) {
            System.out.println("❌ 今日无执勤排班，无法签到");
            return false;
        }
        
        // 检查是否在任何一个排班的允许时间范围内（以开始时间为中心，考虑提前和延迟时间）
        for (DutySchedule schedule : todaySchedules) {
            System.out.println("\n🎯 检查排班: " + schedule.getId());
            System.out.println("⏰ 排班时间: " + schedule.getStartTime() + " - " + schedule.getEndTime());
            System.out.println("⏰ 提前签到分钟: " + schedule.getEarlyCheckinMinutes());
            System.out.println("⏰ 延迟签到分钟: " + schedule.getLateCheckinMinutes());

            // 允许签到时间：从 开始时间-early 到 开始时间+late
            LocalTime allowedStartTime = schedule.getStartTime()
                    .minusMinutes(schedule.getEarlyCheckinMinutes());
            LocalTime allowedEndTime = schedule.getStartTime()
                    .plusMinutes(schedule.getLateCheckinMinutes());
            
            System.out.println("⏰ 允许签到开始时间: " + allowedStartTime);
            System.out.println("⏰ 允许签到结束时间: " + allowedEndTime);
            System.out.println("⏰ 当前时间: " + currentTime);
            
            // 处理跨日的情况
            if (allowedStartTime.isAfter(allowedEndTime)) {
                System.out.println("🌙 检测到跨日排班");
                // 跨日情况：当前时间在开始时间之后或结束时间之前
                if (currentTime.isAfter(allowedStartTime) || currentTime.isBefore(allowedEndTime)) {
                    System.out.println("✅ 当前时间在允许签到范围内（跨日）");
                    return true;
                } else {
                    System.out.println("❌ 当前时间不在允许签到范围内（跨日）");
                }
            } else {
                System.out.println("☀️ 检测到同日排班");
                // 同日情况：当前时间在开始和结束时间之间
                if (!currentTime.isBefore(allowedStartTime) && !currentTime.isAfter(allowedEndTime)) {
                    System.out.println("✅ 当前时间在允许签到范围内（同日）");
                    return true;
                } else {
                    System.out.println("❌ 当前时间不在允许签到范围内（同日）");
                    System.out.println("   - 当前时间早于允许开始时间: " + currentTime.isBefore(allowedStartTime));
                    System.out.println("   - 当前时间晚于允许结束时间: " + currentTime.isAfter(allowedEndTime));
                }
            }
        }
        
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DutyRecord> getUserDutyRecords(Long userId, Pageable pageable) {
        return dutyRecordRepository.findByUser_IdAndDeletedFalseOrderByDutyDateDesc(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DutyRecord> getAllDutyRecords(Pageable pageable) {
        return dutyRecordRepository.findByDeletedFalseOrderByDutyDateDesc(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DutyRecord> searchDutyRecords(Pageable pageable, String keyword, String status, LocalDate startDate, LocalDate endDate) {
        // 如果没有搜索条件，返回所有记录
        if ((keyword == null || keyword.trim().isEmpty()) && 
            status == null && startDate == null && endDate == null) {
            return getAllDutyRecords(pageable);
        }
        
        // 获取所有记录进行过滤（简化实现，生产环境建议用数据库查询）
        List<DutyRecord> allRecords = dutyRecordRepository.findByDeletedFalseOrderByDutyDateDesc(
            org.springframework.data.domain.PageRequest.of(0, 10000, pageable.getSort())
        ).getContent();
        
        List<DutyRecord> filteredRecords = allRecords.stream()
            .filter(record -> {
                boolean matchesKeyword = true;
                boolean matchesStatus = true;
                boolean matchesDateRange = true;
                
                // 关键字搜索：用户姓名或部门名称
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String lowerKeyword = keyword.toLowerCase().trim();
                    String userName = record.getUser().getRealName() != null ? 
                        record.getUser().getRealName().toLowerCase() : "";
                    String userUsername = record.getUser().getUsername() != null ? 
                        record.getUser().getUsername().toLowerCase() : "";
                    String departmentName = record.getUser().getDepartment() != null && 
                        record.getUser().getDepartment().getName() != null ? 
                        record.getUser().getDepartment().getName().toLowerCase() : "";
                    
                    matchesKeyword = userName.contains(lowerKeyword) || 
                                   userUsername.contains(lowerKeyword) || 
                                   departmentName.contains(lowerKeyword);
                }
                
                // 状态搜索
                if (status != null && !status.trim().isEmpty()) {
                    matchesStatus = status.equals(record.getStatus());
                }
                
                // 日期范围搜索
                if (startDate != null || endDate != null) {
                    LocalDate recordDate = record.getDutyDate();
                    if (startDate != null && recordDate.isBefore(startDate)) {
                        matchesDateRange = false;
                    }
                    if (endDate != null && recordDate.isAfter(endDate)) {
                        matchesDateRange = false;
                    }
                }
                
                return matchesKeyword && matchesStatus && matchesDateRange;
            })
            .toList();
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredRecords.size());
        List<DutyRecord> pageContent = start < filteredRecords.size() ? 
            filteredRecords.subList(start, end) : List.of();
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, filteredRecords.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DutyRecord> getDutyRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        return dutyRecordRepository.findByDateRange(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DutyRecord> getDutyRecordsByStatus(String status) {
        return dutyRecordRepository.findByStatusAndDeletedFalse(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DutyRecord getUserDutyRecordByDate(Long userId, LocalDate date) {
        List<DutyRecord> records = dutyRecordRepository.findAllByUser_IdAndDutyDateAndDeletedFalse(userId, date);
        if (records.isEmpty()) {
            return null;
        }

        return records.stream()
                .filter(record -> !"已请假".equals(record.getStatus()) || record.getDutySchedule() == null
                        || leaveService.hasApprovedDutyLeave(userId, date, record.getDutySchedule().getId()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] exportDutyRecordsToExcel(LocalDate startDate, LocalDate endDate) {
        List<DutyRecord> records = getDutyRecordsByDateRange(startDate, endDate);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("办公室执勤记录");
            
            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "姓名", "所属部门", "执勤日期", "星期", "签到时间", 
                              "签退时间", "计划开始时间", "计划结束时间", "状态", "备注"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String[] weekDays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            
            for (int i = 0; i < records.size(); i++) {
                DutyRecord record = records.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(record.getUser().getRealName());
                row.createCell(2).setCellValue(record.getUser().getDepartment() != null ? 
                    record.getUser().getDepartment().getName() : "");
                row.createCell(3).setCellValue(record.getDutyDate().format(dateFormatter));
                row.createCell(4).setCellValue(weekDays[record.getDutySchedule().getDayOfWeek()]);
                row.createCell(5).setCellValue(record.getCheckinTime() != null ? 
                    record.getCheckinTime().format(timeFormatter) : "");
                row.createCell(6).setCellValue(record.getCheckoutTime() != null ? 
                    record.getCheckoutTime().format(timeFormatter) : "");
                row.createCell(7).setCellValue(record.getDutySchedule().getStartTime().format(timeFormatter));
                row.createCell(8).setCellValue(record.getDutySchedule().getEndTime().format(timeFormatter));
                row.createCell(9).setCellValue(record.getStatus());
                row.createCell(10).setCellValue(record.getNotes() != null ? record.getNotes() : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 将工作簿写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public void generateDutyRecords(LocalDate startDate, LocalDate endDate) {
        List<DutySchedule> activeSchedules = getActiveDutySchedules();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            
            // 查找该日期的执勤安排
            for (DutySchedule schedule : activeSchedules) {
                if (schedule.getDayOfWeek().equals(dayOfWeek)) {
                    // 检查是否已存在记录
                    DutyRecord existingRecord = getUserDutyRecordByDate(schedule.getUser().getId(), currentDate);
                    if (existingRecord == null) {
                        DutyRecord record = new DutyRecord(schedule.getUser(), schedule, currentDate);
                        dutyRecordRepository.save(record);
                    }
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
    }
    
    @Override
    @Transactional
    public void deleteDutyRecord(Long id) {
        DutyRecord dutyRecord = dutyRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("执勤记录不存在"));
        
        // 物理删除记录以节省存储空间
        dutyRecordRepository.delete(dutyRecord);
        System.out.println("已物理删除执勤记录: " + id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DutyStatistics getDutyStatistics() {
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate startOfWeek = today.with(weekFields.dayOfWeek(), 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        long totalSchedules = dutyScheduleRepository.countByDeletedFalse();
        long activeSchedules = getActiveDutySchedules().size();
        long totalRecords = dutyRecordRepository.countByDeletedFalse();
        
        long todayDuties = dutyRecordRepository.findByDutyDateAndDeletedFalse(today).size();
        long thisWeekDuties = getDutyRecordsByDateRange(startOfWeek, endOfWeek).size();
        long completedDuties = getDutyRecordsByStatus("已完成").size();
        long missedDuties = getDutyRecordsByStatus("缺勤").size();
        
        return new DutyStatistics(
            totalSchedules, activeSchedules, totalRecords, todayDuties,
            thisWeekDuties, completedDuties, missedDuties
        );
    }

    // ========== 排班调换管理 ==========

    @Override
    public DutySwapRequest createDutySwapRequest(Long requesterId, DutySwapRequestCreateRequest request) {
        DutySchedule requesterSchedule = findDutyScheduleById(request.getRequesterScheduleId());
        DutySchedule targetSchedule = findDutyScheduleById(request.getTargetScheduleId());
        User operator = userService.findById(requesterId);
        User realRequester = requesterSchedule.getUser();

        if (request.getSwapDate() == null) {
            throw new RuntimeException("调换日期不能为空");
        }
        LocalDate today = LocalDate.now();
        if (request.getSwapDate().isBefore(today)) {
            throw new RuntimeException("调换日期只能选择今天或之后的日期");
        }
        // 基本校验：排班必须有效且处于启用状态
        if (!Boolean.TRUE.equals(requesterSchedule.getActive()) || !Boolean.TRUE.equals(targetSchedule.getActive())) {
            throw new RuntimeException("只能对启用中的排班发起调换申请");
        }

        // 校验发起人权限：普通用户只能对自己的排班发起申请，管理员可以代为发起
        boolean isAdmin = operator.getRole() != null && operator.getRole().name().contains("ADMIN");
        if (!isAdmin && !realRequester.getId().equals(requesterId)) {
            throw new RuntimeException("只能对自己的排班发起调换申请");
        }

        // 校验两个排班不属于同一用户
        if (realRequester.getId().equals(targetSchedule.getUser().getId())) {
            throw new RuntimeException("不能与自己调换排班");
        }

        DutySwapRequest swapRequest = new DutySwapRequest();
        // 真实发起人始终为该排班对应的用户（即使管理员代为发起）
        swapRequest.setRequester(realRequester);
        swapRequest.setTargetUser(targetSchedule.getUser());
        swapRequest.setRequesterSchedule(requesterSchedule);
        swapRequest.setTargetSchedule(targetSchedule);
        // 只在指定日期进行调换，不改变长期排班
        swapRequest.setSwapDate(request.getSwapDate());
        swapRequest.setStatus("PENDING");
        swapRequest.setReason(request.getReason());

        return dutySwapRequestRepository.save(swapRequest);
    }

    @Override
    public DutySwapRequest handleDutySwapRequest(Long requestId, Long operatorId, DutySwapRequestDecisionRequest decisionRequest) {
        DutySwapRequest swapRequest = dutySwapRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("排班调换申请不存在"));

        // 只能处理未删除且状态为待处理的申请
        if (Boolean.TRUE.equals(swapRequest.getDeleted()) || !"PENDING".equals(swapRequest.getStatus())) {
            throw new RuntimeException("该排班调换申请已被处理或已失效");
        }

        // 只有被调换人或管理员才能处理
        User operator = userService.findById(operatorId);
        boolean isTargetUser = swapRequest.getTargetUser().getId().equals(operatorId);
        boolean isAdmin = operator.getRole() != null && operator.getRole().name().contains("ADMIN");
        if (!isTargetUser && !isAdmin) {
            throw new RuntimeException("只有被调换人或管理员可以处理该申请");
        }

        boolean approve = Boolean.TRUE.equals(decisionRequest.getApprove());
        if (!approve) {
            swapRequest.setStatus("REJECTED");
            swapRequest.setResponseReason(decisionRequest.getReason());
            return dutySwapRequestRepository.save(swapRequest);
        }

        // 同意申请时，仅标记该申请为已同意并应用单日调换
        swapRequest.setStatus("APPROVED");
        swapRequest.setResponseReason(decisionRequest.getReason());
        DutySwapRequest saved = dutySwapRequestRepository.save(swapRequest);
        applySwapToDutyRecords(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DutySwapRequest> getAllDutySwapRequests() {
        return dutySwapRequestRepository.findAllWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DutySwapRequest> getUserDutySwapRequests(Long userId) {
        return dutySwapRequestRepository.findByUserIdWithDetails(userId);
    }

    @Override
    @Transactional
    public void deleteDutySwapRequest(Long id) {
        DutySwapRequest request = dutySwapRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("排班调换申请不存在"));
        dutySwapRequestRepository.delete(request);
    }

    // ========== 辅助方法 ==========

    /**
     * 获取指定日期对用户生效的排班（自动考虑已同意的单日调换）
     */
    private List<DutySchedule> getEffectiveSchedulesForUserOnDate(Long userId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<DutySchedule> schedules = new ArrayList<>(
                dutyScheduleRepository.findByUser_IdAndDayOfWeekAndActiveTrueAndDeletedFalse(userId, dayOfWeek)
        );

        List<DutySwapRequest> approvedSwaps = dutySwapRequestRepository.findApprovedByUserAndDate(userId, date);
        for (DutySwapRequest swap : approvedSwaps) {
            // 选中的调换日期当天：
            // - 发起人：在该日按对方排班执勤
            // - 被调换人：该日不执勤
            if (swap.getRequester() != null && swap.getRequester().getId().equals(userId)) {
                removeScheduleById(schedules, swap.getRequesterSchedule());
                addScheduleIfAbsent(schedules, swap.getTargetSchedule());
            } else if (swap.getTargetUser() != null && swap.getTargetUser().getId().equals(userId)) {
                removeScheduleById(schedules, swap.getTargetSchedule());
            }
        }

        return schedules;
    }

    private void removeScheduleById(List<DutySchedule> schedules, DutySchedule target) {
        if (target == null || schedules == null || schedules.isEmpty()) {
            return;
        }
        schedules.removeIf(schedule -> schedule != null && schedule.getId() != null
                && schedule.getId().equals(target.getId()));
    }

    private void addScheduleIfAbsent(List<DutySchedule> schedules, DutySchedule scheduleToAdd) {
        if (scheduleToAdd == null || schedules == null) {
            return;
        }
        boolean exists = schedules.stream()
                .anyMatch(schedule -> schedule != null && schedule.getId() != null
                        && schedule.getId().equals(scheduleToAdd.getId()));
        if (!exists) {
            schedules.add(scheduleToAdd);
        }
    }

    /**
     * 调整指定日期的执勤记录，使其与已批准的单日调换保持一致
     */
    private void applySwapToDutyRecords(DutySwapRequest swapRequest) {
        if (swapRequest == null || swapRequest.getSwapDate() == null) {
            return;
        }
        DutySchedule requesterSchedule = swapRequest.getRequesterSchedule();
        DutySchedule targetSchedule = swapRequest.getTargetSchedule();
        User requester = swapRequest.getRequester();
        User targetUser = swapRequest.getTargetUser();
        LocalDate swapDate = swapRequest.getSwapDate();

        // 计算与发起人排班对应的实际日期（与调换日期在同一周）
        LocalDate requesterDate = swapDate;
        if (requesterSchedule != null) {
            int swapDay = swapDate.getDayOfWeek().getValue();
            int requesterDay = requesterSchedule.getDayOfWeek();
            int diff = requesterDay - swapDay;
            requesterDate = swapDate.plusDays(diff);
        }

        // 1. 调换日期当日：发起人按对方排班执勤，被调换人不执勤
        upsertDutyRecordForSwap(requester, targetSchedule, swapDate);
        deleteDutyRecordIfExists(targetUser, swapDate);

        // 2. 发起人原排班那一天：由被调换人执勤，发起人不执勤
        if (requesterSchedule != null) {
            upsertDutyRecordForSwap(targetUser, requesterSchedule, requesterDate);
            deleteDutyRecordIfExists(requester, requesterDate);
        }
    }

    private void upsertDutyRecordForSwap(User user, DutySchedule schedule, LocalDate swapDate) {
        if (user == null || schedule == null || swapDate == null) {
            return;
        }
        DutyRecord record = dutyRecordRepository.findByUser_IdAndDutyDateAndDeletedFalse(user.getId(), swapDate)
                .orElse(null);
        if (record == null) {
            record = new DutyRecord(user, schedule, swapDate);
        } else {
            record.setDutySchedule(schedule);
        }
        dutyRecordRepository.save(record);
    }

    private void deleteDutyRecordIfExists(User user, LocalDate date) {
        if (user == null || date == null) {
            return;
        }
        dutyRecordRepository.findByUser_IdAndDutyDateAndDeletedFalse(user.getId(), date)
                .ifPresent(dutyRecordRepository::delete);
    }
}
