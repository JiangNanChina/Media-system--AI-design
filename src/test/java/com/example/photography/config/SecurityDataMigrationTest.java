package com.example.photography.config;

import com.example.photography.model.entity.User;
import com.example.photography.model.enums.AccountStatus;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityDataMigrationTest {

    @Test
    void backfillsLegacyRoleAndSecurityDefaults() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User legacy = new User("legacy", "plain-password", "Legacy User", UserRole.MEMBER);
        ReflectionTestUtils.setField(legacy, "role", UserRole.ADMIN);
        ReflectionTestUtils.setField(legacy, "roles", new LinkedHashSet<>());
        legacy.setAccountStatus(null);
        legacy.setTokenVersion(null);
        legacy.setFailedLoginAttempts(null);
        legacy.setEnabled(true);
        when(userRepository.findAll()).thenReturn(List.of(legacy));
        when(passwordEncoder.encode("plain-password")).thenReturn("$2a$12$encoded");

        new SecurityDataMigration(userRepository, passwordEncoder).run(mock(ApplicationArguments.class));

        assertThat(legacy.getRoles()).containsExactly(UserRole.SUPER_ADMIN);
        assertThat(legacy.getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(legacy.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(legacy.getTokenVersion()).isZero();
        assertThat(legacy.getFailedLoginAttempts()).isZero();
        assertThat(legacy.getPassword()).isEqualTo("$2a$12$encoded");
        verify(userRepository).saveAll(List.of(legacy));
    }
}
