package com.example.photography.service;

import com.example.photography.dto.request.SubmissionReviewRequest;
import com.example.photography.dto.response.MediaSubmissionResponse;
import com.example.photography.model.entity.MediaSubmission;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.SubmissionStatus;
import com.example.photography.repository.MediaSubmissionRepository;
import com.example.photography.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaSubmissionService {
    private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("mp4", "mov", "webm");
    private static final Set<String> MIME_TYPES = Set.of("video/mp4", "video/quicktime", "video/webm", "application/octet-stream");

    private final MediaSubmissionRepository repository;
    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;
    private final EmailDeliveryService emailDeliveryService;

    @Value("${file.private-upload.path:private-uploads}")
    private String privateUploadPath;
    private Path submissionDirectory;

    @PostConstruct
    void initializeDirectory() throws IOException {
        Path base = Paths.get(privateUploadPath);
        if (!base.isAbsolute()) {
            base = Paths.get(System.getProperty("user.dir")).resolve(base);
        }
        submissionDirectory = base.normalize().toAbsolutePath().resolve("submissions");
        Files.createDirectories(submissionDirectory);
    }

    public void sendVerificationCode(String email) {
        verificationCodeService.send(email, VerificationCodeService.VIDEO_SUBMISSION, "视频投稿邮箱验证码");
    }

    public MediaSubmissionResponse submit(String title, String description, String submitterName,
                                          String phone, String email, String organization,
                                          String code, MultipartFile file) {
        validateText(title, submitterName, phone, email);
        verificationCodeService.verify(email, VerificationCodeService.VIDEO_SUBMISSION, code);
        VideoMetadata metadata = validateVideo(file);
        String storedFilename = UUID.randomUUID() + "." + metadata.extension();
        Path target = submissionDirectory.resolve(storedFilename).normalize();
        if (!target.startsWith(submissionDirectory)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("视频保存失败，请稍后重试", e);
        }

        try {
            MediaSubmission entity = new MediaSubmission();
            entity.setSubmissionNumber(nextSubmissionNumber());
            entity.setTitle(title.trim());
            entity.setDescription(trimTo(description, 5000));
            entity.setSubmitterName(submitterName.trim());
            entity.setPhone(phone.trim());
            entity.setQqEmail(email.trim().toLowerCase(Locale.ROOT));
            entity.setOrganization(trimTo(organization, 160));
            entity.setOriginalFilename(StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "video." + metadata.extension())));
            entity.setStoredFilename(storedFilename);
            entity.setMimeType(metadata.mimeType());
            entity.setFileSize(file.getSize());
            MediaSubmission saved = repository.save(entity);
            sendSafely(saved.getQqEmail(), "视频投稿已收到",
                    "您的投稿编号为 <strong>" + saved.getSubmissionNumber() + "</strong>，审核结果将通过邮箱通知。");
            return MediaSubmissionResponse.from(saved);
        } catch (RuntimeException e) {
            try { Files.deleteIfExists(target); } catch (IOException ignored) { }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<MediaSubmissionResponse> findAll(SubmissionStatus status, Pageable pageable) {
        Page<MediaSubmission> page = status == null
                ? repository.findByDeletedFalse(pageable)
                : repository.findByStatusAndDeletedFalse(status, pageable);
        return page.map(MediaSubmissionResponse::from);
    }

    @Transactional(readOnly = true)
    public MediaSubmission findEntity(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("投稿不存在"));
    }

    public MediaSubmissionResponse review(Long id, Long reviewerId, SubmissionReviewRequest request) {
        if (request.getStatus() == SubmissionStatus.PENDING) {
            throw new IllegalArgumentException("审核结果只能为通过或驳回");
        }
        MediaSubmission entity = findEntity(id);
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("审核人不存在"));
        if (!reviewer.getRole().canReviewSubmission()) {
            throw new org.springframework.security.access.AccessDeniedException("无投稿审核权限");
        }
        entity.setStatus(request.getStatus());
        entity.setReviewer(reviewer);
        entity.setReviewFeedback(trimTo(request.getFeedback(), 1000));
        entity.setReviewedAt(LocalDateTime.now());
        MediaSubmission saved = repository.save(entity);
        String result = saved.getStatus() == SubmissionStatus.APPROVED ? "已通过" : "未通过";
        sendSafely(saved.getQqEmail(), "视频投稿审核结果",
                "投稿 <strong>" + saved.getSubmissionNumber() + "</strong> " + result + "。<br>反馈："
                        + escapeHtml(Objects.requireNonNullElse(saved.getReviewFeedback(), "无")));
        return MediaSubmissionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Resource loadVideo(Long id) {
        MediaSubmission entity = findEntity(id);
        try {
            Path path = submissionDirectory.resolve(entity.getStoredFilename()).normalize();
            if (!path.startsWith(submissionDirectory)) {
                throw new IllegalArgumentException("非法文件路径");
            }
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("投稿视频文件不存在");
            }
            return resource;
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("投稿视频文件路径无效", e);
        }
    }

    private VideoMetadata validateVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择投稿视频");
        if (file.getSize() > MAX_VIDEO_SIZE) throw new IllegalArgumentException("视频大小不能超过500MB");
        String original = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        if (original.contains("..")) throw new IllegalArgumentException("文件名不合法");
        String extension = Optional.ofNullable(StringUtils.getFilenameExtension(original)).orElse("").toLowerCase(Locale.ROOT);
        if (!EXTENSIONS.contains(extension)) throw new IllegalArgumentException("仅支持MP4、MOV和WebM格式");
        String declared = Objects.requireNonNullElse(file.getContentType(), "application/octet-stream").toLowerCase(Locale.ROOT);
        if (!MIME_TYPES.contains(declared)) throw new IllegalArgumentException("视频MIME类型不受支持");
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(16);
            boolean webm = header.length >= 4 && (header[0] & 0xff) == 0x1a && (header[1] & 0xff) == 0x45
                    && (header[2] & 0xff) == 0xdf && (header[3] & 0xff) == 0xa3;
            boolean iso = header.length >= 12 && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';
            if ((extension.equals("webm") && !webm) || (!extension.equals("webm") && !iso)) {
                throw new IllegalArgumentException("视频文件内容与格式不匹配");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取视频文件", e);
        }
        String mime = extension.equals("webm") ? "video/webm" : extension.equals("mov") ? "video/quicktime" : "video/mp4";
        return new VideoMetadata(extension, mime);
    }

    private void validateText(String title, String name, String phone, String email) {
        if (!StringUtils.hasText(title) || title.trim().length() > 160) throw new IllegalArgumentException("投稿标题不能为空且不能超过160字");
        if (!StringUtils.hasText(name) || name.trim().length() > 80) throw new IllegalArgumentException("投稿人姓名不能为空");
        if (!StringUtils.hasText(phone) || !phone.trim().matches("^1[3-9]\\d{9}$")) throw new IllegalArgumentException("请输入有效手机号");
        if (!StringUtils.hasText(email) || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new IllegalArgumentException("请输入有效QQ邮箱");
    }

    private String nextSubmissionNumber() {
        String prefix = "SUB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String value;
        do { value = prefix + String.format("%04d", new java.security.SecureRandom().nextInt(10000)); }
        while (repository.existsBySubmissionNumber(value));
        return value;
    }

    private String trimTo(String value, int max) {
        if (!StringUtils.hasText(value)) return null;
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private void sendSafely(String to, String subject, String content) {
        try { emailDeliveryService.sendHtmlMail(to, subject, content); }
        catch (RuntimeException e) { log.warn("投稿通知邮件发送失败: {}", to, e); }
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private record VideoMetadata(String extension, String mimeType) { }
}
