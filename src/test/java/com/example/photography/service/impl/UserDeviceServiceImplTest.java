package com.example.photography.service.impl;

import com.example.photography.dto.request.DeviceInfoRequest;
import com.example.photography.model.entity.User;
import com.example.photography.model.entity.UserDevice;
import com.example.photography.repository.DeviceAuditLogRepository;
import com.example.photography.repository.UserDeviceRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.UserDeviceService.DeviceValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDeviceServiceImplTest {
    private UserDeviceRepository userDeviceRepository;
    private UserDeviceServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        userDeviceRepository = mock(UserDeviceRepository.class);
        DeviceAuditLogRepository deviceAuditLogRepository = mock(DeviceAuditLogRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        service = new UserDeviceServiceImpl(userDeviceRepository, deviceAuditLogRepository, userRepository);
        user = user(1L, "member");
    }

    @Test
    void firstLoginBindsStableDeviceId() {
        DeviceInfoRequest incoming = mobileInfo("browser_new", "Android 14", "Chrome 130.0.0", "1080x2400");

        when(userDeviceRepository.findActiveDevicesByUserAndTypes(eq(user), any())).thenReturn(Collections.emptyList());
        when(userDeviceRepository.findSuspendedDevicesByUserAndType(user, UserDevice.DeviceType.MOBILE)).thenReturn(Collections.emptyList());
        when(userDeviceRepository.save(any(UserDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeviceValidationResult result = service.validateAndBindDevice(user, incoming, "127.0.0.1", "ua");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDevice().getDeviceFingerprint()).isEqualTo("browser_new");
        assertThat(result.getDevice().getDeviceType()).isEqualTo(UserDevice.DeviceType.MOBILE);
        verify(userDeviceRepository).save(any(UserDevice.class));
    }

    @Test
    void exactStableDeviceIdMatchUpdatesBrowserDisplayInfo() {
        UserDevice bound = device("browser_same", UserDevice.DeviceType.MOBILE, "Android 13", "Chrome 120.0.0", "1080x2400");
        DeviceInfoRequest incoming = mobileInfo("browser_same", "Android 14", "Chrome 130.0.0", "2400x1080");

        when(userDeviceRepository.findActiveDevicesByUserAndTypes(eq(user), any())).thenReturn(List.of(bound));

        DeviceValidationResult result = service.validateAndBindDevice(user, incoming, "127.0.0.1", "ua");

        assertThat(result.isSuccess()).isTrue();
        assertThat(bound.getBrowserInfo()).isEqualTo("Chrome 130.0.0");
        assertThat(bound.getOsInfo()).isEqualTo("Android 14");
        assertThat(bound.getScreenResolution()).isEqualTo("2400x1080");
        verify(userDeviceRepository).save(bound);
    }

    @Test
    void legacyVolatileFingerprintMigratesWhenDeviceProfileMatches() {
        UserDevice bound = device(
                "a6c238b67f0d42652f0db77ce3b6d71b8ca06e27d1ff21fe6c641532dfcbaa00",
                UserDevice.DeviceType.MOBILE,
                "Android 13",
                "Chrome 120.0.0",
                "1080x2400"
        );
        DeviceInfoRequest incoming = mobileInfo("browser_new", "Android 14", "Edge 130.0.0", "2400x1080");

        when(userDeviceRepository.findActiveDevicesByUserAndTypes(eq(user), any())).thenReturn(List.of(bound));

        DeviceValidationResult result = service.validateAndBindDevice(user, incoming, "127.0.0.1", "ua");

        assertThat(result.isSuccess()).isTrue();
        assertThat(bound.getDeviceFingerprint()).isEqualTo("browser_new");
        assertThat(bound.getBrowserInfo()).isEqualTo("Edge 130.0.0");
        verify(userDeviceRepository).save(bound);
    }

    @Test
    void stableDeviceIdMismatchIsBlockedAfterMigration() {
        UserDevice bound = device("browser_old", UserDevice.DeviceType.MOBILE, "Android 14", "Chrome 130.0.0", "1080x2400");
        DeviceInfoRequest incoming = mobileInfo("browser_new", "Android 14", "Chrome 130.0.0", "1080x2400");

        when(userDeviceRepository.findActiveDevicesByUserAndTypes(eq(user), any())).thenReturn(List.of(bound));

        DeviceValidationResult result = service.validateAndBindDevice(user, incoming, "127.0.0.1", "ua");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("其他移动设备");
        assertThat(bound.getDeviceFingerprint()).isEqualTo("browser_old");
    }

    private DeviceInfoRequest mobileInfo(String fingerprint, String osInfo, String browserInfo, String screenResolution) {
        DeviceInfoRequest request = new DeviceInfoRequest();
        request.setDeviceFingerprint(fingerprint);
        request.setDeviceName("Android Device");
        request.setDeviceType("MOBILE");
        request.setOsInfo(osInfo);
        request.setBrowserInfo(browserInfo);
        request.setScreenResolution(screenResolution);
        request.setTimezone("Asia/Shanghai");
        request.setLanguage("zh-CN");
        return request;
    }

    private UserDevice device(String fingerprint,
                              UserDevice.DeviceType type,
                              String osInfo,
                              String browserInfo,
                              String screenResolution) {
        UserDevice device = new UserDevice();
        device.setId(10L);
        device.setUser(user);
        device.setDeviceFingerprint(fingerprint);
        device.setDeviceName("Android Device");
        device.setDeviceType(type);
        device.setOsInfo(osInfo);
        device.setBrowserInfo(browserInfo);
        device.setScreenResolution(screenResolution);
        device.setTimezone("Asia/Shanghai");
        device.setLanguage("zh-CN");
        device.setIpAddress("127.0.0.1");
        device.setIsActive(true);
        device.setBindStatus(UserDevice.BindStatus.ACTIVE);
        device.setFirstBoundAt(LocalDateTime.now().minusDays(1));
        device.setLastActiveAt(LocalDateTime.now().minusHours(1));
        return device;
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(username);
        return user;
    }
}
