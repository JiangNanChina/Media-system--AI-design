package com.example.photography.service;

import com.example.photography.dto.request.LandingContentItemRequest;
import com.example.photography.dto.request.SiteConfigRequest;
import com.example.photography.dto.response.LandingPublicResponse;
import com.example.photography.model.entity.LandingContentItem;
import com.example.photography.model.entity.SiteConfig;
import com.example.photography.model.enums.LandingSectionType;
import com.example.photography.repository.LandingContentItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.photography.util.FileUploadUtil;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class LandingService {
    public static final List<String> PUBLIC_KEYS = List.of(
            SiteConfig.Keys.SITE_TITLE, SiteConfig.Keys.SITE_SUBTITLE, SiteConfig.Keys.SITE_LOGO,
            SiteConfig.Keys.LOGIN_BACKGROUND, SiteConfig.Keys.LANDING_HERO_TITLE,
            SiteConfig.Keys.LANDING_HERO_SUBTITLE, SiteConfig.Keys.LANDING_HERO_MEDIA,
            SiteConfig.Keys.LANDING_HERO_MEDIA_TYPE, SiteConfig.Keys.LANDING_BRAND_TITLE,
            SiteConfig.Keys.LANDING_NAV_HOME_LABEL, SiteConfig.Keys.LANDING_NAV_FEATURES_LABEL,
            SiteConfig.Keys.LANDING_NAV_SHOWCASE_LABEL, SiteConfig.Keys.LANDING_NAV_SUBMISSION_LABEL,
            SiteConfig.Keys.LANDING_HERO_BADGE, SiteConfig.Keys.LANDING_HERO_PRIMARY_CTA,
            SiteConfig.Keys.LANDING_HERO_SECONDARY_CTA, SiteConfig.Keys.LANDING_FEATURES_EYEBROW,
            SiteConfig.Keys.LANDING_FEATURES_TITLE, SiteConfig.Keys.LANDING_FEATURES_DESCRIPTION,
            SiteConfig.Keys.LANDING_SHOWCASE_EYEBROW, SiteConfig.Keys.LANDING_SHOWCASE_TITLE,
            SiteConfig.Keys.LANDING_SHOWCASE_DESCRIPTION, SiteConfig.Keys.LANDING_SUBMISSION_EYEBROW,
            SiteConfig.Keys.LANDING_SUBMISSION_TITLE,
            SiteConfig.Keys.LANDING_SUBMISSION_DESCRIPTION, SiteConfig.Keys.LANDING_CONTACT,
            SiteConfig.Keys.LANDING_SUBMISSION_PRIMARY_CTA, SiteConfig.Keys.LANDING_SUBMISSION_SECONDARY_CTA,
            SiteConfig.Keys.LANDING_SUBMISSION_VISUAL_BADGE, SiteConfig.Keys.LANDING_SUBMISSION_STEP_ONE,
            SiteConfig.Keys.LANDING_SUBMISSION_STEP_TWO, SiteConfig.Keys.LANDING_SUBMISSION_STEP_THREE,
            SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_ONE_TITLE, SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_ONE_SUMMARY,
            SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_TWO_TITLE, SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_TWO_SUMMARY,
            SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_THREE_TITLE, SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_THREE_SUMMARY,
            SiteConfig.Keys.LANDING_CONTACT_EMAIL, SiteConfig.Keys.LANDING_CONTACT_PHONE,
            SiteConfig.Keys.LANDING_CONTACT_ADDRESS, SiteConfig.Keys.LANDING_DOUYIN_URL,
            SiteConfig.Keys.LANDING_WECHAT_URL, SiteConfig.Keys.LANDING_WECHAT_QR,
            SiteConfig.Keys.LANDING_WEBSITE_URL,
            SiteConfig.Keys.LANDING_FOOTER_DESCRIPTION, SiteConfig.Keys.LANDING_FOOTER_SOCIAL_TITLE,
            SiteConfig.Keys.LANDING_FOOTER_CONTACT_TITLE, SiteConfig.Keys.LANDING_FOOTER_COPYRIGHT_SUFFIX
    );

    private final LandingContentItemRepository itemRepository;
    private final SiteConfigService siteConfigService;
    private final FileUploadUtil fileUploadUtil;

    @Transactional(readOnly = true)
    public LandingPublicResponse getPublicContent() {
        Map<String, String> settings = new LinkedHashMap<>();
        for (String key : PUBLIC_KEYS) {
            settings.put(key, siteConfigService.getConfigValue(key, defaultValue(key)));
        }
        List<LandingContentItem> items = itemRepository
                .findByPublishedTrueAndDeletedFalseOrderBySectionTypeAscSortOrderAsc();
        return LandingPublicResponse.builder()
                .settings(settings)
                .campusFeatures(items.stream().filter(i -> i.getSectionType() == LandingSectionType.CAMPUS_FEATURE).toList())
                .departmentShowcases(items.stream().filter(i -> i.getSectionType() == LandingSectionType.DEPARTMENT_SHOWCASE).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<LandingContentItem> getAllItems() {
        return itemRepository.findByDeletedFalseOrderBySectionTypeAscSortOrderAsc();
    }

    public LandingContentItem saveItem(Long id, LandingContentItemRequest request) {
        LandingContentItem item = id == null ? new LandingContentItem() : itemRepository.findById(id)
                .filter(value -> !Boolean.TRUE.equals(value.getDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("落地页内容不存在"));
        item.setSectionType(request.getSectionType());
        item.setTitle(request.getTitle().trim());
        item.setSummary(request.getSummary());
        item.setMediaUrl(request.getMediaUrl());
        item.setLinkUrl(request.getLinkUrl());
        item.setPublished(!Boolean.FALSE.equals(request.getPublished()));
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        LandingContentItem item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("落地页内容不存在"));
        item.setDeleted(true);
        itemRepository.save(item);
    }

    public Map<String, String> saveSettings(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            if (!PUBLIC_KEYS.contains(entry.getKey())) {
                continue;
            }
            SiteConfigRequest request = new SiteConfigRequest();
            request.setConfigKey(entry.getKey());
            request.setConfigValue(entry.getValue());
            request.setConfigType(entry.getKey().contains("media") || entry.getKey().contains("logo")
                    || entry.getKey().contains("background") || entry.getKey().contains("qr")
                    ? SiteConfig.ConfigType.IMAGE : SiteConfig.ConfigType.TEXT);
            request.setDescription("落地页配置");
            siteConfigService.saveOrUpdateConfig(request);
        }
        return getPublicContent().getSettings();
    }

    public String uploadMedia(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择媒体文件");
        String extension = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename()))
                .orElse("").toLowerCase(Locale.ROOT);
        String contentType = Objects.requireNonNullElse(file.getContentType(), "").toLowerCase(Locale.ROOT);
        Set<String> allowed = Set.of("jpg", "jpeg", "png", "webp", "gif", "mp4", "mov", "webm");
        if (!allowed.contains(extension) || !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
            throw new IllegalArgumentException("仅支持JPG、PNG、WebP、GIF、MP4、MOV和WebM文件");
        }
        return fileUploadUtil.uploadFile(file, "site", 500L * 1024 * 1024, false);
    }

    public void initializeDefaults() {
        if (itemRepository.findByDeletedFalseOrderBySectionTypeAscSortOrderAsc().isEmpty()) {
            saveDefault(LandingSectionType.CAMPUS_FEATURE, "校园现场", "用影像与文字记录课堂、社团、赛事和校园公共生活。", 1);
            saveDefault(LandingSectionType.CAMPUS_FEATURE, "青年创作", "让学生创意获得专业支持与公开展示的机会。", 2);
            saveDefault(LandingSectionType.CAMPUS_FEATURE, "影像档案", "持续沉淀学校发展、人物故事与集体记忆。", 3);
            saveDefault(LandingSectionType.DEPARTMENT_SHOWCASE, "摄影部", "负责校园活动摄影、专题影像与视觉素材管理。", 1);
            saveDefault(LandingSectionType.DEPARTMENT_SHOWCASE, "审核部", "负责稿件审核、事实核验与内容质量把控。", 2);
            saveDefault(LandingSectionType.DEPARTMENT_SHOWCASE, "运营部", "负责平台运营、内容分发与用户反馈。", 3);
        }
    }

    private void saveDefault(LandingSectionType type, String title, String summary, int order) {
        LandingContentItem item = new LandingContentItem();
        item.setSectionType(type); item.setTitle(title); item.setSummary(summary);
        item.setPublished(true); item.setSortOrder(order);
        itemRepository.save(item);
    }

    private String defaultValue(String key) {
        return switch (key) {
            case SiteConfig.Keys.LANDING_BRAND_TITLE -> "融媒体中心";
            case SiteConfig.Keys.LANDING_NAV_HOME_LABEL -> "首页";
            case SiteConfig.Keys.LANDING_NAV_FEATURES_LABEL -> "校园特色";
            case SiteConfig.Keys.LANDING_NAV_SHOWCASE_LABEL -> "部门风采";
            case SiteConfig.Keys.LANDING_NAV_SUBMISSION_LABEL -> "视频投稿";
            case SiteConfig.Keys.LANDING_HERO_BADGE -> "校园官方新媒体平台";
            case SiteConfig.Keys.LANDING_HERO_TITLE -> "记录校园，让每一种声音被看见";
            case SiteConfig.Keys.LANDING_HERO_SUBTITLE -> "校融媒体中心连接校园现场、青年创作与公共表达";
            case SiteConfig.Keys.LANDING_HERO_PRIMARY_CTA -> "视频投稿";
            case SiteConfig.Keys.LANDING_HERO_SECONDARY_CTA -> "了解我们";
            case SiteConfig.Keys.LANDING_FEATURES_EYEBROW -> "校园特色";
            case SiteConfig.Keys.LANDING_FEATURES_TITLE -> "发现不一样的校园";
            case SiteConfig.Keys.LANDING_FEATURES_DESCRIPTION -> "在这里，每一个角落都有故事，每一刻时光都值得被记录";
            case SiteConfig.Keys.LANDING_SHOWCASE_EYEBROW -> "部门风采";
            case SiteConfig.Keys.LANDING_SHOWCASE_TITLE -> "我们的故事";
            case SiteConfig.Keys.LANDING_SHOWCASE_DESCRIPTION -> "一群热爱影像与创作的年轻人，用镜头讲述校园里的每一个精彩瞬间。";
            case SiteConfig.Keys.LANDING_SUBMISSION_EYEBROW -> "视频投稿";
            case SiteConfig.Keys.LANDING_SUBMISSION_TITLE -> "把你的校园故事交给我们";
            case SiteConfig.Keys.LANDING_SUBMISSION_DESCRIPTION -> "支持校园新闻、人物、活动与创意短视频投稿";
            case SiteConfig.Keys.LANDING_SUBMISSION_PRIMARY_CTA -> "开始投稿";
            case SiteConfig.Keys.LANDING_SUBMISSION_SECONDARY_CTA -> "部门风采";
            case SiteConfig.Keys.LANDING_SUBMISSION_VISUAL_BADGE -> "校园影像库";
            case SiteConfig.Keys.LANDING_SUBMISSION_STEP_ONE -> "上传素材";
            case SiteConfig.Keys.LANDING_SUBMISSION_STEP_TWO -> "填写信息";
            case SiteConfig.Keys.LANDING_SUBMISSION_STEP_THREE -> "等待审核";
            case SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_ONE_TITLE -> "校园新闻";
            case SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_ONE_SUMMARY -> "记录现场与公共议题";
            case SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_TWO_TITLE -> "人物故事";
            case SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_TWO_SUMMARY -> "呈现青春里的闪光时刻";
            case SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_THREE_TITLE -> "活动创意";
            case SiteConfig.Keys.LANDING_SUBMISSION_TOPIC_THREE_SUMMARY -> "捕捉舞台、社团与灵感";
            case SiteConfig.Keys.LANDING_CONTACT_EMAIL -> "media@campus.edu.cn";
            case SiteConfig.Keys.LANDING_CONTACT_PHONE -> "010-12345678";
            case SiteConfig.Keys.LANDING_CONTACT_ADDRESS -> "行政楼 203 室";
            case SiteConfig.Keys.LANDING_WEBSITE_URL -> "https://www.campus.edu.cn";
            case SiteConfig.Keys.LANDING_FOOTER_DESCRIPTION -> "用镜头记录青春，用创意点亮校园。我们是校园里的记录者，用影像传递温度与力量。";
            case SiteConfig.Keys.LANDING_FOOTER_SOCIAL_TITLE -> "关注我们";
            case SiteConfig.Keys.LANDING_FOOTER_CONTACT_TITLE -> "联系我们";
            case SiteConfig.Keys.LANDING_FOOTER_COPYRIGHT_SUFFIX -> "版权所有";
            case SiteConfig.Keys.LANDING_HERO_MEDIA_TYPE -> "image";
            default -> "";
        };
    }
}
