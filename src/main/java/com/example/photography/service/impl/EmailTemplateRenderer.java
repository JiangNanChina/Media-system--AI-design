package com.example.photography.service.impl;

import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一邮件模板。邮件客户端对 CSS 支持有限，所以主要使用内联样式和表格布局。
 */
final class EmailTemplateRenderer {
    private static final String BRAND_NAME = "融媒体管理系统";
    private static final String TEXT_PRIMARY = "#0f172a";
    private static final String TEXT_SECONDARY = "#475569";
    private static final String ACCENT = "#2f7df6";

    private EmailTemplateRenderer() {
    }

    static String renderTestMail() {
        return renderShell(
                "QQ邮箱配置测试",
                "配置验证",
                "SMTP 连接正常，系统已经可以通过 QQ 邮箱发送验证码与提醒邮件。",
                List.of(
                        detail("状态", "测试通过"),
                        detail("服务", "QQ邮箱 SMTP"),
                        detail("用途", "注册验证码、执勤提醒、晚自习打卡提醒、请假审批与设备逾期提醒")
                ),
                "后续系统提醒将以同样的邮件样式发送给相关成员。"
        );
    }

    static String renderRegisterCode(String code, int expireMinutes) {
        String escapedCode = escape(code);
        return renderShell(
                "注册邮箱验证码",
                "安全验证",
                "您正在注册融媒体管理系统账号，请在页面中填写以下验证码完成邮箱验证。",
                List.of(
                        htmlDetail("验证码", "<span style=\"font-size:32px;line-height:1;font-weight:800;letter-spacing:8px;color:" + ACCENT + ";\">" + escapedCode + "</span>"),
                        detail("有效期", expireMinutes + "分钟"),
                        detail("安全提示", "验证码使用后立即失效，请勿转发给他人")
                ),
                "如果这不是您本人操作，可以忽略本邮件。"
        );
    }

    static String renderNotification(String title, String... lines) {
        List<Detail> details = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        for (String line : lines) {
            ParsedLine parsed = parseLine(line);
            if (parsed.hasLabel()) {
                details.add(detail(parsed.label(), parsed.value()));
            } else if (parsed.hasText()) {
                notes.add(parsed.value());
            }
        }

        String intro = notes.isEmpty()
                ? "您有一条新的系统提醒，请及时查看并处理。"
                : notes.remove(notes.size() - 1);
        String note = notes.isEmpty() ? "" : String.join("，", notes);

        return renderShell(title, "系统提醒", intro, details, note);
    }

    private static String renderShell(String title, String eyebrow, String intro, List<Detail> details, String note) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>")
                .append("<html><body style=\"margin:0;padding:0;background:#eef6ff;\">")
                .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"width:100%;border-collapse:collapse;background:linear-gradient(135deg,#eef6ff 0%,#ffffff 46%,#effdf7 100%);padding:32px 0;\">")
                .append("<tr><td align=\"center\" style=\"padding:32px 16px;\">")
                .append("<table role=\"presentation\" width=\"640\" cellspacing=\"0\" cellpadding=\"0\" style=\"width:100%;max-width:640px;border-collapse:separate;border-spacing:0;\">")
                .append("<tr><td style=\"padding:0 0 14px 0;\">")
                .append("<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;\"><tr>")
                .append("<td style=\"width:40px;height:40px;border-radius:14px;background:linear-gradient(135deg,#2f7df6,#7dd3fc);color:#ffffff;font-size:20px;font-weight:800;text-align:center;vertical-align:middle;box-shadow:0 12px 26px rgba(47,125,246,0.22);\">融</td>")
                .append("<td style=\"padding-left:12px;font-family:Arial,'Microsoft YaHei',sans-serif;color:")
                .append(TEXT_PRIMARY)
                .append(";font-size:15px;font-weight:700;\">")
                .append(BRAND_NAME)
                .append("<div style=\"margin-top:2px;color:#64748b;font-size:12px;font-weight:400;\">智能提醒邮件</div>")
                .append("</td></tr></table>")
                .append("</td></tr>")
                .append("<tr><td style=\"background:rgba(255,255,255,0.86);border:1px solid rgba(203,213,225,0.86);border-radius:24px;padding:32px;box-shadow:0 22px 60px rgba(15,23,42,0.10);backdrop-filter:blur(18px);-webkit-backdrop-filter:blur(18px);font-family:Arial,'Microsoft YaHei',sans-serif;\">")
                .append("<div style=\"display:inline-block;padding:7px 12px;border-radius:999px;background:rgba(47,125,246,0.10);border:1px solid rgba(47,125,246,0.16);color:#1d4ed8;font-size:12px;font-weight:700;letter-spacing:0.4px;\">")
                .append(escape(eyebrow))
                .append("</div>")
                .append("<h1 style=\"margin:18px 0 10px 0;color:")
                .append(TEXT_PRIMARY)
                .append(";font-size:28px;line-height:1.28;font-weight:800;letter-spacing:0;\">")
                .append(escape(title))
                .append("</h1>")
                .append("<p style=\"margin:0 0 24px 0;color:")
                .append(TEXT_SECONDARY)
                .append(";font-size:15px;line-height:1.9;\">")
                .append(escape(intro))
                .append("</p>");

        if (!details.isEmpty()) {
            html.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:separate;border-spacing:0 10px;\">");
            for (Detail detail : details) {
                html.append("<tr>")
                        .append("<td style=\"width:116px;padding:14px 16px;border-radius:16px 0 0 16px;background:rgba(248,250,252,0.92);border:1px solid #e2e8f0;border-right:0;color:#64748b;font-size:13px;font-weight:700;vertical-align:top;\">")
                        .append(detail.label())
                        .append("</td>")
                        .append("<td style=\"padding:14px 16px;border-radius:0 16px 16px 0;background:rgba(255,255,255,0.78);border:1px solid #e2e8f0;color:")
                        .append(TEXT_PRIMARY)
                        .append(";font-size:14px;line-height:1.75;font-weight:600;vertical-align:top;\">")
                        .append(detail.htmlValue())
                        .append("</td>")
                        .append("</tr>");
            }
            html.append("</table>");
        }

        if (hasText(note)) {
            html.append("<div style=\"margin-top:18px;padding:14px 16px;border-radius:18px;background:rgba(47,125,246,0.08);border:1px solid rgba(47,125,246,0.14);color:#1e3a8a;font-size:14px;line-height:1.8;\">")
                    .append(escape(note))
                    .append("</div>");
        }

        html.append("</td></tr>")
                .append("<tr><td style=\"padding:18px 8px 0 8px;text-align:center;font-family:Arial,'Microsoft YaHei',sans-serif;color:#94a3b8;font-size:12px;line-height:1.7;\">")
                .append("本邮件由系统自动发送，请勿直接回复。")
                .append("</td></tr>")
                .append("</table>")
                .append("</td></tr>")
                .append("</table>")
                .append("</body></html>");
        return html.toString();
    }

    private static Detail detail(String label, String value) {
        return new Detail(escape(label), escape(value));
    }

    private static Detail htmlDetail(String label, String htmlValue) {
        return new Detail(escape(label), htmlValue == null ? "" : htmlValue);
    }

    private static ParsedLine parseLine(String line) {
        if (!hasText(line)) {
            return new ParsedLine("", "");
        }

        String trimmed = line.trim();
        int separator = trimmed.indexOf('：');
        if (separator < 0) {
            separator = trimmed.indexOf(':');
        }

        if (separator <= 0) {
            return new ParsedLine("", trimmed);
        }

        return new ParsedLine(trimmed.substring(0, separator), trimmed.substring(separator + 1).trim());
    }

    private static String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private record Detail(String label, String htmlValue) {
    }

    private record ParsedLine(String label, String value) {
        boolean hasLabel() {
            return EmailTemplateRenderer.hasText(label);
        }

        boolean hasText() {
            return EmailTemplateRenderer.hasText(value);
        }
    }
}
