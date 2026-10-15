package com.example.photography.model.enums;

import com.example.photography.model.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRoleTest {

    @Test
    void onlyRolesAtTheHighestAssignedLevelParticipateInAuthorization() {
        User user = new User("director", "password-123", "Director", UserRole.MEMBER);

        user.setRoles(List.of(UserRole.ADVISOR, UserRole.DIRECTOR, UserRole.MEMBER));

        assertEquals(UserRole.DIRECTOR, user.getRole());
        assertEquals(Set.of(UserRole.DIRECTOR), user.getEffectiveRoles());
        assertTrue(user.hasRole(UserRole.DIRECTOR));
        assertFalse(user.hasRole(UserRole.ADVISOR));
        assertTrue(user.hasAssignedRole(UserRole.ADVISOR));
    }

    @Test
    void tiedHighestRolesBothParticipateInAuthorization() {
        User user = new User("manager", "password-123", "Manager", UserRole.MEMBER);

        user.setRoles(List.of(UserRole.MEMBER, UserRole.ADVISOR, UserRole.MINISTER));

        assertEquals(UserRole.MINISTER, user.getRole());
        assertEquals(Set.of(UserRole.MINISTER, UserRole.ADVISOR), user.getEffectiveRoles());
        assertTrue(user.hasRole(UserRole.MINISTER));
        assertTrue(user.hasRole(UserRole.ADVISOR));
        assertFalse(user.hasRole(UserRole.MEMBER));
    }

    @Test
    void legacyAdminIsNormalizedToSuperAdmin() {
        assertEquals(UserRole.SUPER_ADMIN, UserRole.highest(List.of(UserRole.ADMIN, UserRole.DIRECTOR)));
        assertEquals(Set.of(UserRole.SUPER_ADMIN), UserRole.normalize(List.of(UserRole.ADMIN)));
    }
}
