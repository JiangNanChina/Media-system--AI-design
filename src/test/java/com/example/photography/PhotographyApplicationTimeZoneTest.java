package com.example.photography;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotographyApplicationTimeZoneTest {

    private final TimeZone originalTimeZone = TimeZone.getDefault();

    @AfterEach
    void restoreDefaultTimeZone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    void shouldUseShanghaiTimeRegardlessOfServerTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        PhotographyApplication.configureApplicationTimeZone();

        assertEquals("Asia/Shanghai", TimeZone.getDefault().getID());

        LocalDateTime applicationTime = LocalDateTime.of(
                2026, 10, 15, 15, 11, 44);
        ZonedDateTime instantInShanghai = applicationTime
                .atZone(PhotographyApplication.APPLICATION_TIME_ZONE);

        assertEquals(
                LocalDateTime.of(2026, 10, 15, 7, 11, 44),
                instantInShanghai.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime());
    }
}
