package com.example.photography.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 办公室执勤记录简要响应 DTO
 * 用于“我的执勤记录/最近记录”等场景，避免直接暴露实体并触发懒加载问题
 */
public class DutyRecordSimpleResponse {

    private Long id;

    /**
     * 执勤用户ID（管理员视图使用）
     */
    private Long userId;

    /**
     * 执勤用户姓名（管理员/个人视图通用）
     */
    private String userRealName;

    /**
     * 执勤用户登录名（可选，便于管理员搜索/排查）
     */
    private String username;

    /**
     * 执勤用户所属部门名称（管理员视图使用）
     */
    private String departmentName;

    /**
     * 执勤日期
     */
    private LocalDate dutyDate;

    /**
     * 签到时间
     */
    private LocalDateTime checkinTime;

    /**
     * 签退时间
     */
    private LocalDateTime checkoutTime;

    /**
     * 计划开始时间（来自排班）
     */
    private LocalTime startTime;

    /**
     * 计划结束时间（来自排班）
     */
    private LocalTime endTime;

    /**
     * 排班ID（管理员视图使用，用于后续追踪排班来源）
     */
    private Long dutyScheduleId;

    /**
     * 执勤状态：待执勤、执勤中、已完成、缺勤、请假
     */
    private String status;

    /**
     * 备注（例如请假审批信息）
     */
    private String notes;

    /**
     * 最近更新时间（用于界面展示审核时间等）
     */
    private LocalDateTime updatedAt;

    public DutyRecordSimpleResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDate getDutyDate() {
        return dutyDate;
    }

    public void setDutyDate(LocalDate dutyDate) {
        this.dutyDate = dutyDate;
    }

    public LocalDateTime getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(LocalDateTime checkinTime) {
        this.checkinTime = checkinTime;
    }

    public LocalDateTime getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(LocalDateTime checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Long getDutyScheduleId() {
        return dutyScheduleId;
    }

    public void setDutyScheduleId(Long dutyScheduleId) {
        this.dutyScheduleId = dutyScheduleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


