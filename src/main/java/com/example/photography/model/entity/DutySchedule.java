package com.example.photography.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/**
 * 办公室执勤排班实体
 */
@Entity
@Table(name = "duty_schedules",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "day_of_week", "start_time"}))
public class DutySchedule extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "执勤用户不能为空")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "borrowRecords", "dutyRecords", "leaveRequests"})
    private User user;
    
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几必须在1-7之间")
    @Max(value = 7, message = "星期几必须在1-7之间")
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1-7 表示周一到周日
    
    @NotNull(message = "开始时间不能为空")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true; // 是否启用
    
    @Column(name = "notes", length = 500)
    private String notes; // 备注
    
    @Column(name = "early_checkin_minutes", nullable = false)
    private Integer earlyCheckinMinutes = 30; // 允许提前签到的分钟数，默认30分钟
    
    @Column(name = "late_checkin_minutes", nullable = false)
    private Integer lateCheckinMinutes = 15; // 允许延迟签到的分钟数，默认15分钟
    
    // Constructors
    public DutySchedule() {}
    
    public DutySchedule(User user, Integer dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.user = user;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Integer getEarlyCheckinMinutes() {
        return earlyCheckinMinutes;
    }
    
    public void setEarlyCheckinMinutes(Integer earlyCheckinMinutes) {
        this.earlyCheckinMinutes = earlyCheckinMinutes;
    }
    
    public Integer getLateCheckinMinutes() {
        return lateCheckinMinutes;
    }
    
    public void setLateCheckinMinutes(Integer lateCheckinMinutes) {
        this.lateCheckinMinutes = lateCheckinMinutes;
    }
}
