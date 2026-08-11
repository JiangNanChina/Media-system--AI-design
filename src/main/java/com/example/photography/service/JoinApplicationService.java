package com.example.photography.service;

import com.example.photography.dto.request.InterviewGroupRequest;
import com.example.photography.dto.request.JoinApplicationReviewRequest;
import com.example.photography.dto.request.SiteConfigRequest;
import com.example.photography.dto.response.JoinApplicationResponse;
import com.example.photography.model.entity.JoinApplication;
import com.example.photography.model.entity.SiteConfig;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.Gender;
import com.example.photography.model.enums.JoinApplicationStatus;
import com.example.photography.repository.JoinApplicationRepository;
import com.example.photography.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JoinApplicationService {
    private static final long MAX_WORK_SIZE = 300L * 1024 * 1024;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "webm");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
            "video/mp4", "video/quicktime", "video/webm", "application/octet-stream"
    );

    private final JoinApplicationRepository joinApplicationRepository;
    private final UserRepository userRepository;
    private final SiteConfigService siteConfigService;
    private final EmailNotificationService emailNotificationService;

    @Value("${file.private-upload.path:private-uploads}")
    private String privateUploadPath;

    private Path workDirectory;

    @PostConstruct
    void initializeDirectory() throws IOException {
        Path base = Paths.get(privateUploadPath);
        if (!base.isAbsolute()) {
            base = Paths.get(System.getProperty("user.dir")).resolve(base);
        }
        workDirectory = base.normalize().toAbsolutePath().resolve("join-applications");
        Files.createDirectories(workDirectory);
    }

    public JoinApplicationResponse submit(String realName, String qqEmail, String phone, Gender gender,
                                          String college, String major, Integer enrollmentYear,
                                          String selfIntroduction, MultipartFile work) {
        validateApplicant(realName, qqEmail, phone, gender, college, major, enrollmentYear, selfIntroduction);
        WorkMetadata metadata = validateWork(work);

        String storedFilename = null;
        if (metadata != null) {
            storedFilename = UUID.randomUUID() + "." + metadata.extension();
            Path target = workDirectory.resolve(storedFilename).normalize();
            if (!target.startsWith(workDirectory)) {
                throw new IllegalArgumentException("非法文件路径");
            }
            try (InputStream input = work.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("作品保存失败，请稍后重试", e);
            }
        }

        try {
            JoinApplication entity = new JoinApplication();
            entity.setApplicationNumber(nextApplicationNumber());
            entity.setRealName(realName.trim());
            entity.setQqEmail(qqEmail.trim().toLowerCase(Locale.ROOT));
            entity.setPhone(phone.trim());
            entity.setGender(gender);
            entity.setCollege(college.trim());
            entity.setMajor(major.trim());
            entity.setEnrollmentYear(enrollmentYear);
            entity.setSelfIntroduction(trimTo(selfIntroduction, 5000));

            if (metadata != null) {
                entity.setWorkOriginalFilename(StringUtils.cleanPath(Objects.requireNonNullElse(work.getOriginalFilename(), "work." + metadata.extension())));
                entity.setWorkStoredFilename(storedFilename);
                entity.setWorkMimeType(metadata.mimeType());
                entity.setWorkFileSize(work.getSize());
            }

            return JoinApplicationResponse.from(joinApplicationRepository.save(entity));
        } catch (RuntimeException e) {
            if (storedFilename != null) {
                try {
                    Files.deleteIfExists(workDirectory.resolve(storedFilename));
                } catch (IOException ignored) {
                    log.warn("清理失败的入部作品文件失败: {}", storedFilename);
                }
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<JoinApplicationResponse> findAll(JoinApplicationStatus status, Pageable pageable) {
        Page<JoinApplication> page = status == null
                ? joinApplicationRepository.findByDeletedFalse(pageable)
                : joinApplicationRepository.findByStatusAndDeletedFalse(status, pageable);
        return page.map(JoinApplicationResponse::from);
    }

    @Transactional(readOnly = true)
    public JoinApplicationResponse findOne(Long id) {
        return JoinApplicationResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public JoinApplication findEntity(Long id) {
        return joinApplicationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("入部申请不存在"));
    }

    public JoinApplicationResponse review(Long id, Long reviewerId, JoinApplicationReviewRequest request) {
        JoinApplication entity = findEntity(id);
        if (entity.getStatus() != JoinApplicationStatus.PENDING) {
            throw new IllegalArgumentException("该申请已审核，不能重复处理");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("审核人不存在"));
        if (reviewer.getRole() == null || !reviewer.getRole().canManageBusiness()) {
            throw new AccessDeniedException("无入部申请审核权限");
        }

        entity.setReviewer(reviewer);
        entity.setReviewFeedback(trimTo(request.getFeedback(), 1000));
        entity.setReviewedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(request.getApproved())) {
            String interviewGroup = getInterviewQqGroup();
            if (!StringUtils.hasText(interviewGroup)) {
                throw new IllegalArgumentException("请先配置面试QQ群号");
            }
            entity.setStatus(JoinApplicationStatus.INTERVIEW);
            entity.setInterviewQqGroup(interviewGroup);
            entity.setNotificationSent(false);
            entity.setNotificationError(null);
            JoinApplication saved = joinApplicationRepository.save(entity);
            EmailNotificationService.NotificationResult result = emailNotificationService.notifyJoinApplicationInterview(saved);
            saved.setNotificationSent(result.success());
            saved.setNotifiedAt(result.success() ? LocalDateTime.now() : null);
            saved.setNotificationError(result.success() ? null : trimTo(result.errorMessage(), 1000));
            return JoinApplicationResponse.from(joinApplicationRepository.save(saved));
        }

        entity.setStatus(JoinApplicationStatus.REJECTED);
        entity.setInterviewQqGroup(null);
        entity.setNotificationSent(false);
        entity.setNotifiedAt(null);
        entity.setNotificationError(null);
        return JoinApplicationResponse.from(joinApplicationRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Resource loadWork(Long id) {
        JoinApplication entity = findEntity(id);
        if (!StringUtils.hasText(entity.getWorkStoredFilename())) {
            throw new IllegalArgumentException("该申请未上传作品");
        }
        try {
            Path path = workDirectory.resolve(entity.getWorkStoredFilename()).normalize();
            if (!path.startsWith(workDirectory)) {
                throw new IllegalArgumentException("非法文件路径");
            }
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("作品文件不存在");
            }
            return resource;
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("作品文件路径无效", e);
        }
    }

    @Transactional(readOnly = true)
    public String getInterviewQqGroup() {
        return siteConfigService.getConfigValue(SiteConfig.Keys.JOIN_INTERVIEW_QQ_GROUP, "");
    }

    public String saveInterviewQqGroup(InterviewGroupRequest request) {
        SiteConfigRequest configRequest = new SiteConfigRequest();
        configRequest.setConfigKey(SiteConfig.Keys.JOIN_INTERVIEW_QQ_GROUP);
        configRequest.setConfigValue(request.getQqGroupNumber().trim());
        configRequest.setDescription("入部面试QQ群号");
        configRequest.setConfigType(SiteConfig.ConfigType.TEXT);
        configRequest.setEnabled(true);
        configRequest.setSortOrder(1100);
        siteConfigService.saveOrUpdateConfig(configRequest);
        return request.getQqGroupNumber().trim();
    }

    private void validateApplicant(String realName, String qqEmail, String phone, Gender gender,
                                   String college, String major, Integer enrollmentYear, String selfIntroduction) {
        if (!StringUtils.hasText(realName) || realName.trim().length() > 80) {
            throw new IllegalArgumentException("姓名不能为空且不能超过80字");
        }
        if (!StringUtils.hasText(qqEmail) || !qqEmail.trim().matches("^[^@\\s]+@qq\\.com$")) {
            throw new IllegalArgumentException("请输入有效QQ邮箱");
        }
        if (!StringUtils.hasText(phone) || !phone.trim().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("请输入有效手机号");
        }
        if (gender == null) {
            throw new IllegalArgumentException("请选择性别");
        }
        if (!StringUtils.hasText(college) || college.trim().length() > 160) {
            throw new IllegalArgumentException("学院不能为空且不能超过160字");
        }
        if (!StringUtils.hasText(major) || major.trim().length() > 160) {
            throw new IllegalArgumentException("专业不能为空且不能超过160字");
        }
        int currentYear = Year.now().getValue();
        if (enrollmentYear == null || enrollmentYear < 2000 || enrollmentYear > currentYear + 1) {
            throw new IllegalArgumentException("请输入有效入学年份");
        }
        if (!StringUtils.hasText(selfIntroduction) || selfIntroduction.trim().length() > 5000) {
            throw new IllegalArgumentException("自我介绍不能为空且不能超过5000字");
        }
    }

    private WorkMetadata validateWork(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_WORK_SIZE) {
            throw new IllegalArgumentException("作品文件不能超过300MB");
        }

        String original = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        if (!StringUtils.hasText(original) || original.contains("..")) {
            throw new IllegalArgumentException("作品文件名不合法");
        }

        String extension = Optional.ofNullable(StringUtils.getFilenameExtension(original))
                .orElse("")
                .toLowerCase(Locale.ROOT);
        boolean image = IMAGE_EXTENSIONS.contains(extension);
        boolean video = VIDEO_EXTENSIONS.contains(extension);
        if (!image && !video) {
            throw new IllegalArgumentException("作品仅支持JPG、PNG、GIF、WebP、BMP、MP4、MOV和WebM格式");
        }

        String declared = Objects.requireNonNullElse(file.getContentType(), "application/octet-stream").toLowerCase(Locale.ROOT);
        if (!ALLOWED_MIME_TYPES.contains(declared)) {
            throw new IllegalArgumentException("作品文件MIME类型不受支持");
        }

        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(16);
            if (image && !isValidImageHeader(extension, header)) {
                throw new IllegalArgumentException("图片文件内容与格式不匹配");
            }
            if (video && !isValidVideoHeader(extension, header)) {
                throw new IllegalArgumentException("视频文件内容与格式不匹配");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取作品文件", e);
        }

        return new WorkMetadata(extension, normalizeMimeType(extension, declared));
    }

    private boolean isValidImageHeader(String extension, byte[] header) {
        if (extension.equals("jpg") || extension.equals("jpeg")) {
            return header.length >= 3 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
        }
        if (extension.equals("png")) {
            return header.length >= 8 && (header[0] & 0xff) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
        }
        if (extension.equals("gif")) {
            return header.length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
        }
        if (extension.equals("bmp")) {
            return header.length >= 2 && header[0] == 'B' && header[1] == 'M';
        }
        if (extension.equals("webp")) {
            return header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
        }
        return false;
    }

    private boolean isValidVideoHeader(String extension, byte[] header) {
        boolean webm = header.length >= 4 && (header[0] & 0xff) == 0x1a && (header[1] & 0xff) == 0x45
                && (header[2] & 0xff) == 0xdf && (header[3] & 0xff) == 0xa3;
        boolean iso = header.length >= 12 && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';
        return extension.equals("webm") ? webm : iso;
    }

    private String normalizeMimeType(String extension, String declared) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "webm" -> "video/webm";
            case "mov" -> "video/quicktime";
            case "mp4" -> "video/mp4";
            default -> declared;
        };
    }

    private String nextApplicationNumber() {
        String prefix = "JOIN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String value;
        do {
            value = prefix + String.format("%04d", new java.security.SecureRandom().nextInt(10000));
        } while (joinApplicationRepository.existsByApplicationNumber(value));
        return value;
    }

    private String trimTo(String value, int max) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private record WorkMetadata(String extension, String mimeType) {
    }
}
