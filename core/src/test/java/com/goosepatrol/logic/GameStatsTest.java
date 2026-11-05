package com.goosepatrol.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Tests for {@link GameStats}. Requirement: FR_ITEM_COUNTER. */
class GameStatsTest {

    @Test
    void countersStartAtZero() {
        GameStats stats = new GameStats();
        assertEquals(0, stats.getItemsUsed());
        assertEquals(0, stats.getEventsTriggered());
        assertEquals(0, stats.getGeeseDispersed());
    }

    @Test
    void recordsEachCounterIndependently() {
        GameStats stats = new GameStats();
        stats.recordItemUsed();
        stats.recordItemUsed();
        stats.recordEventTriggered();
        stats.recordGeeseDispersed(3);
        assertEquals(2, stats.getItemsUsed());
        assertEquals(1, stats.getEventsTriggered());
        assertEquals(3, stats.getGeeseDispersed());
    }

    @Test
    void rejectsNegativeDispersedCount() {
        assertThrows(IllegalArgumentException.class, () -> new GameStats().recordGeeseDispersed(-1));
    }
}
