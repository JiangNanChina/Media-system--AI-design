package com.example.photography.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 测试邮件请求DTO
 */
@Data
public class MailTestRequest {
    @NotBlank(message = "测试收件邮箱不能为空")
    @Email(message = "测试收件邮箱格式不正确")
    private String email;
}
