package com.example.photography.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * 服务端签发的动态签到二维码。
 */
@Data
@Builder
public class DynamicQrCodeResponse {

    private Long configurationId;
    private String content;
    private Instant generatedAt;
    private Instant expiresAt;
    private long refreshAfterSeconds;
    private long validitySeconds;
}
