package com.example.photography.dto.response;

import com.example.photography.model.entity.EmailNotificationLog;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件发送日志响应。
 */
@Data
public class EmailNotificationLogResponse {
    private Long id;
    private String notificationType;
    private String notificationTypeDescription;
    private String businessType;
    private String businessTypeDescription;
    private Long businessId;
    private String recipientEmail;
    private String recipientName;
    private String periodKey;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public static EmailNotificationLogResponse fromEntity(EmailNotificationLog entity) {
        EmailNotificationLogResponse response = new EmailNotificationLogResponse();
        response.setId(entity.getId());
        response.setNotificationType(entity.getNotificationType());
        response.setNotificationTypeDescription(describeNotificationType(entity.getNotificationType()));
        response.setBusinessType(entity.getBusinessType());
        response.setBusinessTypeDescription(describeBusinessType(entity.getBusinessType()));
        response.setBusinessId(entity.getBusinessId());
        response.setRecipientEmail(entity.getRecipientEmail());
        response.setRecipientName(entity.getRecipientName());
        response.setPeriodKey(entity.getPeriodKey());
        response.setSuccess(entity.getSuccess());
        response.setErrorMessage(entity.getErrorMessage());
        response.setSentAt(entity.getSentAt());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    private static String describeNotificationType(String type) {
        if (type == null) {
            return "-";
        }
        return switch (type) {
            case "TEST_MAIL" -> "测试邮件";
            case "REGISTER_CODE" -> "注册验证码";
            case "DUTY_REMINDER" -> "执勤提醒";
            case "CHECKIN_REMINDER" -> "晚自习打卡提醒";
            case "LEAVE_APPROVAL" -> "请假审批提醒";
            case "LEAVE_APPROVED" -> "请假通过通知";
            case "BORROW_OVERDUE" -> "设备逾期提醒";
            default -> type;
        };
    }

    private static String describeBusinessType(String type) {
        if (type == null) {
            return "-";
        }
        return switch (type) {
            case "MAIL_TEST" -> "测试发送";
            case "EMAIL_VERIFICATION_CODE" -> "邮箱验证码";
            case "DUTY_SCHEDULE" -> "执勤排班";
            case "CHECKIN_CONFIGURATION" -> "打卡配置";
            case "LEAVE_REQUEST" -> "请假申请";
            case "BORROW_RECORD" -> "设备借用";
            default -> type;
        };
    }
}
