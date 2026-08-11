package com.example.photography.service;

import com.example.photography.model.entity.LeaveRequest;
import com.example.photography.model.entity.JoinApplication;

/**
 * 邮件提醒服务
 */
public interface EmailNotificationService {
    void sendTestMail(String email);

    void notifyLeaveApproval(LeaveRequest leaveRequest);

    void notifyLeaveApprovedToApplicant(LeaveRequest leaveRequest);

    NotificationResult notifyJoinApplicationInterview(JoinApplication joinApplication);

    void sendDutyReminders();

    void sendCheckinReminders();

    void sendBorrowOverdueReminders();

    record NotificationResult(boolean success, String errorMessage) {
        public static NotificationResult ok() {
            return new NotificationResult(true, null);
        }

        public static NotificationResult fail(String errorMessage) {
            return new NotificationResult(false, errorMessage);
        }
    }
}
