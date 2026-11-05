package com.goosepatrol.logic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link GameTimer}. Requirement: FR_TIMER_EXPIRE. */
class GameTimerTest {

    @Test
    @DisplayName("FR_TIMER_EXPIRE: the default session lasts exactly 5 minutes")
    void defaultDurationIsFiveMinutes() {
        GameTimer timer = new GameTimer(com.goosepatrol.GameConfig.GAME_DURATION_SECONDS);
        assertEquals(300f, timer.getDurationSeconds());
        assertEquals(300f, timer.getRemainingSeconds());
        assertEquals(0f, timer.getElapsedSeconds());
    }

    @Test
    void tickReducesRemainingTime() {
        GameTimer timer = new GameTimer(10f);
        timer.tick(2.5f);
        assertEquals(7.5f, timer.getRemainingSeconds(), 1e-5f);
        assertEquals(2.5f, timer.getElapsedSeconds(), 1e-5f);
        assertFalse(timer.isExpired());
    }

    @Test
    void remainingTimeNeverGoesNegativeAndTimerExpires() {
        GameTimer timer = new GameTimer(1f);
        timer.tick(5f);
        assertEquals(0f, timer.getRemainingSeconds());
        assertTrue(timer.isExpired());
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new GameTimer(0f));
        assertThrows(IllegalArgumentException.class, () -> new GameTimer(10f).tick(-1f));
    }

    @Test
    void formatsAsMinutesAndSecondsRoundingUp() {
        assertEquals("5:00", GameTimer.format(300f));
        assertEquals("5:00", GameTimer.format(299.2f));
        assertEquals("4:59", GameTimer.format(298.4f));
        assertEquals("1:05", GameTimer.format(65f));
        assertEquals("0:01", GameTimer.format(0.01f));
        assertEquals("0:00", GameTimer.format(0f));
        assertEquals("0:00", GameTimer.format(-3f));
    }
}
