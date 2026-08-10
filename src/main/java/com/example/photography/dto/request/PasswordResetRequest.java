package com.example.photography.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String code;
    @NotBlank @Size(min = 8, max = 72)
    private String newPassword;
}
