package com.example.photography.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "checkin_device_usages", uniqueConstraints = @UniqueConstraint(
        name = "uk_checkin_device_slot", columnNames = {"configuration_id", "usage_date", "device_fingerprint_hash"}))
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckinDeviceUsage extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuration_id", nullable = false)
    private CheckinConfiguration configuration;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;
    @Column(name = "device_fingerprint_hash", nullable = false, length = 64)
    private String deviceFingerprintHash;
}
