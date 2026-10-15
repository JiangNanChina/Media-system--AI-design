package com.example.photography.service;

import com.example.photography.dto.response.DynamicQrCodeResponse;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.repository.CheckinConfigurationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicQrCodeServiceTest {

    private CheckinConfigurationRepository repository;
    private DynamicQrCodeService service;
    private CheckinConfiguration configuration;

    @BeforeEach
    void setUp() {
        repository = mock(CheckinConfigurationRepository.class);
        configuration = new CheckinConfiguration();
        configuration.setId(6L);
        configuration.setName("图书馆晚自习");
        configuration.setLocationName("图书馆一楼");
        configuration.setSessionName("晚自习");
        configuration.setStartTime(LocalTime.of(19, 0));
        configuration.setEndTime(LocalTime.of(21, 30));
        configuration.setIsActive(true);
        configuration.setDeleted(false);
        when(repository.findById(6L)).thenReturn(Optional.of(configuration));
        when(repository.save(any(CheckinConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new DynamicQrCodeService(repository, new ObjectMapper(),
                "test-dynamic-qr-secret", 90, 45);
    }

    @Test
    void issuesSignedTokenAndAcceptsItWithinValidityWindow() throws Exception {
        DynamicQrCodeResponse issued = service.issue(6L);

        JsonNode payload = new ObjectMapper().readTree(issued.getContent());
        assertThat(payload.get("type").asText()).isEqualTo("CHECKIN");
        assertThat(payload.get("version").asInt()).isEqualTo(2);
        assertThat(payload.get("signature").asText()).isNotBlank();
        assertThat(issued.getRefreshAfterSeconds()).isEqualTo(45);
        assertThat(configuration.getQrCode()).isEqualTo(issued.getContent());

        service.validate(issued.getContent(), 6L);
    }

    @Test
    void rejectsTamperedOrMismatchedTokens() {
        DynamicQrCodeResponse issued = service.issue(6L);
        String tampered = issued.getContent().replace("\"signature\":\"", "\"signature\":\"x");

        assertThatThrownBy(() -> service.validate(tampered, 6L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("签名");
        assertThatThrownBy(() -> service.validate(issued.getContent(), 7L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不匹配");
    }

    @Test
    void refusesInactiveConfiguration() {
        configuration.setIsActive(false);

        assertThatThrownBy(() -> service.issue(6L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("未启用");
    }
}
