package com.example.photography.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 学院创建/更新请求 DTO。
 */
public class CollegeCreateRequest {

    @NotBlank(message = "学院名称不能为空")
    @Size(max = 160, message = "学院名称不能超过160个字符")
    private String name;

    @Size(max = 500, message = "学院描述不能超过500个字符")
    private String description;

    public CollegeCreateRequest() {}

    public CollegeCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
