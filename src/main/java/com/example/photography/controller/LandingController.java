package com.example.photography.controller;

import com.example.photography.dto.request.LandingContentItemRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.LandingPublicResponse;
import com.example.photography.model.entity.LandingContentItem;
import com.example.photography.service.LandingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/landing")
@RequiredArgsConstructor
public class LandingController {
    private final LandingService landingService;

    @GetMapping("/public")
    public ApiResponse<LandingPublicResponse> publicContent() {
        return ApiResponse.success(landingService.getPublicContent());
    }

    @GetMapping("/admin/items")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<LandingContentItem>> items() {
        return ApiResponse.success(landingService.getAllItems());
    }

    @PostMapping("/admin/items")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<LandingContentItem> create(@Valid @RequestBody LandingContentItemRequest request) {
        return ApiResponse.success("内容已创建", landingService.saveItem(null, request));
    }

    @PutMapping("/admin/items/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<LandingContentItem> update(@PathVariable Long id, @Valid @RequestBody LandingContentItemRequest request) {
        return ApiResponse.success("内容已更新", landingService.saveItem(id, request));
    }

    @DeleteMapping("/admin/items/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        landingService.deleteItem(id);
        return ApiResponse.success("内容已删除");
    }

    @PutMapping("/admin/settings")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, String>> settings(@RequestBody Map<String, String> settings) {
        return ApiResponse.success("落地页设置已保存", landingService.saveSettings(settings));
    }

    @PostMapping(value = "/admin/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<String> uploadMedia(@RequestPart MultipartFile file) {
        return ApiResponse.success("媒体上传成功", landingService.uploadMedia(file));
    }
}
