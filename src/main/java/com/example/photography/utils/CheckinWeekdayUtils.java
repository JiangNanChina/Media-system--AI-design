package com.example.photography.utils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 晚自习打卡星期配置工具。
 * 使用 ISO 星期值：1=周一，7=周日。
 */
public final class CheckinWeekdayUtils {

    public static final String DEFAULT_REQUIRED_WEEKDAYS = "1,2,3,4";

    private CheckinWeekdayUtils() {
    }

    public static List<Integer> defaultRequiredWeekdays() {
        return List.of(1, 2, 3, 4);
    }

    public static String serializeRequiredWeekdays(List<Integer> weekdays) {
        String serialized = normalizeWeekdays(weekdays).stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return serialized.isBlank() ? DEFAULT_REQUIRED_WEEKDAYS : serialized;
    }

    public static List<Integer> parseRequiredWeekdays(String weekdays) {
        if (weekdays == null || weekdays.trim().isEmpty()) {
            return defaultRequiredWeekdays();
        }

        List<Integer> parsed = List.of(weekdays.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(CheckinWeekdayUtils::parseWeekday)
                .filter(Objects::nonNull)
                .filter(CheckinWeekdayUtils::isValidWeekday)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return parsed.isEmpty() ? defaultRequiredWeekdays() : parsed;
    }

    public static boolean isRequiredOnDate(String requiredWeekdays, LocalDate date) {
        if (date == null) {
            return false;
        }
        int weekday = date.getDayOfWeek().getValue();
        return parseRequiredWeekdays(requiredWeekdays).contains(weekday);
    }

    private static List<Integer> normalizeWeekdays(List<Integer> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            return defaultRequiredWeekdays();
        }

        List<Integer> normalized = weekdays.stream()
                .filter(Objects::nonNull)
                .filter(CheckinWeekdayUtils::isValidWeekday)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return normalized.isEmpty() ? defaultRequiredWeekdays() : normalized;
    }

    private static boolean isValidWeekday(Integer weekday) {
        return weekday >= 1 && weekday <= 7;
    }

    private static Integer parseWeekday(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
