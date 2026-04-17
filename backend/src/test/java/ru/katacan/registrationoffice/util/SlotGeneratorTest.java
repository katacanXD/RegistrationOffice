package ru.katacan.registrationoffice.util;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotGeneratorTest {

    @Test
    void generateSlots_always_uses_hour_step() {
        List<LocalTime> slots = SlotGenerator.generateSlots(
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                30,
                null,
                null
        );

        assertEquals(List.of(
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        ), slots);
    }

    @Test
    void generateSlots_excludes_full_hour_break() {
        List<LocalTime> slots = SlotGenerator.generateSlots(
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                60,
                LocalTime.of(13, 0),
                LocalTime.of(14, 0)
        );

        assertEquals(List.of(
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                LocalTime.of(17, 0)
        ), slots);
    }

    @Test
    void calculateTotalSlots_counts_hour_break_correctly() {
        int total = SlotGenerator.calculateTotalSlots(
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                60,
                LocalTime.of(13, 0),
                LocalTime.of(14, 0)
        );

        assertEquals(8, total);
    }
}

