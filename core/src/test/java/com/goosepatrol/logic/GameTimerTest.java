package com.goosepatrol.logic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link GameTimer}. Requirements: FR_TIMER, FR_TIMER_EXPIRE, QR_TIMER_ACCURACY. */
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
    @DisplayName("FR_TIMER: ticking counts the countdown down and play time up")
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
        assertEquals(1f, timer.getElapsedSeconds(), 1e-5f, "play time stops at expiry");
        assertTrue(timer.isExpired());
        timer.tick(1f);
        assertEquals(1f, timer.getElapsedSeconds(), 1e-5f);
    }

    @Test
    @DisplayName("QR_TIMER_ACCURACY: drift < 1 s over a 5-minute session of 60 FPS frames")
    void staysAccurateOverAFullSessionOfFrames() {
        GameTimer timer = new GameTimer(300f);
        float frame = 1f / 60f;
        int frames = 0;
        while (!timer.isExpired()) {
            timer.tick(frame);
            frames++;
        }
        float simulatedRealTime = frames * frame;
        assertEquals(300f, simulatedRealTime, 1f);
        assertEquals(300f, timer.getElapsedSeconds(), 1f);
        assertEquals(18000, frames, 1, "should expire after exactly 18000 frames");
    }

    @Test
    void removeTimeShortensCountdownButNotPlayTime() {
        GameTimer timer = new GameTimer(100f);
        timer.tick(10f);
        timer.removeTime(15f);
        assertEquals(75f, timer.getRemainingSeconds(), 1e-4f);
        assertEquals(10f, timer.getElapsedSeconds(), 1e-4f);
        timer.removeTime(500f);
        assertEquals(0f, timer.getRemainingSeconds());
        assertTrue(timer.isExpired());
        assertThrows(IllegalArgumentException.class, () -> timer.removeTime(-1f));
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
