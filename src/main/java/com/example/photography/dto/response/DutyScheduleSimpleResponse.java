package com.example.photography.dto.response;

import java.time.LocalTime;

/**
 * 执勤排班简要响应 DTO
 * 仅包含前端展示当前排班所需的基础字段，避免懒加载关联（如 User）导致的序列化问题
 */
public class DutyScheduleSimpleResponse {

    private Long id;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean active;
    private String notes;
    private Integer earlyCheckinMinutes;
    private Integer lateCheckinMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


