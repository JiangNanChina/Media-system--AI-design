package com.example.photography.model.enums;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 用户角色及其权限等级。
 */
public enum UserRole {
    MEMBER("成员", 10, 0),
    MINISTER("部长", 20, 2),
    ADVISOR("指导老师", 20, 1),
    DIRECTOR("主任", 30, 0),
    SUPER_ADMIN("系统超级管理员", 40, 1),
    /** @deprecated 仅用于兼容旧数据，新账号应使用 SUPER_ADMIN。 */
    @Deprecated
    ADMIN("管理员（兼容）", 40, 0);

    private static final Comparator<UserRole> PRIORITY_COMPARATOR = Comparator
            .comparingInt(UserRole::getLevel)
            .thenComparingInt(UserRole::getTieBreakPriority);

    private final String description;
    private final int level;
    private final int tieBreakPriority;

    UserRole(String description, int level, int tieBreakPriority) {
        this.description = description;
        this.level = level;
        this.tieBreakPriority = tieBreakPriority;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public int getTieBreakPriority() {
        return tieBreakPriority;
    }

    public UserRole normalized() {
        return this == ADMIN ? SUPER_ADMIN : this;
    }

    public boolean isSuperAdmin() {
        return normalized() == SUPER_ADMIN;
    }

    public boolean canManageBusiness() {
        UserRole normalized = normalized();
        return normalized == MINISTER || normalized == DIRECTOR || normalized == SUPER_ADMIN;
    }

    public boolean canReviewSubmission() {
        return canManageBusiness();
    }

    public static UserRole highest(Collection<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return MEMBER;
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(UserRole::normalized)
                .max(PRIORITY_COMPARATOR)
                .orElse(MEMBER);
    }

    /**
     * Normalizes legacy values, removes duplicates and returns a stable high-to-low order.
     */
    public static LinkedHashSet<UserRole> normalize(Collection<UserRole> roles) {
        Set<UserRole> normalized = new LinkedHashSet<>();
        if (roles != null) {
            roles.stream()
                    .filter(Objects::nonNull)
                    .map(UserRole::normalized)
                    .forEach(normalized::add);
        }
        if (normalized.isEmpty()) {
            normalized.add(MEMBER);
        }
        List<UserRole> ordered = normalized.stream()
                .sorted(PRIORITY_COMPARATOR.reversed())
                .toList();
        return new LinkedHashSet<>(ordered);
    }
}
