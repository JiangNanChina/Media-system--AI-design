package com.example.photography.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 办公室执勤记录实体
 */
@Entity
@Table(name = "duty_records")
public class DutyRecord extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "执勤用户不能为空")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duty_schedule_id", nullable = false)
    @NotNull(message = "执勤排班不能为空")
    private DutySchedule dutySchedule;
    
    @NotNull(message = "执勤日期不能为空")
    @Column(name = "duty_date", nullable = false)
    private LocalDate dutyDate;
    
    @Column(name = "checkin_time")
    private LocalDateTime checkinTime; // 签到时间
    
    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime; // 签退时间
    
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime; // 实际开始时间
    
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime; // 实际结束时间
    
    @Column(name = "status", length = 20)
    private String status = "待执勤"; // 执勤状态：待执勤、执勤中、已完成、缺勤、请假
    
    @Column(name = "notes", length = 500)
    private String notes; // 备注
    
    // Constructors
    public DutyRecord() {}
    
    public DutyRecord(User user, DutySchedule dutySchedule, LocalDate dutyDate) {
        this.user = user;
        this.dutySchedule = dutySchedule;
        this.dutyDate = dutyDate;
    }
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public DutySchedule getDutySchedule() {
        return dutySchedule;
    }
    
    public void setDutySchedule(DutySchedule dutySchedule) {
        this.dutySchedule = dutySchedule;
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
    
    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }
    
    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }
    
    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }
    
    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
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
}
