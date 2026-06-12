package com.example.photography.service.impl;

import com.example.photography.dto.response.AttendanceStatisticsResponse;
import com.example.photography.dto.response.AttendanceStatisticsResponse.UserAttendanceDetail;
import com.example.photography.dto.response.AttendanceStatisticsResponse.AttendanceStatus;
import com.example.photography.model.entity.*;
import com.example.photography.repository.*;
import com.example.photography.service.AttendanceService;
import com.example.photography.utils.CheckinWeekdayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤服务实现类
 */
@Slf4j
@Service
public class AttendanceServiceImpl implements AttendanceService {
    
    @Autowired
    private CheckinConfigurationRepository configRepository;
    
    @Autowired
    private CheckinRecordRepository checkinRecordRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Override
    @Transactional(readOnly = true)
    public AttendanceStatisticsResponse getAttendanceStatistics(Long configId, LocalDate date) {
        log.info("获取考勤统计: configId={}, date={}", configId, date);
        
        // 查询配置信息（包含需要打卡的用户）
        CheckinConfiguration config = configRepository.findByIdWithUsers(configId)
            .orElseThrow(() -> new RuntimeException("打卡配置不存在"));

        if (!CheckinWeekdayUtils.isRequiredOnDate(config.getRequiredWeekdays(), date)) {
            return AttendanceStatisticsResponse.builder()
                .configId(config.getId())
                .configName(config.getName())
                .statisticsDate(date)
                .requiredCount(0)
                .actualCount(0)
                .leaveCount(0)
                .absentCount(0)
                .attendanceRate(0.0)
                .userDetails(Collections.emptyList())
                .build();
        }
        
        // 获取当日的打卡记录
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<CheckinRecord> checkinRecords = checkinRecordRepository
            .findByConfigurationIdAndCheckinTimeBetween(configId, startOfDay, endOfDay);
        
        // 获取当日的请假记录
        List<LeaveRequest> leaveRequests = leaveRequestRepository
            .findApprovedLeavesByDate(date)
            .stream()
            .filter(leaveRequest -> appliesToCheckinConfiguration(leaveRequest, configId))
            .toList();
        
        // 构建考勤统计
        return buildAttendanceStatistics(config, date, checkinRecords, leaveRequests);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceStatisticsResponse> getAttendanceStatistics(Long configId, LocalDate startDate, LocalDate endDate) {
        log.info("获取考勤统计范围: configId={}, startDate={}, endDate={}", configId, startDate, endDate);
        
        List<AttendanceStatisticsResponse> results = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            AttendanceStatisticsResponse statistics = getAttendanceStatistics(configId, currentDate);
            results.add(statistics);
            currentDate = currentDate.plusDays(1);
        }
        
        return results;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceStatisticsResponse> getAllAttendanceStatistics(LocalDate date) {
        log.info("获取所有配置的考勤统计: date={}", date);
        
        List<CheckinConfiguration> configs = configRepository.findAllActiveWithUsers();
        return configs.stream()
            .filter(config -> CheckinWeekdayUtils.isRequiredOnDate(config.getRequiredWeekdays(), date))
            .map(config -> getAttendanceStatistics(config.getId(), date))
            .collect(Collectors.toList());
    }
    
    /**
     * 构建考勤统计数据
     */
    private AttendanceStatisticsResponse buildAttendanceStatistics(
            CheckinConfiguration config, 
            LocalDate date,
            List<CheckinRecord> checkinRecords,
            List<LeaveRequest> leaveRequests) {
        
        Set<User> requiredUsers = config.getRequiredUsers();
        Map<Long, CheckinRecord> userCheckinMap = checkinRecords.stream()
            .collect(Collectors.toMap(
                record -> record.getUser().getId(),
                record -> record,
                (existing, replacement) -> existing // 如果有多条记录，保留第一条
            ));
        
        Map<Long, LeaveRequest> userLeaveMap = leaveRequests.stream()
            .collect(Collectors.toMap(
                leave -> leave.getUser().getId(),
                leave -> leave,
                (existing, replacement) -> existing
            ));
        
        List<UserAttendanceDetail> userDetails = new ArrayList<>();
        int presentCount = 0;
        int leaveCount = 0;
        int absentCount = 0;
        
        // 遍历所有需要打卡的用户
        for (User user : requiredUsers) {
            UserAttendanceDetail detail = buildUserAttendanceDetail(
                user, userCheckinMap.get(user.getId()), userLeaveMap.get(user.getId()), config);
            userDetails.add(detail);
            
            // 统计各种状态的人数
            switch (detail.getStatus()) {
                case PRESENT:
                case LATE:
                    presentCount++;
                    break;
                case LEAVE:
                    leaveCount++;
                    break;
                case ABSENT:
                    absentCount++;
                    break;
            }
        }
        
        // 计算出勤率（出勤人数 / (总人数 - 请假人数) * 100）
        double attendanceRate = 0.0;
        int totalRequired = requiredUsers.size();
        if (totalRequired > leaveCount) {
            attendanceRate = (double) presentCount / (totalRequired - leaveCount) * 100;
        }
        
        return AttendanceStatisticsResponse.builder()
            .configId(config.getId())
            .configName(config.getName())
            .statisticsDate(date)
            .requiredCount(totalRequired)
            .actualCount(presentCount)
            .leaveCount(leaveCount)
            .absentCount(absentCount)
            .attendanceRate(Math.round(attendanceRate * 100.0) / 100.0) // 保留两位小数
            .userDetails(userDetails)
            .build();
    }
    
    /**
     * 构建用户考勤详情
     */
    private UserAttendanceDetail buildUserAttendanceDetail(
            User user, 
            CheckinRecord checkinRecord, 
            LeaveRequest leaveRequest,
            CheckinConfiguration config) {
        
        UserAttendanceDetail.UserAttendanceDetailBuilder builder = UserAttendanceDetail.builder()
            .userId(user.getId())
            .userName(user.getRealName())
            .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null);
        
        // 优先检查请假状态
        if (leaveRequest != null && leaveRequest.getStatus() == LeaveRequest.RequestStatus.APPROVED) {
            return builder
                .status(AttendanceStatus.LEAVE)
                .leaveType(leaveRequest.getLeaveType().getDescription())
                .leaveReason(leaveRequest.getReason())
                .build();
        }
        
        // 检查打卡状态
        if (checkinRecord != null) {
            boolean isLate = checkinRecord.getIsLate() != null && checkinRecord.getIsLate();
            return builder
                .status(isLate ? AttendanceStatus.LATE : AttendanceStatus.PRESENT)
                .checkinTime(checkinRecord.getCheckinTime())
                .isLate(isLate)
                .lateMinutes(checkinRecord.getLateMinutes())
                .build();
        }
        
        // 缺勤状态
        return builder
            .status(AttendanceStatus.ABSENT)
            .build();
    }

    private boolean appliesToCheckinConfiguration(LeaveRequest request, Long checkinConfigurationId) {
        if (request.getLeaveType() == LeaveRequest.LeaveType.OTHER) {
            return true;
        }
        return request.getLeaveType() == LeaveRequest.LeaveType.CHECKIN_LEAVE
                && request.getCheckinConfigurationId() != null
                && request.getCheckinConfigurationId().equals(checkinConfigurationId);
    }
}
