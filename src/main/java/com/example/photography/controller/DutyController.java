package com.example.photography.controller;

import com.example.photography.dto.request.DutyCheckinRequest;
import com.example.photography.dto.request.DutyScheduleRequest;
import com.example.photography.dto.request.DutySwapRequestCreateRequest;
import com.example.photography.dto.request.DutySwapRequestDecisionRequest;
import com.example.photography.dto.response.DutyRecordSimpleResponse;
import com.example.photography.dto.response.DutyScheduleSimpleResponse;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.model.entity.DutyRecord;
import com.example.photography.model.entity.DutySchedule;
import com.example.photography.model.entity.DutySwapRequest;
import com.example.photography.service.DutyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 办公室执勤控制器
 */
@RestController
@RequestMapping("/duty")
@Tag(name = "办公室执勤", description = "办公室执勤排班管理、执勤打卡、记录查询等操作")
public class DutyController {
    
    @Autowired
    private DutyService dutyService;
    
    // ========== 执勤排班管理 ==========
    
    @GetMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有执勤排班", description = "管理员获取所有执勤排班（仅管理员）")
    public ApiResponse<List<DutySchedule>> getAllDutySchedules(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer dayOfWeek) {
        try {
            List<DutySchedule> schedules = dutyService.searchDutySchedules(keyword, dayOfWeek);
            return ApiResponse.success(schedules);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/schedules/active")
    @Operation(summary = "获取启用的执勤排班", description = "获取所有启用的执勤排班")
    public ApiResponse<List<DutySchedule>> getActiveDutySchedules() {
        try {
            List<DutySchedule> schedules = dutyService.getActiveDutySchedules();
            return ApiResponse.success(schedules);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/schedules/my")
    @Operation(summary = "获取我的执勤排班", description = "获取当前用户的执勤排班")
    public ApiResponse<List<DutySchedule>> getMyDutySchedules() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            List<DutySchedule> schedules = dutyService.getUserDutySchedules(userId);
            return ApiResponse.success(schedules);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/schedules/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取指定用户的执勤排班", description = "管理员获取指定用户的执勤排班（仅管理员）")
    public ApiResponse<List<DutySchedule>> getUserDutySchedules(@PathVariable Long userId) {
        try {
            List<DutySchedule> schedules = dutyService.getUserDutySchedules(userId);
            return ApiResponse.success(schedules);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/schedules/day/{dayOfWeek}")
    @Operation(summary = "根据星期几获取执勤排班", description = "根据星期几获取执勤排班（1-7表示周一到周日）")
    public ApiResponse<List<DutySchedule>> getDutySchedulesByDayOfWeek(@PathVariable Integer dayOfWeek) {
        try {
            List<DutySchedule> schedules = dutyService.getDutySchedulesByDayOfWeek(dayOfWeek);
            return ApiResponse.success(schedules);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/schedules/current")
    @Operation(summary = "获取当前执勤排班", description = "获取当前用户的当前执勤排班")
    public ApiResponse<DutyScheduleSimpleResponse> getCurrentDutySchedule() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            DutySchedule schedule = dutyService.getCurrentDutySchedule(userId);
            if (schedule == null) {
                return ApiResponse.success(null);
            }

            // 映射为简要 DTO，避免懒加载 user 等关联导致的 JSON 序列化问题
            DutyScheduleSimpleResponse dto = new DutyScheduleSimpleResponse();
            dto.setId(schedule.getId());
            dto.setDayOfWeek(schedule.getDayOfWeek());
            dto.setStartTime(schedule.getStartTime());
            dto.setEndTime(schedule.getEndTime());
            dto.setActive(schedule.getActive());
            dto.setNotes(schedule.getNotes());
            dto.setEarlyCheckinMinutes(schedule.getEarlyCheckinMinutes());
            dto.setLateCheckinMinutes(schedule.getLateCheckinMinutes());

            return ApiResponse.success(dto);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/schedules/{id}")
    @Operation(summary = "根据ID获取执勤排班", description = "根据排班ID获取执勤排班详情")
    public ApiResponse<DutySchedule> getDutyScheduleById(@PathVariable Long id) {
        try {
            DutySchedule schedule = dutyService.findDutyScheduleById(id);
            return ApiResponse.success(schedule);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建执勤排班", description = "创建新的执勤排班（仅管理员）")
    public ApiResponse<DutySchedule> createDutySchedule(@Valid @RequestBody DutyScheduleRequest request) {
        try {
            DutySchedule schedule = dutyService.createDutySchedule(request);
            return ApiResponse.success("执勤排班创建成功", schedule);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新执勤排班", description = "更新执勤排班信息（仅管理员）")
    public ApiResponse<DutySchedule> updateDutySchedule(@PathVariable Long id, 
                                                       @Valid @RequestBody DutyScheduleRequest request) {
        try {
            DutySchedule schedule = dutyService.updateDutySchedule(id, request);
            return ApiResponse.success("执勤排班更新成功", schedule);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除执勤排班", description = "删除指定执勤排班（仅管理员）")
    public ApiResponse<Void> deleteDutySchedule(@PathVariable Long id) {
        try {
            dutyService.deleteDutySchedule(id);
            return ApiResponse.success("执勤排班删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/schedules/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "启用/禁用执勤排班", description = "启用或禁用执勤排班（仅管理员）")
    public ApiResponse<Void> toggleDutyScheduleStatus(@PathVariable Long id, 
                                                     @RequestBody Map<String, Boolean> request) {
        try {
            Boolean active = request.get("active");
            if (active == null) {
                return ApiResponse.error("启用状态不能为空");
            }
            
            dutyService.toggleDutyScheduleStatus(id, active);
            return ApiResponse.success(active ? "排班已启用" : "排班已禁用");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ========== 执勤打卡 ==========
    
    @PostMapping("/checkin")
    @Operation(summary = "执勤签到", description = "用户进行执勤签到")
    public ApiResponse<DutyRecordSimpleResponse> checkin(@RequestBody DutyCheckinRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            DutyRecord record = dutyService.checkin(userId, request);
            return ApiResponse.success("签到成功", convertToSimpleDto(record));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 将执勤记录实体转换为简要 DTO，避免懒加载关联在 JSON 序列化时出错
     */
    private DutyRecordSimpleResponse convertToSimpleDto(DutyRecord record) {
        if (record == null) {
            return null;
        }
        DutyRecordSimpleResponse dto = new DutyRecordSimpleResponse();
        dto.setId(record.getId());
        dto.setDutyDate(record.getDutyDate());
        dto.setStatus(record.getStatus());
        dto.setCheckinTime(record.getCheckinTime());
        dto.setCheckoutTime(record.getCheckoutTime());
        dto.setNotes(record.getNotes());
        dto.setUpdatedAt(record.getUpdatedAt());

        if (record.getDutySchedule() != null) {
            dto.setDutyScheduleId(record.getDutySchedule().getId());
            dto.setStartTime(record.getDutySchedule().getStartTime());
            dto.setEndTime(record.getDutySchedule().getEndTime());
        }
        if (record.getUser() != null) {
            dto.setUserId(record.getUser().getId());
            dto.setUserRealName(record.getUser().getRealName());
            dto.setUsername(record.getUser().getUsername());
            if (record.getUser().getDepartment() != null) {
                dto.setDepartmentName(record.getUser().getDepartment().getName());
            }
        }
        return dto;
    }
    
    @PostMapping("/checkout")
    @Operation(summary = "执勤签退", description = "用户进行执勤签退")
    public ApiResponse<DutyRecordSimpleResponse> checkout(@RequestBody DutyCheckinRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            DutyRecord record = dutyService.checkout(userId, request);
            return ApiResponse.success("签退成功", convertToSimpleDto(record));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/can-checkin")
    @Operation(summary = "检查是否可以执勤打卡", description = "检查当前用户是否可以进行执勤打卡")
    public ApiResponse<Boolean> canDutyCheckin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            boolean canCheckin = dutyService.canDutyCheckin(userId);
            return ApiResponse.success("执勤打卡检查完成", canCheckin);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ========== 执勤记录查询 ==========
    
    @GetMapping("/records")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有执勤记录", description = "管理员获取所有执勤记录（仅管理员，返回简要DTO以避免懒加载问题）")
    public ApiResponse<Page<DutyRecordSimpleResponse>> getAllDutyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dutyDate"));
            Page<DutyRecord> records = dutyService.searchDutyRecords(pageable, keyword, status, startDate, endDate);

            // 将实体映射为简要 DTO，包含管理员列表和详情需要的字段
            Page<DutyRecordSimpleResponse> dtoPage = records.map(record -> {
                DutyRecordSimpleResponse dto = new DutyRecordSimpleResponse();
                dto.setId(record.getId());
                dto.setDutyDate(record.getDutyDate());
                dto.setStatus(record.getStatus());
                dto.setCheckinTime(record.getCheckinTime());
                dto.setCheckoutTime(record.getCheckoutTime());
                dto.setNotes(record.getNotes());
                dto.setUpdatedAt(record.getUpdatedAt());

                if (record.getDutySchedule() != null) {
                    dto.setDutyScheduleId(record.getDutySchedule().getId());
                    dto.setStartTime(record.getDutySchedule().getStartTime());
                    dto.setEndTime(record.getDutySchedule().getEndTime());
                }
                if (record.getUser() != null) {
                    dto.setUserId(record.getUser().getId());
                    dto.setUserRealName(record.getUser().getRealName());
                    dto.setUsername(record.getUser().getUsername());
                    if (record.getUser().getDepartment() != null) {
                        dto.setDepartmentName(record.getUser().getDepartment().getName());
                    }
                }
                return dto;
            });

            return ApiResponse.success(dtoPage);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/records/my")
    @Operation(summary = "获取我的执勤记录", description = "获取当前用户的执勤记录（简要信息，避免懒加载问题）")
    public ApiResponse<Page<DutyRecordSimpleResponse>> getMyDutyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dutyDate"));
            Page<DutyRecord> records = dutyService.getUserDutyRecords(userId, pageable);

            // 将实体映射为简要 DTO，只包含前端需要的字段，避免序列化懒加载代理
            Page<DutyRecordSimpleResponse> dtoPage = records.map(record -> {
                DutyRecordSimpleResponse dto = new DutyRecordSimpleResponse();
                dto.setId(record.getId());
                dto.setDutyDate(record.getDutyDate());
                dto.setStatus(record.getStatus());
                dto.setCheckinTime(record.getCheckinTime());
                dto.setCheckoutTime(record.getCheckoutTime());
                dto.setNotes(record.getNotes());
                dto.setUpdatedAt(record.getUpdatedAt());
                if (record.getDutySchedule() != null) {
                    dto.setDutyScheduleId(record.getDutySchedule().getId());
                    dto.setStartTime(record.getDutySchedule().getStartTime());
                    dto.setEndTime(record.getDutySchedule().getEndTime());
                }
                if (record.getUser() != null) {
                    dto.setUserId(record.getUser().getId());
                    dto.setUserRealName(record.getUser().getRealName());
                    dto.setUsername(record.getUser().getUsername());
                    if (record.getUser().getDepartment() != null) {
                        dto.setDepartmentName(record.getUser().getDepartment().getName());
                    }
                }
                return dto;
            });

            return ApiResponse.success(dtoPage);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/records/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取指定用户的执勤记录", description = "管理员获取指定用户的执勤记录（仅管理员）")
    public ApiResponse<Page<DutyRecord>> getUserDutyRecords(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dutyDate"));
            Page<DutyRecord> records = dutyService.getUserDutyRecords(userId, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/records/today")
    @Operation(summary = "获取今日执勤记录", description = "获取当前用户今日的执勤记录（简要信息，避免懒加载问题）")
    public ApiResponse<DutyRecordSimpleResponse> getTodayDutyRecord() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            DutyRecord record = dutyService.getUserDutyRecordByDate(userId, LocalDate.now());
            if (record == null) {
                return ApiResponse.success(null);
            }

            DutyRecordSimpleResponse dto = new DutyRecordSimpleResponse();
            dto.setId(record.getId());
            dto.setDutyDate(record.getDutyDate());
            dto.setStatus(record.getStatus());
            dto.setCheckinTime(record.getCheckinTime());
            dto.setCheckoutTime(record.getCheckoutTime());
            dto.setNotes(record.getNotes());
            dto.setUpdatedAt(record.getUpdatedAt());
            if (record.getDutySchedule() != null) {
                dto.setDutyScheduleId(record.getDutySchedule().getId());
                dto.setStartTime(record.getDutySchedule().getStartTime());
                dto.setEndTime(record.getDutySchedule().getEndTime());
            }
            if (record.getUser() != null) {
                dto.setUserId(record.getUser().getId());
                dto.setUserRealName(record.getUser().getRealName());
                dto.setUsername(record.getUser().getUsername());
                if (record.getUser().getDepartment() != null) {
                    dto.setDepartmentName(record.getUser().getDepartment().getName());
                }
            }

            return ApiResponse.success(dto);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/records/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "生成执勤记录", description = "管理员为指定时间范围生成执勤记录（仅管理员）")
    public ApiResponse<Void> generateDutyRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            dutyService.generateDutyRecords(startDate, endDate);
            return ApiResponse.success("执勤记录生成成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出执勤记录", description = "管理员导出指定时间范围的执勤记录到Excel（仅管理员）")
    public ResponseEntity<byte[]> exportDutyRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] excelData = dutyService.exportDutyRecordsToExcel(startDate, endDate);
            
            String filename = "办公室执勤记录_" + 
                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/records/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除执勤记录", description = "管理员删除执勤记录（仅管理员）")
    public ApiResponse<Void> deleteDutyRecord(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long adminId = (Long) authentication.getDetails();
            System.out.println("管理员 " + adminId + " 正在删除执勤记录: " + id);
            
            dutyService.deleteDutyRecord(id);
            return ApiResponse.success("执勤记录删除成功");
        } catch (Exception e) {
            System.err.println("删除执勤记录失败: " + e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取执勤统计信息", description = "管理员获取执勤统计数据（仅管理员）")
    public ApiResponse<DutyService.DutyStatistics> getDutyStatistics() {
        try {
            DutyService.DutyStatistics statistics = dutyService.getDutyStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== 排班调换管理 ==========

    @PostMapping("/swap-requests")
    @Operation(summary = "发起排班调换申请", description = "当前登录用户发起与他人调换排班的申请")
    public ApiResponse<Void> createDutySwapRequest(@Valid @RequestBody DutySwapRequestCreateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            dutyService.createDutySwapRequest(userId, request);
            return ApiResponse.success("排班调换申请已提交");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/swap-requests/{id}/decision")
    @Operation(summary = "处理排班调换申请", description = "被调换人或管理员同意/拒绝排班调换申请")
    public ApiResponse<Void> handleDutySwapRequest(@PathVariable Long id,
                                                   @Valid @RequestBody DutySwapRequestDecisionRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            dutyService.handleDutySwapRequest(id, userId, request);
            return ApiResponse.success("排班调换申请已处理");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/swap-requests/my")
    @Operation(summary = "获取与我相关的排班调换申请", description = "作为发起人或被调换人的排班调换申请列表")
    public ApiResponse<List<DutySwapRequest>> getMyDutySwapRequests() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            List<DutySwapRequest> list = dutyService.getUserDutySwapRequests(userId);
            return ApiResponse.success(list);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/swap-requests")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有排班调换申请", description = "管理员查看所有排班调换申请")
    public ApiResponse<List<DutySwapRequest>> getAllDutySwapRequests() {
        try {
            List<DutySwapRequest> list = dutyService.getAllDutySwapRequests();
            return ApiResponse.success(list);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/swap-requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除排班调换申请", description = "管理员物理删除排班调换申请记录")
    public ApiResponse<Void> deleteDutySwapRequest(@PathVariable Long id) {
        try {
            dutyService.deleteDutySwapRequest(id);
            return ApiResponse.success("排班调换申请已删除");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/debug/schedule/{id}")
    @Operation(summary = "调试排班信息", description = "查看指定排班的详细信息（包括提前签到设置）")
    public ApiResponse<Map<String, Object>> debugSchedule(@PathVariable Long id) {
        try {
            DutySchedule schedule = dutyService.findDutyScheduleById(id);
            Map<String, Object> debugInfo = new HashMap<>();
            
            debugInfo.put("scheduleId", schedule.getId());
            debugInfo.put("userId", schedule.getUser().getId());
            debugInfo.put("userName", schedule.getUser().getRealName());
            debugInfo.put("dayOfWeek", schedule.getDayOfWeek());
            debugInfo.put("startTime", schedule.getStartTime());
            debugInfo.put("endTime", schedule.getEndTime());
            debugInfo.put("earlyCheckinMinutes", schedule.getEarlyCheckinMinutes());
            debugInfo.put("lateCheckinMinutes", schedule.getLateCheckinMinutes());
            debugInfo.put("active", schedule.getActive());
            debugInfo.put("notes", schedule.getNotes());
            
            // 计算允许签到的时间范围（以开始时间为中心，向前/向后扩展）
            LocalTime allowedStartTime = schedule.getStartTime()
                    .minusMinutes(schedule.getEarlyCheckinMinutes());
            LocalTime allowedEndTime = schedule.getStartTime()
                    .plusMinutes(schedule.getLateCheckinMinutes());
            
            debugInfo.put("allowedCheckinStart", allowedStartTime);
            debugInfo.put("allowedCheckinEnd", allowedEndTime);
            
            // 当前时间信息
            LocalDateTime now = LocalDateTime.now();
            LocalTime currentTime = now.toLocalTime();
            int currentDayOfWeek = now.getDayOfWeek().getValue();
            
            debugInfo.put("currentTime", currentTime);
            debugInfo.put("currentDayOfWeek", currentDayOfWeek);
            debugInfo.put("isToday", currentDayOfWeek == schedule.getDayOfWeek());
            
            // 检查是否可以签到
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            debugInfo.put("canCheckin", dutyService.canDutyCheckin(userId));
            
            return ApiResponse.success("排班调试信息", debugInfo);
        } catch (Exception e) {
            return ApiResponse.error("获取排班信息失败: " + e.getMessage());
        }
    }
}
