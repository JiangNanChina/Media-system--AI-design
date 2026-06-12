package com.example.photography.service.impl;

import com.example.photography.dto.response.CheckinRecordDetailResponse;
import com.example.photography.dto.response.DailyCheckinSummaryResponse;
import com.example.photography.dto.response.UserAttendanceStatusResponse;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.model.entity.CheckinRecord;
import com.example.photography.model.entity.LeaveRequest;
import com.example.photography.model.entity.User;
import com.example.photography.repository.CheckinConfigurationRepository;
import com.example.photography.repository.CheckinRecordRepository;
import com.example.photography.repository.LeaveRequestRepository;
import com.example.photography.service.DailyCheckinService;
import com.example.photography.utils.CheckinWeekdayUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.stream.Collectors;

/**
 * 每日打卡汇总服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyCheckinServiceImpl implements DailyCheckinService {
    
    private final CheckinRecordRepository checkinRecordRepository;
    private final CheckinConfigurationRepository checkinConfigurationRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Page<DailyCheckinSummaryResponse> getDailyCheckinSummaries(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("获取每日打卡汇总: startDate={}, endDate={}", startDate, endDate);
        
        try {
            // 获取日期范围内的所有记录
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            List<CheckinRecord> allRecords = checkinRecordRepository.findAllWithFetch();
            
            // 按日期分组
            Map<LocalDate, List<CheckinRecord>> recordsByDate = allRecords.stream()
                .filter(record -> {
                    LocalDate recordDate = record.getCheckinTime().toLocalDate();
                    return !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate);
                })
                .collect(Collectors.groupingBy(record -> record.getCheckinTime().toLocalDate()));
            
            // 生成每日汇总 - 只为有记录的日期生成汇总
            List<DailyCheckinSummaryResponse> summaries = new ArrayList<>();
            
            // 只处理有打卡记录的日期
            for (Map.Entry<LocalDate, List<CheckinRecord>> entry : recordsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<CheckinRecord> dayRecords = entry.getValue();
                DailyCheckinSummaryResponse summary = generateDailySummary(date, dayRecords);
                summaries.add(summary);
            }
            
            // 按日期降序排列
            summaries.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), summaries.size());
            List<DailyCheckinSummaryResponse> pageContent = start < summaries.size() ? 
                summaries.subList(start, end) : new ArrayList<>();
            
            return new PageImpl<>(pageContent, pageable, summaries.size());
            
        } catch (Exception e) {
            log.error("获取每日打卡汇总失败", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DailyCheckinSummaryResponse> getUserDailyCheckinSummaries(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("获取用户每日打卡汇总: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        try {
            // 获取用户在日期范围内的记录
            List<CheckinRecord> userRecords = checkinRecordRepository.findByUserIdAndDeletedFalseWithFetch(userId);
            
            // 按日期分组
            Map<LocalDate, List<CheckinRecord>> recordsByDate = userRecords.stream()
                .filter(record -> {
                    LocalDate recordDate = record.getCheckinTime().toLocalDate();
                    return !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate);
                })
                .collect(Collectors.groupingBy(record -> record.getCheckinTime().toLocalDate()));
            
            // 生成每日汇总 - 只为有记录的日期生成汇总
            List<DailyCheckinSummaryResponse> summaries = new ArrayList<>();
            
            // 只处理有打卡记录的日期
            for (Map.Entry<LocalDate, List<CheckinRecord>> entry : recordsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<CheckinRecord> dayRecords = entry.getValue();
                if (dayRecords != null && !dayRecords.isEmpty()) {
                    DailyCheckinSummaryResponse summary = generateUserDailySummary(date, dayRecords);
                    summaries.add(summary);
                }
            }
            
            // 按日期降序排列
            summaries.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), summaries.size());
            List<DailyCheckinSummaryResponse> pageContent = start < summaries.size() ? 
                summaries.subList(start, end) : new ArrayList<>();
            
            return new PageImpl<>(pageContent, pageable, summaries.size());
            
        } catch (Exception e) {
            log.error("获取用户每日打卡汇总失败", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public DailyCheckinSummaryResponse getDailyCheckinDetail(LocalDate date, boolean isAdmin, Long userId) {
        log.info("获取每日打卡详情: date={}, isAdmin={}, userId={}", date, isAdmin, userId);
        
        try {
            List<CheckinRecord> dayRecords;
            
            if (isAdmin) {
                // 管理员获取当天所有记录
                List<CheckinRecord> allRecords = checkinRecordRepository.findAllWithFetch();
                dayRecords = allRecords.stream()
                    .filter(record -> record.getCheckinTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            } else {
                // 普通用户只获取自己的记录
                List<CheckinRecord> userRecords = checkinRecordRepository.findByUserIdAndDeletedFalseWithFetch(userId);
                dayRecords = userRecords.stream()
                    .filter(record -> record.getCheckinTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            }
            
            return isAdmin ? generateDailySummary(date, dayRecords) : generateUserDailySummary(date, dayRecords);
            
        } catch (Exception e) {
            log.error("获取每日打卡详情失败", e);
            return new DailyCheckinSummaryResponse();
        }
    }
    
    /**
     * 生成每日汇总（管理员视图）
     */
    private DailyCheckinSummaryResponse generateDailySummary(LocalDate date, List<CheckinRecord> dayRecords) {
        DailyCheckinSummaryResponse summary = new DailyCheckinSummaryResponse();
        summary.setDate(date);
        
        if (dayRecords == null) {
            dayRecords = new ArrayList<>();
        }
        
        // 获取当天活跃的配置
        List<CheckinConfiguration> activeConfigs = checkinConfigurationRepository.findAllActiveWithUsers().stream()
            .filter(config -> CheckinWeekdayUtils.isRequiredOnDate(config.getRequiredWeekdays(), date))
            .collect(Collectors.toList());
        
        // 计算应签到总人数（去重）
        Set<Long> allRequiredUserIds = new HashSet<>();
        for (CheckinConfiguration config : activeConfigs) {
            if (config.getRequiredUsers() != null) {
                config.getRequiredUsers().forEach(user -> allRequiredUserIds.add(user.getId()));
            }
        }
        
        // 获取当天请假的用户（从请假申请和打卡记录中综合统计）
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findApprovedLeavesByDate(date);
        Set<Long> leaveRequestUserIds = leaveRequests.stream()
            .map(leave -> leave.getUser().getId())
            .collect(Collectors.toSet());
        
        // 从打卡记录中获取请假状态的用户ID
        Set<Long> leaveCheckinUserIds = dayRecords.stream()
            .filter(record -> record.getStatus() == CheckinRecord.CheckinStatus.LEAVE)
            .map(record -> record.getUser().getId())
            .collect(Collectors.toSet());
        
        // 合并两种请假来源，避免重复计算
        Set<Long> leaveUserIds = new HashSet<>(leaveRequestUserIds);
        leaveUserIds.addAll(leaveCheckinUserIds);
        
        // 统计信息
        summary.setTotalRequiredCount(allRequiredUserIds.size());
        summary.setActiveConfigCount(activeConfigs.size());
        summary.setLeaveCount(leaveUserIds.size());
        
        // 统计已签到用户（排除请假、缺勤状态，且必须审核通过或无需审核的记录）
        Set<Long> checkedInUserIds = dayRecords.stream()
            .filter(record -> record.getStatus() != CheckinRecord.CheckinStatus.LEAVE 
                           && record.getStatus() != CheckinRecord.CheckinStatus.ABSENT
                           // 必须是审核通过或无需审核（不是PENDING状态）
                           && (record.getAuditStatus() == null 
                               || record.getAuditStatus() != CheckinRecord.AuditStatus.PENDING))
            .map(record -> record.getUser().getId())
            .collect(Collectors.toSet());
        
        summary.setCheckedInCount(checkedInUserIds.size());
        
        // 统计待审核人数
        long pendingAuditCount = dayRecords.stream()
            .filter(record -> record.getAuditStatus() == CheckinRecord.AuditStatus.PENDING)
            .map(record -> record.getUser().getId())
            .distinct()
            .count();
        summary.setPendingAuditCount((int) pendingAuditCount);
        
        // 统计迟到人数
        long lateCount = dayRecords.stream()
            .filter(record -> Boolean.TRUE.equals(record.getIsLate()))
            .map(record -> record.getUser().getId())
            .distinct()
            .count();
        summary.setLateCount((int) lateCount);
        
        // 统计缺勤用户：1. 有ABSENT状态的记录 2. 应签到但未签到且未请假且未待审核的人数
        Set<Long> absentRecordUserIds = dayRecords.stream()
            .filter(record -> record.getStatus() == CheckinRecord.CheckinStatus.ABSENT)
            .map(record -> record.getUser().getId())
            .collect(Collectors.toSet());
        
        // 获取待审核用户ID（用于排除）
        Set<Long> pendingAuditUserIds = dayRecords.stream()
            .filter(record -> record.getAuditStatus() == CheckinRecord.AuditStatus.PENDING)
            .map(record -> record.getUser().getId())
            .collect(Collectors.toSet());
        
        // 计算未打卡也未请假也未待审核的缺勤人数
        Set<Long> noRecordAbsentUserIds = new HashSet<>(allRequiredUserIds);
        noRecordAbsentUserIds.removeAll(checkedInUserIds);      // 移除已签到的
        noRecordAbsentUserIds.removeAll(leaveUserIds);          // 移除请假的
        noRecordAbsentUserIds.removeAll(absentRecordUserIds);   // 移除已有ABSENT记录的用户，避免重复
        noRecordAbsentUserIds.removeAll(pendingAuditUserIds);   // 移除待审核的用户（待审核不计入缺勤）
        
        // 合并两种缺勤情况
        Set<Long> allAbsentUserIds = new HashSet<>(absentRecordUserIds);
        allAbsentUserIds.addAll(noRecordAbsentUserIds);
        
        summary.setAbsentCount(allAbsentUserIds.size());
        
        // 计算签到率
        summary.calculateCheckinRate();
        
        // 设置主要配置信息（取第一个配置作为主要配置）
        if (!activeConfigs.isEmpty()) {
            CheckinConfiguration mainConfig = activeConfigs.get(0);
            summary.setMainLocationName(mainConfig.getLocationName());
            summary.setMainSessionName(mainConfig.getSessionName());
            summary.setConfigurationName(mainConfig.getName());
        }
        
        // 转换详细记录
        List<CheckinRecordDetailResponse> recordDetails = dayRecords.stream()
            .map(CheckinRecordDetailResponse::fromEntity)
            .sorted((a, b) -> a.getCheckinTime().compareTo(b.getCheckinTime()))
            .collect(Collectors.toList());
        
        summary.setRecords(recordDetails);
        
        // 生成用户状态列表 - 传递完整的用户信息
        Map<Long, User> allUsersMap = new HashMap<>();
        for (CheckinConfiguration config : activeConfigs) {
            if (config.getRequiredUsers() != null) {
                config.getRequiredUsers().forEach(user -> allUsersMap.put(user.getId(), user));
            }
        }
        
        summary.setUserStatuses(generateUserStatusList(allRequiredUserIds, dayRecords, leaveRequests, allUsersMap));
        
        return summary;
    }
    
    /**
     * 生成用户每日汇总
     */
    private DailyCheckinSummaryResponse generateUserDailySummary(LocalDate date, List<CheckinRecord> dayRecords) {
        DailyCheckinSummaryResponse summary = new DailyCheckinSummaryResponse();
        summary.setDate(date);
        
        if (dayRecords == null || dayRecords.isEmpty()) {
            summary.setTotalRequiredCount(0);
            summary.setCheckedInCount(0);
            summary.setLateCount(0);
            summary.setAbsentCount(0);
            summary.setLeaveCount(0);
            summary.setActiveConfigCount(0);
            summary.calculateCheckinRate();
            summary.setRecords(new ArrayList<>());
            return summary;
        }
        
        // 对于用户视图，只显示其个人统计
        summary.setTotalRequiredCount(dayRecords.size());
        summary.setCheckedInCount(dayRecords.size());
        
        long lateCount = dayRecords.stream()
            .filter(record -> Boolean.TRUE.equals(record.getIsLate()))
            .count();
        summary.setLateCount((int) lateCount);
        
        summary.setAbsentCount(0);
        summary.setLeaveCount(0);
        
        // 活跃配置数量
        long configCount = dayRecords.stream()
            .map(record -> record.getConfiguration().getId())
            .distinct()
            .count();
        summary.setActiveConfigCount((int) configCount);
        
        summary.calculateCheckinRate();
        
        // 转换详细记录
        List<CheckinRecordDetailResponse> recordDetails = dayRecords.stream()
            .map(CheckinRecordDetailResponse::fromEntity)
            .sorted((a, b) -> a.getCheckinTime().compareTo(b.getCheckinTime()))
            .collect(Collectors.toList());
        
        summary.setRecords(recordDetails);
        
        return summary;
    }
    
    /**
     * 生成用户状态列表
     */
    private List<UserAttendanceStatusResponse> generateUserStatusList(Set<Long> allRequiredUserIds, 
                                                                     List<CheckinRecord> dayRecords, 
                                                                     List<LeaveRequest> leaveRequests,
                                                                     Map<Long, User> allUsersMap) {
        List<UserAttendanceStatusResponse> userStatuses = new ArrayList<>();
        
        // 创建用户ID到打卡记录的映射
        Map<Long, CheckinRecord> userRecordMap = dayRecords.stream()
            .collect(Collectors.toMap(
                record -> record.getUser().getId(),
                record -> record,
                (existing, replacement) -> existing // 如果有重复，保留第一个
            ));
        
        // 创建用户ID到请假记录的映射
        Map<Long, LeaveRequest> userLeaveMap = leaveRequests.stream()
            .collect(Collectors.toMap(
                leave -> leave.getUser().getId(),
                leave -> leave,
                (existing, replacement) -> existing
            ));
        
        // 为每个应签到用户生成状态
        for (Long userId : allRequiredUserIds) {
            UserAttendanceStatusResponse userStatus = new UserAttendanceStatusResponse();
            
            CheckinRecord record = userRecordMap.get(userId);
            LeaveRequest leave = userLeaveMap.get(userId);
            
            if (record != null) {
                // 检查打卡记录状态
                userStatus.setUserId(record.getUser().getId());
                userStatus.setUserName(record.getUser().getRealName());
                userStatus.setDepartmentName(record.getUser().getDepartment() != null ? 
                    record.getUser().getDepartment().getName() : null);
                
                // 根据打卡记录的状态设置用户状态
                if (record.getStatus() == CheckinRecord.CheckinStatus.LEAVE) {
                    // 打卡记录标记为请假
                    userStatus.setStatus(UserAttendanceStatusResponse.AttendanceStatus.ON_LEAVE);
                    userStatus.setRemark(record.getNotes()); // 显示请假备注
                } else if (record.getStatus() == CheckinRecord.CheckinStatus.ABSENT) {
                    // ✅ 打卡记录标记为缺勤（例如审核拒绝）
                    userStatus.setStatus(UserAttendanceStatusResponse.AttendanceStatus.ABSENT);
                    userStatus.setRemark(record.getNotes()); // 显示缺勤原因
                } else {
                    // 正常签到（NORMAL, LATE, CHECKED_IN等）
                    userStatus.setStatus(UserAttendanceStatusResponse.AttendanceStatus.CHECKED_IN);
                }
                
                userStatus.setCheckinTime(record.getCheckinTime());
                userStatus.setCheckoutTime(record.getCheckoutTime());
                userStatus.setIsLate(record.getIsLate());
                userStatus.setLateMinutes(record.getLateMinutes());
                userStatus.setDurationMinutes(record.getDurationMinutes());
                userStatus.setCheckinStatus(record.getStatus());
                
                // 设置审核相关字段
                userStatus.setAuditStatus(record.getAuditStatus());
                userStatus.setAuditedByName(record.getAuditedBy() != null ? record.getAuditedBy().getRealName() : null);
                userStatus.setAuditTime(record.getAuditTime());
                userStatus.setAuditNotes(record.getAuditNotes());
                
            } else if (leave != null) {
                // 用户请假
                userStatus.setUserId(leave.getUser().getId());
                userStatus.setUserName(leave.getUser().getRealName());
                userStatus.setDepartmentName(leave.getUser().getDepartment() != null ? 
                    leave.getUser().getDepartment().getName() : null);
                userStatus.setStatus(UserAttendanceStatusResponse.AttendanceStatus.ON_LEAVE);
                userStatus.setLeaveType(leave.getLeaveType().getDescription());
                userStatus.setRemark(leave.getReason());
                
            } else {
                // 用户缺勤 - 从预加载的用户信息中获取
                User user = allUsersMap.get(userId);
                if (user != null) {
                    userStatus.setUserId(user.getId());
                    userStatus.setUserName(user.getRealName());
                    userStatus.setDepartmentName(user.getDepartment() != null ? 
                        user.getDepartment().getName() : null);
                    userStatus.setStatus(UserAttendanceStatusResponse.AttendanceStatus.ABSENT);
                } else {
                    // 如果找不到用户信息，设置默认值
                    userStatus.setUserId(userId);
                    userStatus.setUserName("未知用户");
                    userStatus.setDepartmentName("未知部门");
                    userStatus.setStatus(UserAttendanceStatusResponse.AttendanceStatus.ABSENT);
                }
            }
            
            userStatuses.add(userStatus);
        }
        
        // 按状态和用户名排序
        userStatuses.sort((a, b) -> {
            int statusCompare = a.getStatus().ordinal() - b.getStatus().ordinal();
            if (statusCompare != 0) {
                return statusCompare;
            }
            return a.getUserName().compareTo(b.getUserName());
        });
        
        return userStatuses;
    }
    
    @Override
    @Transactional
    public boolean deleteDailyRecords(LocalDate date) {
        try {
            // 计算日期范围（当天00:00:00到23:59:59）
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            log.info("删除当天所有打卡记录: date={}, startOfDay={}, endOfDay={}", date, startOfDay, endOfDay);
            
            // 使用批量删除方法，直接从数据库物理删除
            int deletedCount = checkinRecordRepository.deleteByCheckinTimeBetweenAndDeletedFalse(startOfDay, endOfDay);
            
            if (deletedCount == 0) {
                log.warn("未找到需要删除的打卡记录: date={}", date);
                return false;
            }
            
            log.info("成功物理删除当天所有打卡记录: date={}, deletedCount={}", date, deletedCount);
            return true;
            
        } catch (Exception e) {
            log.error("删除当天打卡记录失败: date={}", date, e);
            throw new RuntimeException("删除当天打卡记录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] exportDailyCheckinToExcel(DailyCheckinSummaryResponse summary) {
        try {
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("每日打卡汇总");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "姓名", "部门", "打卡状态", "签到时间", "签退时间", 
                               "是否迟到", "迟到分钟", "持续时长(分钟)", "请假类型", "备注"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            int rowNum = 1;
            for (UserAttendanceStatusResponse userStatus : summary.getUserStatuses()) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(rowNum - 1); // 序号
                row.createCell(1).setCellValue(userStatus.getUserName() != null ? userStatus.getUserName() : ""); // 姓名
                row.createCell(2).setCellValue(userStatus.getDepartmentName() != null ? userStatus.getDepartmentName() : ""); // 部门
                row.createCell(3).setCellValue(getStatusText(userStatus.getStatus())); // 打卡状态
                
                // 签到时间
                if (userStatus.getCheckinTime() != null) {
                    row.createCell(4).setCellValue(userStatus.getCheckinTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } else {
                    row.createCell(4).setCellValue("");
                }
                
                // 签退时间
                if (userStatus.getCheckoutTime() != null) {
                    row.createCell(5).setCellValue(userStatus.getCheckoutTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } else {
                    row.createCell(5).setCellValue("");
                }
                
                // 是否迟到
                row.createCell(6).setCellValue(userStatus.getIsLate() != null && userStatus.getIsLate() ? "是" : "否");
                
                // 迟到分钟
                row.createCell(7).setCellValue(userStatus.getLateMinutes() != null ? userStatus.getLateMinutes() : 0);
                
                // 持续时长
                row.createCell(8).setCellValue(userStatus.getDurationMinutes() != null ? userStatus.getDurationMinutes() : 0);
                
                // 请假类型
                row.createCell(9).setCellValue(userStatus.getLeaveType() != null ? userStatus.getLeaveType() : "");
                
                // 备注
                row.createCell(10).setCellValue(userStatus.getRemark() != null ? userStatus.getRemark() : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成Excel文件失败", e);
            throw new RuntimeException("生成Excel文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(UserAttendanceStatusResponse.AttendanceStatus status) {
        if (status == null) return "未知";
        switch (status) {
            case CHECKED_IN: return "已签到";
            case ON_LEAVE: return "请假";
            case ABSENT: return "缺勤";
            default: return "未知";
        }
    }
}
