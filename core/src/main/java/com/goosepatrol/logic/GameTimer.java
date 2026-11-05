package com.goosepatrol.logic;

/**
 * Counts down the length of a game session (5 real-world minutes by default). [FR_TIMER_EXPIRE]
 *
 * <p>The timer only advances when {@link #tick(float)} is called. {@link GameWorld} calls it only
 * while the game is running, so the timer is frozen while the game is paused.</p>
 */
public class GameTimer {

    private final float durationSeconds;
    private float remainingSeconds;

    /**
     * @param durationSeconds total session length; must be positive
     */
    public GameTimer(float durationSeconds) {
        if (durationSeconds <= 0f) {
            throw new IllegalArgumentException("durationSeconds must be positive, was " + durationSeconds);
        }
        this.durationSeconds = durationSeconds;
        this.remainingSeconds = durationSeconds;
    }

    /**
     * Advances the timer by {@code delta} seconds. The remaining time never goes below zero.
     *
     * @param delta elapsed time in seconds; must not be negative
     */
    public void tick(float delta) {
        if (delta < 0f) {
            throw new IllegalArgumentException("delta must not be negative, was " + delta);
        }
        remainingSeconds = Math.max(0f, remainingSeconds - delta);
    }

    /** @return {@code true} once the full duration has elapsed */
    public boolean isExpired() {
        return remainingSeconds <= 0f;
    }

    public float getRemainingSeconds() {
        return remainingSeconds;
    }

    public float getElapsedSeconds() {
        return durationSeconds - remainingSeconds;
    }

    public float getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * Formats a number of seconds as {@code m:ss}, rounding up so a countdown only shows
     * {@code 0:00} once it has actually finished.
     */
    public static String format(float seconds) {
        int total = (int) Math.ceil(Math.max(0f, seconds));
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
