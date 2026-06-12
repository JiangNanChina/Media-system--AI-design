package com.example.photography.service;

import com.example.photography.model.entity.LeaveRequest;

/**
 * 邮件提醒服务
 */
public interface EmailNotificationService {
    void sendTestMail(String email);

    void notifyLeaveApproval(LeaveRequest leaveRequest);

    void notifyLeaveApprovedToApplicant(LeaveRequest leaveRequest);

    void sendDutyReminders();

    void sendCheckinReminders();

    void sendBorrowOverdueReminders();
}
