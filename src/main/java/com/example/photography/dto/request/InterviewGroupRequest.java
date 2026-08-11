package com.example.photography.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterviewGroupRequest {
    @NotBlank(message = "面试QQ群号不能为空")
    @Size(max = 50, message = "面试QQ群号不能超过50个字符")
    @Pattern(regexp = "^\\d{5,12}$", message = "请输入5到12位数字QQ群号")
    private String qqGroupNumber;
}
