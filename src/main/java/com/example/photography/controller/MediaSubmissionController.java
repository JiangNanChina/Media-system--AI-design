package com.example.photography.controller;

import com.example.photography.dto.request.EmailCodeRequest;
import com.example.photography.dto.request.SubmissionReviewRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.MediaSubmissionResponse;
import com.example.photography.model.entity.MediaSubmission;
import com.example.photography.model.enums.SubmissionStatus;
import com.example.photography.service.MediaSubmissionService;
import com.example.photography.service.VerificationCodeService;
import com.example.photography.service.VerificationRequestLimiter;
import jakarta.servlet.http.HttpServletRequest;
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

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class MediaSubmissionController {
    private final MediaSubmissionService service;
    private final VerificationRequestLimiter verificationRequestLimiter;

    @PostMapping("/public/email-code")
    public ApiResponse<Void> sendCode(@Valid @RequestBody EmailCodeRequest request, HttpServletRequest httpRequest) {
        verificationRequestLimiter.check(request.getEmail(), VerificationCodeService.VIDEO_SUBMISSION, clientIp(httpRequest));
        service.sendVerificationCode(request.getEmail());
        return ApiResponse.success("验证码已发送");
    }

    @PostMapping(value = "/public", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaSubmissionResponse> submit(
            @RequestParam String title, @RequestParam(required = false) String description,
            @RequestParam String submitterName, @RequestParam String phone,
            @RequestParam String email, @RequestParam(required = false) String organization,
            @RequestParam String code, @RequestPart("file") MultipartFile file) {
        return ApiResponse.success("投稿已提交", service.submit(title, description, submitterName, phone, email, organization, code, file));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    public ApiResponse<Page<MediaSubmissionResponse>> list(@RequestParam(required = false) SubmissionStatus status,
                                                           Pageable pageable) {
        return ApiResponse.success(service.findAll(status, pageable));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    public ApiResponse<MediaSubmissionResponse> review(@PathVariable Long id,
                                                       @Valid @RequestBody SubmissionReviewRequest request,
                                                       Authentication authentication) {
        Long reviewerId = (Long) authentication.getDetails();
        return ApiResponse.success("审核完成", service.review(id, reviewerId, request));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('MINISTER','DIRECTOR','SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        MediaSubmission entity = service.findEntity(id);
        Resource resource = service.loadVideo(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(entity.getOriginalFilename(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(entity.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(resource);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
