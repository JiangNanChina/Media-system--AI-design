package com.example.photography.controller;

import com.example.photography.dto.request.InterviewGroupRequest;
import com.example.photography.dto.request.JoinApplicationReviewRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.JoinApplicationResponse;
import com.example.photography.model.entity.JoinApplication;
import com.example.photography.model.enums.Gender;
import com.example.photography.model.enums.JoinApplicationStatus;
import com.example.photography.service.JoinApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/join-applications")
@RequiredArgsConstructor
@Tag(name = "入部申请", description = "游客入部申请提交与后台审核")
public class JoinApplicationController {
    private final JoinApplicationService joinApplicationService;

    @PostMapping(value = "/public", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "提交入部申请", description = "游客填写个人信息并可选上传自我作品")
    public ApiResponse<JoinApplicationResponse> submit(
            @RequestParam String realName,
            @RequestParam String qqEmail,
            @RequestParam String phone,
            @RequestParam Gender gender,
            @RequestParam String college,
            @RequestParam String major,
            @RequestParam Integer enrollmentYear,
            @RequestParam String selfIntroduction,
            @RequestPart(value = "work", required = false) MultipartFile work) {
        JoinApplicationResponse response = joinApplicationService.submit(
                realName, qqEmail, phone, gender, college, major, enrollmentYear, selfIntroduction, work);
        return ApiResponse.success("申请已提交", response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    @Operation(summary = "分页查看入部申请", description = "部长及以上身份可查看入部申请")
    public ApiResponse<Page<JoinApplicationResponse>> list(@RequestParam(required = false) JoinApplicationStatus status,
                                                           Pageable pageable) {
        return ApiResponse.success(joinApplicationService.findAll(status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    @Operation(summary = "查看入部申请详情", description = "查看单个入部申请详情")
    public ApiResponse<JoinApplicationResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(joinApplicationService.findOne(id));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    @Operation(summary = "审核入部申请", description = "同意后申请进入面试阶段并发送面试QQ群邮件")
    public ApiResponse<JoinApplicationResponse> review(@PathVariable Long id,
                                                       @Valid @RequestBody JoinApplicationReviewRequest request,
                                                       Authentication authentication) {
        Long reviewerId = (Long) authentication.getDetails();
        JoinApplicationResponse response = joinApplicationService.review(id, reviewerId, request);
        String message = Boolean.TRUE.equals(request.getApproved())
                ? (Boolean.TRUE.equals(response.getNotificationSent()) ? "已进入面试并发送邮件通知" : "已进入面试，邮件通知发送失败")
                : "申请已驳回";
        return ApiResponse.success(message, response);
    }

    @GetMapping("/{id}/work")
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    @Operation(summary = "下载入部申请作品", description = "下载游客上传的自我作品")
    public ResponseEntity<Resource> downloadWork(@PathVariable Long id) {
        JoinApplication entity = joinApplicationService.findEntity(id);
        Resource resource = joinApplicationService.loadWork(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(entity.getWorkOriginalFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entity.getWorkMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @GetMapping("/admin/interview-group")
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    @Operation(summary = "获取面试QQ群号", description = "读取入部申请通过后邮件通知使用的面试QQ群号")
    public ApiResponse<Map<String, String>> getInterviewGroup() {
        return ApiResponse.success(Map.of("qqGroupNumber", joinApplicationService.getInterviewQqGroup()));
    }

    @PutMapping("/admin/interview-group")
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    @Operation(summary = "保存面试QQ群号", description = "配置入部申请通过后邮件通知使用的面试QQ群号")
    public ApiResponse<Map<String, String>> saveInterviewGroup(@Valid @RequestBody InterviewGroupRequest request) {
        String qqGroupNumber = joinApplicationService.saveInterviewQqGroup(request);
        return ApiResponse.success("面试QQ群号已保存", Map.of("qqGroupNumber", qqGroupNumber));
    }
}
