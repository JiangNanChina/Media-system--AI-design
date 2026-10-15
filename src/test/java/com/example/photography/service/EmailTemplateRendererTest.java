package com.example.photography.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTemplateRendererTest {

    @Test
    void rendersTestMailWithLandingArchiveStyle() {
        String html = EmailTemplateRenderer.renderTestMail();

        assertTrue(html.startsWith("<!doctype html>"));
        assertTrue(html.contains("background:#edf4f5"));
        assertTrue(html.contains("#ffffff"));
        assertTrue(html.contains("#173b46"));
        assertTrue(html.contains("#dcebea"));
        assertTrue(html.contains("#c94f3d"));
        assertFalse(html.contains("#11100e"));
        assertTrue(html.contains("MEDIA ARCHIVE / AUTO NOTICE"));
        assertTrue(html.contains("QQ邮箱配置测试"));
        assertTrue(html.contains("QQ邮箱 SMTP"));
        assertTrue(html.contains("role=\"presentation\""));
    }

    @Test
    void rendersVerificationCodeAsEscapedFilmMarker() {
        String html = EmailTemplateRenderer.renderVerificationCode("身份验证", "12<34&", 10);

        assertTrue(html.contains("12&lt;34&amp;"));
        assertTrue(html.contains("letter-spacing:7px"));
        assertTrue(html.contains("background:#efc75e"));
        assertTrue(html.contains("10分钟"));
        assertFalse(html.contains("12<34&"));
    }

    @Test
    void escapesNotificationContentAndKeepsDetails() {
        String html = EmailTemplateRenderer.renderNotification(
                "审核结果 <已完成>",
                "投稿编号：SUB-001",
                "审核反馈：<script>alert('x')</script>",
                "请查看本次处理结果。"
        );

        assertTrue(html.contains("审核结果 &lt;已完成&gt;"));
        assertTrue(html.contains("投稿编号"));
        assertTrue(html.contains("SUB-001"));
        assertTrue(html.contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;"));
        assertFalse(html.contains("<script>alert('x')</script>"));
    }
}
