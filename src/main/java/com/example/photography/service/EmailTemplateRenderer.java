package com.example.photography.service;

import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一邮件模板。邮件客户端对 CSS 支持有限，因此使用表格布局和内联样式。
 */
public final class EmailTemplateRenderer {
    private static final String BRAND_NAME = "融媒体管理系统";
    private static final String PAGE = "#edf4f5";
    private static final String FRAME = "#dce9ec";
    private static final String INK = "#173b46";
    private static final String PAPER = "#ffffff";
    private static final String WHITE = "#ffffff";
    private static final String RED = "#c94f3d";
    private static final String YELLOW = "#efc75e";
    private static final String GREY = "#587078";
    private static final String LABEL = "#dcebea";
    private static final String BORDER = "#b9ced1";
    private static final String RAIL = "#55757d";
    private static final String BODY_FONT = "'Microsoft YaHei','PingFang SC',Arial,sans-serif";
    private static final String MONO_FONT = "Consolas,'Courier New',monospace";

    private EmailTemplateRenderer() {
    }

    public static String renderTestMail() {
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

    public static String renderRegisterCode(String code, int expireMinutes) {
        return renderVerificationCode(
                "注册邮箱验证码",
                "您正在注册融媒体管理系统账号，请在页面中填写以下验证码完成邮箱验证。",
                code,
                expireMinutes
        );
    }

    public static String renderVerificationCode(String title, String code, int expireMinutes) {
        return renderVerificationCode(
                title,
                "您正在进行邮箱身份验证，请在页面中填写以下验证码完成操作。",
                code,
                expireMinutes
        );
    }

    public static String renderNotification(String title, String... lines) {
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

    private static String renderVerificationCode(String title, String intro, String code, int expireMinutes) {
        String codeHtml = "<span style=\"display:inline-block;padding:7px 12px;background:" + YELLOW
                + ";color:" + INK + ";font-family:" + MONO_FONT
                + ";font-size:30px;line-height:1;font-weight:800;letter-spacing:7px;\">"
                + escape(code) + "</span>";
        return renderShell(
                title,
                "安全验证",
                intro,
                List.of(
                        htmlDetail("验证码", codeHtml),
                        detail("有效期", expireMinutes + "分钟"),
                        detail("安全提示", "验证码使用后立即失效，请勿转发给他人")
                ),
                "如果这不是您本人操作，可以忽略本邮件。"
        );
    }

    private static String renderShell(String title, String eyebrow, String intro, List<Detail> details, String note) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>")
                .append("<html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
                .append("<style>@media only screen and (max-width:680px){.mail-pad{padding:20px 14px!important}.mail-card{padding:24px 18px!important}.mail-title{font-size:25px!important}.mail-label{width:82px!important;padding:12px 10px!important}.mail-value{padding:12px!important}.mail-meta-right{display:none!important}}</style>")
                .append("</head><body style=\"margin:0;padding:0;background:").append(PAGE)
                .append(";-webkit-text-size-adjust:100%;\">")
                .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" bgcolor=\"")
                .append(PAGE).append("\" style=\"width:100%;border-collapse:collapse;background:").append(PAGE).append(";\">")
                .append("<tr><td class=\"mail-pad\" align=\"center\" style=\"padding:36px 16px;\">")
                .append("<table role=\"presentation\" width=\"640\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"width:100%;max-width:640px;border-collapse:collapse;\">")
                .append("<tr><td style=\"padding:0 0 18px 0;\">")
                .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"width:100%;border-collapse:collapse;\"><tr>")
                .append("<td style=\"width:48px;vertical-align:middle;\"><div style=\"width:44px;height:44px;border-radius:50%;background:").append(INK)
                .append(";color:").append(WHITE).append(";font-family:").append(BODY_FONT)
                .append(";font-size:20px;line-height:44px;font-weight:800;text-align:center;\">融</div></td>")
                .append("<td style=\"padding-left:12px;vertical-align:middle;font-family:").append(BODY_FONT).append(";\">")
                .append("<div style=\"color:").append(INK).append(";font-size:16px;line-height:1.25;font-weight:800;\">")
                .append(BRAND_NAME).append("</div>")
                .append("<div style=\"margin-top:4px;color:").append(RED).append(";font-family:").append(MONO_FONT)
                .append(";font-size:10px;line-height:1.2;font-weight:700;\">MEDIA ARCHIVE / AUTO NOTICE</div></td>")
                .append("<td class=\"mail-meta-right\" align=\"right\" style=\"color:#718990;font-family:").append(MONO_FONT)
                .append(";font-size:9px;vertical-align:bottom;\">CHANNEL / MEMBER SYSTEM</td>")
                .append("</tr></table></td></tr>")
                .append("<tr><td style=\"padding:0;border-top:1px solid ").append(BORDER).append(";border-right:1px solid ").append(BORDER)
                .append(";border-left:1px solid ").append(BORDER).append(";background:").append(RAIL).append(";\">")
                .append(renderPerforations())
                .append("</td></tr>")
                .append("<tr><td class=\"mail-card\" bgcolor=\"").append(PAPER)
                .append("\" style=\"padding:32px;background:").append(PAPER).append(";border-right:1px solid ").append(BORDER)
                .append(";border-left:1px solid ").append(BORDER).append(";font-family:").append(BODY_FONT).append(";\">")
                .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"width:100%;border-collapse:collapse;\">")
                .append("<tr><td style=\"padding-bottom:20px;border-bottom:2px solid ").append(INK).append(";\">")
                .append("<span style=\"display:inline-block;padding:6px 9px;background:").append(RED).append(";color:").append(WHITE)
                .append(";font-family:").append(MONO_FONT).append(";font-size:10px;line-height:1;font-weight:700;\">")
                .append(escape(eyebrow)).append(" / MESSAGE</span>")
                .append("<h1 class=\"mail-title\" style=\"margin:17px 0 10px 0;color:").append(INK)
                .append(";font-family:").append(BODY_FONT).append(";font-size:30px;line-height:1.25;font-weight:900;\">")
                .append(escape(title)).append("</h1>")
                .append("<p style=\"margin:0;color:").append(GREY).append(";font-size:15px;line-height:1.8;\">")
                .append(escape(intro)).append("</p></td></tr>");

        if (!details.isEmpty()) {
            html.append("<tr><td style=\"padding-top:20px;\"><table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"width:100%;border-collapse:collapse;\">");
            for (Detail detail : details) {
                html.append("<tr>")
                        .append("<td class=\"mail-label\" width=\"108\" bgcolor=\"").append(LABEL)
                        .append("\" style=\"width:108px;padding:14px 15px;background:").append(LABEL).append(";border-bottom:1px solid ").append(BORDER).append(";color:").append(INK)
                        .append(";font-family:").append(MONO_FONT).append(";font-size:11px;line-height:1.5;font-weight:700;vertical-align:top;\">")
                        .append(detail.label()).append("</td>")
                        .append("<td class=\"mail-value\" bgcolor=\"").append(WHITE)
                        .append("\" style=\"padding:14px 16px;background:").append(WHITE).append(";border-right:1px solid ").append(BORDER)
                        .append(";border-bottom:1px solid ").append(BORDER).append(";color:").append(INK)
                        .append(";font-size:14px;line-height:1.75;font-weight:600;vertical-align:top;word-break:break-word;\">")
                        .append(detail.htmlValue()).append("</td></tr>");
            }
            html.append("</table></td></tr>");
        }

        if (hasText(note)) {
            html.append("<tr><td style=\"padding-top:20px;\"><table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" bgcolor=\"")
                    .append(FRAME).append("\" style=\"width:100%;border-collapse:collapse;background:").append(FRAME).append(";\"><tr>")
                    .append("<td width=\"5\" bgcolor=\"").append(RED).append("\" style=\"width:5px;background:").append(RED).append(";font-size:0;line-height:0;\">&nbsp;</td>")
                    .append("<td style=\"padding:14px 16px;color:").append(INK).append(";font-size:13px;line-height:1.75;\">")
                    .append(escape(note)).append("</td></tr></table></td></tr>");
        }

        html.append("</table></td></tr>")
                .append("<tr><td style=\"padding:0;border-right:1px solid ").append(BORDER).append(";border-bottom:1px solid ").append(BORDER)
                .append(";border-left:1px solid ").append(BORDER).append(";background:").append(RAIL).append(";\">")
                .append(renderPerforations()).append("</td></tr>")
                .append("<tr><td style=\"padding:16px 4px 0 4px;\">")
                .append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"width:100%;border-collapse:collapse;\"><tr>")
                .append("<td style=\"color:").append(GREY).append(";font-family:").append(MONO_FONT).append(";font-size:9px;line-height:1.6;\">本邮件由系统自动发送，请勿直接回复。</td>")
                .append("<td align=\"right\" style=\"color:").append(INK).append(";font-family:").append(MONO_FONT).append(";font-size:9px;white-space:nowrap;\"><span style=\"color:").append(RED).append(";\">●</span>&nbsp; SIGNAL LOCKED</td>")
                .append("</tr></table></td></tr>")
                .append("</table></td></tr></table></body></html>");
        return html.toString();
    }

    private static String renderPerforations() {
        StringBuilder holes = new StringBuilder("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"width:100%;table-layout:fixed;border-collapse:separate;border-spacing:7px 6px;\"><tr>");
        for (int index = 0; index < 16; index++) {
            holes.append("<td height=\"8\" bgcolor=\"").append(PAGE)
                    .append("\" style=\"height:8px;background:").append(PAGE).append(";font-size:0;line-height:0;\">&nbsp;</td>");
        }
        return holes.append("</tr></table>").toString();
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
