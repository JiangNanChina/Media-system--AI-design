package com.example.photography.service.impl;

import com.example.photography.model.entity.BorrowRecord;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.model.entity.DutySchedule;
import com.example.photography.model.entity.EmailNotificationLog;
import com.example.photography.model.entity.Equipment;
import com.example.photography.model.entity.LeaveRequest;
import com.example.photography.model.entity.SiteConfig;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.BorrowRecordRepository;
import com.example.photography.repository.CheckinConfigurationRepository;
import com.example.photography.repository.DutyScheduleRepository;
import com.example.photography.repository.EmailNotificationLogRepository;
import com.example.photography.repository.LeaveRequestRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.EmailDeliveryService;
import com.example.photography.service.EmailNotificationService;
import com.example.photography.utils.CheckinWeekdayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 邮件提醒服务实现。
 */
@Service
@Slf4j
@Transactional
public class EmailNotificationServiceImpl implements EmailNotificationService {
    private static final String TYPE_DUTY_REMINDER = "DUTY_REMINDER";
    private static final String TYPE_CHECKIN_REMINDER = "CHECKIN_REMINDER";
    private static final String TYPE_LEAVE_APPROVAL = "LEAVE_APPROVAL";
    private static final String TYPE_LEAVE_APPROVED = "LEAVE_APPROVED";
    private static final String TYPE_BORROW_OVERDUE = "BORROW_OVERDUE";
    private static final String TYPE_TEST_MAIL = "TEST_MAIL";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TEST_PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Autowired
    private MailSettingsService mailSettingsService;

    @Autowired
    private EmailDeliveryService emailDeliveryService;

    @Autowired
    private EmailNotificationLogRepository emailNotificationLogRepository;

    @Autowired
    private DutyScheduleRepository dutyScheduleRepository;

    @Autowired
    private CheckinConfigurationRepository checkinConfigurationRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(noRollbackFor = RuntimeException.class)
    public void sendTestMail(String email) {
        String recipientEmail = normalizeEmail(email);
        LocalDateTime sentAt = LocalDateTime.now();
        EmailNotificationLog logEntity = new EmailNotificationLog();
        logEntity.setNotificationType(TYPE_TEST_MAIL);
        logEntity.setBusinessType("MAIL_TEST");
        logEntity.setBusinessId(0L);
        logEntity.setRecipientEmail(recipientEmail);
        logEntity.setRecipientName("测试收件人");
        logEntity.setPeriodKey("test-" + sentAt.format(TEST_PERIOD_FORMATTER));
        logEntity.setSentAt(sentAt);

        try {
            emailDeliveryService.sendTestMail(recipientEmail);
            logEntity.setSuccess(true);
        } catch (RuntimeException e) {
            logEntity.setSuccess(false);
            logEntity.setErrorMessage(truncate(e.getMessage(), 1000));
            emailNotificationLogRepository.save(logEntity);
            throw e;
        }

        emailNotificationLogRepository.save(logEntity);
    }

    @Override
    public void notifyLeaveApproval(LeaveRequest leaveRequest) {
        if (leaveRequest == null || leaveRequest.getId() == null || !isNotificationAvailable(SiteConfig.Keys.MAIL_LEAVE_APPROVAL_REMINDER_ENABLED)) {
            return;
        }

        LeaveRequest request = leaveRequestRepository.findByIdWithUser(leaveRequest.getId()).orElse(leaveRequest);
        List<User> approvers = findLeaveApprovers(request);
        if (approvers.isEmpty()) {
            log.info("请假审批提醒没有可用审批人: leaveRequestId={}", request.getId());
            return;
        }

        String applicantName = request.getUser() != null ? request.getUser().getRealName() : "成员";
        String subject = "请假审批提醒：" + applicantName;
        String content = buildMailContent(
                "请假审批提醒",
                "申请人：" + applicantName,
                "请假类型：" + (request.getLeaveType() != null ? request.getLeaveType().getDescription() : "-"),
                "请假日期：" + request.getStartDate() + " 至 " + request.getEndDate(),
                "申请原因：" + request.getReason(),
                "请及时登录系统完成审批。"
        );

        for (User approver : approvers) {
            sendNotificationOnce(TYPE_LEAVE_APPROVAL, "LEAVE_REQUEST", request.getId(), approver, "approval", subject, content);
        }
    }

    @Override
    public void notifyLeaveApprovedToApplicant(LeaveRequest leaveRequest) {
        if (leaveRequest == null || leaveRequest.getId() == null
                || !isNotificationAvailable(SiteConfig.Keys.MAIL_LEAVE_APPROVAL_REMINDER_ENABLED)) {
            return;
        }

        LeaveRequest request = leaveRequestRepository.findByIdWithUser(leaveRequest.getId()).orElse(leaveRequest);
        if (request.getStatus() != LeaveRequest.RequestStatus.APPROVED) {
            return;
        }

        User applicant = request.getUser();
        if (!isActiveRecipient(applicant)) {
            return;
        }

        String approverName = request.getApprover() != null ? safe(request.getApprover().getRealName()) : "系统";
        String subject = "请假申请已通过";
        String content = buildMailContent(
                "请假申请已通过",
                "申请人：" + safe(applicant.getRealName()),
                "请假类型：" + (request.getLeaveType() != null ? request.getLeaveType().getDescription() : "-"),
                "请假日期：" + request.getStartDate() + " 至 " + request.getEndDate(),
                "申请原因：" + safe(request.getReason()),
                "审批人：" + approverName,
                "审批备注：" + safe(request.getApproveNotes()),
                "请按已批准的请假安排执行。"
        );

        sendNotificationOnce(TYPE_LEAVE_APPROVED, "LEAVE_REQUEST", request.getId(), applicant, "approved", subject, content);
    }

    @Override
    public void sendDutyReminders() {
        if (!isNotificationAvailable(SiteConfig.Keys.MAIL_DUTY_REMINDER_ENABLED)) {
            return;
        }

        LocalDateTime windowStart = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime windowEnd = windowStart.plusMinutes(1);
        int advanceMinutes = mailSettingsService.getReminderAdvanceMinutes();
        LocalDate targetDate = windowStart.plusMinutes(advanceMinutes).toLocalDate();
        int dayOfWeek = targetDate.getDayOfWeek().getValue();

        List<DutySchedule> schedules = dutyScheduleRepository.findByDayOfWeekAndActiveTrueAndDeletedFalse(dayOfWeek);
        for (DutySchedule schedule : schedules) {
            if (schedule.getStartTime() == null || !isReminderInWindow(targetDate, schedule.getStartTime(), advanceMinutes, windowStart, windowEnd)) {
                continue;
            }

            User recipient = schedule.getUser();
            if (!isActiveRecipient(recipient) || hasApprovedDutyLeave(recipient, targetDate, schedule.getId())) {
                continue;
            }

            String periodKey = targetDate.format(DATE_FORMATTER) + "#" + schedule.getStartTime();
            String subject = "执勤提醒：" + formatTime(schedule.getStartTime());
            String content = buildMailContent(
                    "执勤提醒",
                    "成员：" + recipient.getRealName(),
                    "执勤日期：" + targetDate.format(DATE_FORMATTER),
                    "执勤时间：" + formatTime(schedule.getStartTime()) + " - " + formatTime(schedule.getEndTime()),
                    "请按时到岗并完成执勤打卡。"
            );

            sendNotificationOnce(TYPE_DUTY_REMINDER, "DUTY_SCHEDULE", schedule.getId(), recipient, periodKey, subject, content);
        }
    }

    @Override
    public void sendCheckinReminders() {
        if (!isNotificationAvailable(SiteConfig.Keys.MAIL_CHECKIN_REMINDER_ENABLED)) {
            return;
        }

        LocalDateTime windowStart = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime windowEnd = windowStart.plusMinutes(1);
        int advanceMinutes = mailSettingsService.getReminderAdvanceMinutes();
        LocalDate targetDate = windowStart.plusMinutes(advanceMinutes).toLocalDate();

        List<CheckinConfiguration> configurations = checkinConfigurationRepository.findAllActiveWithUsers();
        for (CheckinConfiguration configuration : configurations) {
            if (!CheckinWeekdayUtils.isRequiredOnDate(configuration.getRequiredWeekdays(), targetDate)) {
                continue;
            }

            if (configuration.getStartTime() == null || !isReminderInWindow(targetDate, configuration.getStartTime(), advanceMinutes, windowStart, windowEnd)) {
                continue;
            }

            List<User> recipients = resolveCheckinRecipients(configuration);
            String periodKey = targetDate.format(DATE_FORMATTER) + "#" + configuration.getId() + "#" + configuration.getStartTime();
            String subject = "晚自习打卡提醒：" + safe(configuration.getSessionName());
            String content = buildMailContent(
                    "晚自习打卡提醒",
                    "打卡配置：" + safe(configuration.getName()),
                    "打卡时段：" + safe(configuration.getSessionName()),
                    "打卡日期：" + targetDate.format(DATE_FORMATTER),
                    "开始时间：" + formatTime(configuration.getStartTime()),
                    "地点：" + safe(configuration.getLocationName()),
                    "请按时完成晚自习打卡。"
            );

            for (User recipient : recipients) {
                if (!isActiveRecipient(recipient) || hasApprovedCheckinLeave(recipient, targetDate, configuration.getId())) {
                    continue;
                }
                sendNotificationOnce(TYPE_CHECKIN_REMINDER, "CHECKIN_CONFIGURATION", configuration.getId(), recipient, periodKey, subject, content);
            }
        }
    }

    @Override
    public void sendBorrowOverdueReminders() {
        if (!isNotificationAvailable(SiteConfig.Keys.MAIL_BORROW_OVERDUE_REMINDER_ENABLED)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int intervalHours = mailSettingsService.getOverdueReminderIntervalHours();
        String periodKey = buildOverduePeriodKey(now, intervalHours);

        List<BorrowRecord> records = borrowRecordRepository.findOverdueRecords(now);
        for (BorrowRecord record : records) {
            User borrower = record.getUser();
            if (borrower == null || !StringUtils.hasText(borrower.getEmail())) {
                continue;
            }

            Equipment equipment = record.getEquipment();
            String equipmentName = equipment != null ? equipment.getName() : "借用设备";
            String subject = "设备归还逾期提醒：" + equipmentName;
            String content = buildMailContent(
                    "设备归还逾期提醒",
                    "借用人：" + safe(borrower.getRealName()),
                    "设备名称：" + safe(equipmentName),
                    "设备编号：" + safe(equipment != null ? equipment.getSerialNumber() : "-"),
                    "预计归还时间：" + (record.getExpectedReturnTime() != null ? record.getExpectedReturnTime().format(DATETIME_FORMATTER) : "-"),
                    "该设备已超过预计归还时间，请尽快归还。"
            );

            sendNotificationOnce(TYPE_BORROW_OVERDUE, "BORROW_RECORD", record.getId(), borrower, periodKey, subject, content);
        }
    }

    private boolean isNotificationAvailable(String featureKey) {
        if (!mailSettingsService.isFeatureEnabled(featureKey)) {
            return false;
        }
        if (!mailSettingsService.isMailEnabled()) {
            return false;
        }
        if (!mailSettingsService.isMailConfigured()) {
            log.warn("邮件提醒已启用但SMTP配置不完整，跳过本轮提醒");
            return false;
        }
        return true;
    }

    private List<User> findLeaveApprovers(LeaveRequest leaveRequest) {
        User applicant = leaveRequest.getUser();
        Long applicantId = applicant != null ? applicant.getId() : null;
        Long departmentId = applicant != null && applicant.getDepartment() != null ? applicant.getDepartment().getId() : null;

        List<User> candidates = new ArrayList<>();
        if (departmentId != null) {
            candidates.addAll(userRepository.findByDepartmentIdAndRoleAndDeletedFalse(departmentId, UserRole.ADMIN));
        }

        List<User> sameDepartmentApprovers = filterApprovers(candidates, applicantId);
        if (!sameDepartmentApprovers.isEmpty()) {
            return sameDepartmentApprovers;
        }

        return filterApprovers(userRepository.findByRoleAndDeletedFalse(UserRole.ADMIN), applicantId);
    }

    private List<User> filterApprovers(List<User> users, Long applicantId) {
        Map<Long, User> uniqueUsers = new LinkedHashMap<>();
        for (User user : users) {
            if (user == null || user.getId() == null || user.getId().equals(applicantId)) {
                continue;
            }
            if (!Boolean.TRUE.equals(user.getEnabled()) || !StringUtils.hasText(user.getEmail())) {
                continue;
            }
            uniqueUsers.putIfAbsent(user.getId(), user);
        }
        return new ArrayList<>(uniqueUsers.values());
    }

    private List<User> resolveCheckinRecipients(CheckinConfiguration configuration) {
        Set<User> requiredUsers = configuration.getRequiredUsers();
        if (requiredUsers == null || requiredUsers.isEmpty()) {
            return userRepository.findByEnabledTrueAndDeletedFalseOrderByRealNameAsc();
        }
        return new ArrayList<>(requiredUsers);
    }

    private boolean isActiveRecipient(User user) {
        return user != null
                && Boolean.TRUE.equals(user.getEnabled())
                && StringUtils.hasText(user.getEmail());
    }

    private boolean hasApprovedCheckinLeave(User user, LocalDate date, Long checkinConfigurationId) {
        if (user == null || date == null) {
            return false;
        }
        return leaveRequestRepository.findApprovedLeaveForUserAndDate(user, date).stream()
                .anyMatch(request -> request.getLeaveType() == LeaveRequest.LeaveType.OTHER
                        || (request.getLeaveType() == LeaveRequest.LeaveType.CHECKIN_LEAVE
                        && request.getCheckinConfigurationId() != null
                        && request.getCheckinConfigurationId().equals(checkinConfigurationId)));
    }

    private boolean hasApprovedDutyLeave(User user, LocalDate date, Long dutyScheduleId) {
        if (user == null || date == null) {
            return false;
        }
        return leaveRequestRepository.findApprovedLeaveForUserAndDate(user, date).stream()
                .anyMatch(request -> request.getLeaveType() == LeaveRequest.LeaveType.OTHER
                        || (request.getLeaveType() == LeaveRequest.LeaveType.DUTY_LEAVE
                        && containsDutyScheduleId(request.getDutyScheduleIds(), dutyScheduleId)));
    }

    private boolean containsDutyScheduleId(String dutyScheduleIds, Long dutyScheduleId) {
        if (!StringUtils.hasText(dutyScheduleIds) || dutyScheduleId == null) {
            return false;
        }
        for (String idText : dutyScheduleIds.split(",")) {
            if (String.valueOf(dutyScheduleId).equals(idText.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isReminderInWindow(LocalDate targetDate, LocalTime startTime, int advanceMinutes,
                                       LocalDateTime windowStart, LocalDateTime windowEnd) {
        LocalDateTime reminderAt = LocalDateTime.of(targetDate, startTime).minusMinutes(advanceMinutes);
        return !reminderAt.isBefore(windowStart) && reminderAt.isBefore(windowEnd);
    }

    private String buildOverduePeriodKey(LocalDateTime now, int intervalHours) {
        if (intervalHours >= 24) {
            long bucket = now.toLocalDate().toEpochDay() / Math.max(1, intervalHours / 24);
            return "day-" + bucket;
        }
        int bucket = now.getHour() / intervalHours;
        return now.toLocalDate().format(DATE_FORMATTER) + "#hourBucket-" + bucket;
    }

    private void sendNotificationOnce(String notificationType, String businessType, Long businessId, User recipient,
                                      String periodKey, String subject, String content) {
        String recipientEmail = normalizeEmail(recipient.getEmail());
        if (emailNotificationLogRepository.existsSuccessfulLog(notificationType, businessId, recipientEmail, periodKey)) {
            return;
        }

        EmailNotificationLog logEntity = new EmailNotificationLog();
        logEntity.setNotificationType(notificationType);
        logEntity.setBusinessType(businessType);
        logEntity.setBusinessId(businessId);
        logEntity.setRecipientEmail(recipientEmail);
        logEntity.setRecipientName(recipient.getRealName());
        logEntity.setPeriodKey(periodKey);
        logEntity.setSentAt(LocalDateTime.now());

        try {
            emailDeliveryService.sendHtmlMail(recipientEmail, subject, content);
            logEntity.setSuccess(true);
        } catch (Exception e) {
            logEntity.setSuccess(false);
            logEntity.setErrorMessage(truncate(e.getMessage(), 1000));
            log.warn("邮件提醒发送失败: type={}, businessId={}, recipient={}", notificationType, businessId, recipientEmail, e);
        }

        emailNotificationLogRepository.save(logEntity);
    }

    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String buildMailContent(String title, String... lines) {
        return EmailTemplateRenderer.renderNotification(title, lines);
    }

    private String formatTime(LocalTime time) {
        return time == null ? "-" : time.format(TIME_FORMATTER);
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
