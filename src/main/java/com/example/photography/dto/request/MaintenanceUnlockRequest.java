package com.example.photography.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceUnlockRequest {
    @NotBlank
    private String password;
}
