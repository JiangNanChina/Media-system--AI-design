package com.example.photography.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 站点配置实体类
 * 用于管理登录界面背景、网站LOGO等全局配置
 */
@Entity
@Table(name = "site_configs")
@Data
@EqualsAndHashCode(callSuper = true)
public class SiteConfig extends BaseEntity {
    
    /**
     * 配置键名（唯一）
     */
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;
    
    /**
     * 配置值
     */
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    /**
     * 配置描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 配置类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false)
    private ConfigType configType;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * 排序权重
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        /**
         * 文本配置
         */
        TEXT("文本"),
        
        /**
         * 图片文件路径
         */
        IMAGE("图片"),
        
        /**
         * 颜色值
         */
        COLOR("颜色"),
        
        /**
         * 数字
         */
        NUMBER("数字"),
        
        /**
         * 布尔值
         */
        BOOLEAN("布尔值"),
        
        /**
         * JSON配置
         */
        JSON("JSON");
        
        private final String description;
        
        ConfigType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 预定义配置键常量
     */
    public static class Keys {
        /**
         * 网站LOGO
         */
        public static final String SITE_LOGO = "site.logo";
        
        /**
         * 登录页面背景图
         */
        public static final String LOGIN_BACKGROUND = "login.background";
        
        /**
         * 网站标题
         */
        public static final String SITE_TITLE = "site.title";
        
        /**
         * 网站副标题
         */
        public static final String SITE_SUBTITLE = "site.subtitle";
        
        /**
         * 主题色
         */
        public static final String PRIMARY_COLOR = "theme.primary_color";
        
        /**
         * 登录页面标题
         */
        public static final String LOGIN_TITLE = "login.title";
        
        /**
         * 登录页面欢迎语
         */
        public static final String LOGIN_WELCOME = "login.welcome";

        /**
         * 是否启用邮件能力
         */
        public static final String MAIL_ENABLED = "mail.enabled";

        /**
         * SMTP服务器地址
         */
        public static final String MAIL_SMTP_HOST = "mail.smtp_host";

        /**
         * SMTP服务器端口
         */
        public static final String MAIL_SMTP_PORT = "mail.smtp_port";

        /**
         * 是否启用SMTP SSL
         */
        public static final String MAIL_SMTP_SSL_ENABLED = "mail.smtp_ssl_enabled";

        /**
         * QQ邮箱账号
         */
        public static final String MAIL_QQ_ACCOUNT = "mail.qq_account";

        /**
         * QQ邮箱SMTP授权码
         */
        public static final String MAIL_QQ_AUTH_CODE = "mail.qq_auth_code";

        /**
         * 发件人显示名称
         */
        public static final String MAIL_SENDER_NAME = "mail.sender_name";

        /**
         * 提醒提前分钟数
         */
        public static final String MAIL_REMINDER_ADVANCE_MINUTES = "mail.reminder_advance_minutes";

        /**
         * 设备逾期提醒间隔小时数
         */
        public static final String MAIL_OVERDUE_REMINDER_INTERVAL_HOURS = "mail.overdue_reminder_interval_hours";

        /**
         * 执勤提醒开关
         */
        public static final String MAIL_DUTY_REMINDER_ENABLED = "mail.duty_reminder_enabled";

        /**
         * 晚自习打卡提醒开关
         */
        public static final String MAIL_CHECKIN_REMINDER_ENABLED = "mail.checkin_reminder_enabled";

        /**
         * 请假审批提醒开关
         */
        public static final String MAIL_LEAVE_APPROVAL_REMINDER_ENABLED = "mail.leave_approval_reminder_enabled";

        /**
         * 设备逾期归还提醒开关
         */
        public static final String MAIL_BORROW_OVERDUE_REMINDER_ENABLED = "mail.borrow_overdue_reminder_enabled";

        public static final String LANDING_HERO_TITLE = "landing.hero.title";
        public static final String LANDING_HERO_SUBTITLE = "landing.hero.subtitle";
        public static final String LANDING_HERO_MEDIA = "landing.hero.media";
        public static final String LANDING_HERO_MEDIA_TYPE = "landing.hero.media_type";
        public static final String LANDING_BRAND_TITLE = "landing.brand.title";
        public static final String LANDING_NAV_HOME_LABEL = "landing.nav.home_label";
        public static final String LANDING_NAV_FEATURES_LABEL = "landing.nav.features_label";
        public static final String LANDING_NAV_SHOWCASE_LABEL = "landing.nav.showcase_label";
        public static final String LANDING_NAV_SUBMISSION_LABEL = "landing.nav.submission_label";
        public static final String LANDING_HERO_BADGE = "landing.hero.badge";
        public static final String LANDING_HERO_PRIMARY_CTA = "landing.hero.primary_cta";
        public static final String LANDING_HERO_SECONDARY_CTA = "landing.hero.secondary_cta";
        public static final String LANDING_FEATURES_EYEBROW = "landing.features.eyebrow";
        public static final String LANDING_FEATURES_TITLE = "landing.features.title";
        public static final String LANDING_FEATURES_DESCRIPTION = "landing.features.description";
        public static final String LANDING_SHOWCASE_EYEBROW = "landing.showcase.eyebrow";
        public static final String LANDING_SHOWCASE_TITLE = "landing.showcase.title";
        public static final String LANDING_SHOWCASE_DESCRIPTION = "landing.showcase.description";
        public static final String LANDING_SUBMISSION_EYEBROW = "landing.submission.eyebrow";
        public static final String LANDING_SUBMISSION_TITLE = "landing.submission.title";
        public static final String LANDING_SUBMISSION_DESCRIPTION = "landing.submission.description";
        public static final String LANDING_SUBMISSION_PRIMARY_CTA = "landing.submission.primary_cta";
        public static final String LANDING_SUBMISSION_SECONDARY_CTA = "landing.submission.secondary_cta";
        public static final String LANDING_SUBMISSION_VISUAL_BADGE = "landing.submission.visual_badge";
        public static final String LANDING_SUBMISSION_STEP_ONE = "landing.submission.step_one";
        public static final String LANDING_SUBMISSION_STEP_TWO = "landing.submission.step_two";
        public static final String LANDING_SUBMISSION_STEP_THREE = "landing.submission.step_three";
        public static final String LANDING_SUBMISSION_TOPIC_ONE_TITLE = "landing.submission.topic_one.title";
        public static final String LANDING_SUBMISSION_TOPIC_ONE_SUMMARY = "landing.submission.topic_one.summary";
        public static final String LANDING_SUBMISSION_TOPIC_TWO_TITLE = "landing.submission.topic_two.title";
        public static final String LANDING_SUBMISSION_TOPIC_TWO_SUMMARY = "landing.submission.topic_two.summary";
        public static final String LANDING_SUBMISSION_TOPIC_THREE_TITLE = "landing.submission.topic_three.title";
        public static final String LANDING_SUBMISSION_TOPIC_THREE_SUMMARY = "landing.submission.topic_three.summary";
        public static final String LANDING_CONTACT = "landing.contact";
        public static final String LANDING_CONTACT_EMAIL = "landing.contact.email";
        public static final String LANDING_CONTACT_PHONE = "landing.contact.phone";
        public static final String LANDING_CONTACT_ADDRESS = "landing.contact.address";
        public static final String LANDING_DOUYIN_URL = "landing.social.douyin_url";
        public static final String LANDING_WECHAT_URL = "landing.social.wechat_url";
        public static final String LANDING_WECHAT_QR = "landing.social.wechat_qr";
        public static final String LANDING_WEBSITE_URL = "landing.social.website_url";
        public static final String LANDING_FOOTER_DESCRIPTION = "landing.footer.description";
        public static final String LANDING_FOOTER_SOCIAL_TITLE = "landing.footer.social_title";
        public static final String LANDING_FOOTER_CONTACT_TITLE = "landing.footer.contact_title";
        public static final String LANDING_FOOTER_COPYRIGHT_SUFFIX = "landing.footer.copyright_suffix";
        public static final String MAINTENANCE_ENABLED = "maintenance.enabled";
        public static final String MAINTENANCE_TITLE = "maintenance.title";
        public static final String MAINTENANCE_MESSAGE = "maintenance.message";
        public static final String MAINTENANCE_PASSWORD_HASH = "maintenance.password_hash";
    }
}
