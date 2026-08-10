package com.example.photography.config;

import com.example.photography.model.entity.User;
import com.example.photography.model.enums.AccountStatus;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.security.migrate-legacy-users", havingValue = "true", matchIfMissing = true)
public class SecurityDataMigration implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<User> users = userRepository.findAll();
        int passwordCount = 0;
        int roleCount = 0;
        for (User user : users) {
            if (user.getAccountStatus() == null) {
                user.setAccountStatus(Boolean.TRUE.equals(user.getEnabled()) ? AccountStatus.ACTIVE : AccountStatus.DISABLED);
            }
            if (user.getTokenVersion() == null) user.setTokenVersion(0);
            if (user.getFailedLoginAttempts() == null) user.setFailedLoginAttempts(0);
            if (user.getRole() == UserRole.ADMIN) {
                user.setRole(UserRole.SUPER_ADMIN);
                roleCount++;
            }
            String password = user.getPassword();
            if (password != null && !password.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(password));
                passwordCount++;
            }
        }
        userRepository.saveAll(users);
        if (passwordCount > 0 || roleCount > 0) {
            log.info("用户安全迁移完成: 密码升级={}, 角色升级={}", passwordCount, roleCount);
        }
    }
}
