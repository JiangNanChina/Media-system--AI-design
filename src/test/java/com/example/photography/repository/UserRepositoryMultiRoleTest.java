package com.example.photography.repository;

import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryMultiRoleTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAllAssignedRolesAndSynchronizesTheLegacyHighestRoleColumn() {
        User saved = userRepository.saveAndFlush(user(
                "multi-role",
                UserRole.MEMBER,
                UserRole.ADVISOR,
                UserRole.DIRECTOR
        ));
        entityManager.clear();

        User reloaded = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getRoles())
                .containsExactly(UserRole.DIRECTOR, UserRole.ADVISOR, UserRole.MEMBER);
        assertThat(reloaded.getEffectiveRoles()).containsExactly(UserRole.DIRECTOR);
        assertThat(reloaded.getRole()).isEqualTo(UserRole.DIRECTOR);
        assertThat(entityManager.createNativeQuery("SELECT role FROM users WHERE id = :id")
                .setParameter("id", saved.getId())
                .getSingleResult())
                .isEqualTo("DIRECTOR");
    }

    @Test
    void roleFiltersMatchAssignedMembershipRatherThanOnlyTheEffectiveRole() {
        User directorAndAdvisor = userRepository.save(user(
                "director-advisor",
                UserRole.DIRECTOR,
                UserRole.ADVISOR
        ));
        User advisor = userRepository.save(user("advisor", UserRole.ADVISOR));
        userRepository.save(user("member", UserRole.MEMBER));
        userRepository.flush();
        entityManager.clear();

        assertThat(userRepository.findByRoleAndDeletedFalse(UserRole.ADVISOR))
                .extracting(User::getId)
                .containsExactlyInAnyOrder(directorAndAdvisor.getId(), advisor.getId());

        Page<User> result = userRepository.searchUsersWithFilters(
                null,
                UserRole.ADVISOR,
                null,
                null,
                PageRequest.of(0, 10)
        );
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(User::getId)
                .containsExactlyInAnyOrder(directorAndAdvisor.getId(), advisor.getId());
    }

    private User user(String username, UserRole... roles) {
        User user = new User(username, "password-123", username, UserRole.MEMBER);
        user.setEmail(username + "@example.test");
        user.setRoles(List.of(roles));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
