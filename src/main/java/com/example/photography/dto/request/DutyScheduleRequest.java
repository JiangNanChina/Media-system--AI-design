package com.example.photography.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/**
 * 执勤排班请求DTO
 */
public class DutyScheduleRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几必须在1-7之间")
    @Max(value = 7, message = "星期几必须在1-7之间")
    private Integer dayOfWeek; // 1-7 表示周一到周日
    
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;
    
    private Boolean active = true;
    
    private String notes;
    
    @Min(value = 0, message = "提前签到时间不能为负数")
    @Max(value = 180, message = "提前签到时间不能超过180分钟")
    private Integer earlyCheckinMinutes = 30; // 允许提前签到的分钟数，默认30分钟
    
    @Min(value = 0, message = "延迟签到时间不能为负数")
    @Max(value = 120, message = "延迟签到时间不能超过120分钟")
    private Integer lateCheckinMinutes = 15; // 允许延迟签到的分钟数，默认15分钟
    
    // Constructors
    public DutyScheduleRequest() {}
    
    public DutyScheduleRequest(Long userId, Integer dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.userId = userId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
