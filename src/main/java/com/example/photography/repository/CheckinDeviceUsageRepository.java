package com.example.photography.repository;

import com.example.photography.model.entity.CheckinDeviceUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CheckinDeviceUsageRepository extends JpaRepository<CheckinDeviceUsage, Long> {
    Optional<CheckinDeviceUsage> findByConfigurationIdAndUsageDateAndDeviceFingerprintHashAndDeletedFalse(
            Long configurationId, LocalDate usageDate, String deviceFingerprintHash);
}
