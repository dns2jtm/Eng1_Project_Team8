package com.goosepatrol.logic;

/**
 * The 5-minute countdown for a game session. [FR_TIMER, FR_TIMER_EXPIRE, QR_TIMER_ACCURACY]
 *
 * <p>The timer only advances when {@link #tick(float)} is called. {@link GameWorld} calls it only
 * while the game is running, so the timer is frozen while the game is paused. It keeps two
 * values:</p>
 * <ul>
 *   <li><b>remaining time</b>: the countdown shown to the player. Hidden events can take time off
 *       it ({@link #removeTime(float)}).</li>
 *   <li><b>elapsed play time</b>: how long the player has actually been playing (excluding pauses),
 *       shown on the end screen.</li>
 * </ul>
 * Both use {@code double} internally, so rounding error stays well under the 1-second limit in
 * QR_TIMER_ACCURACY even after thousands of frames.
 */
public class GameTimer {

    private final float durationSeconds;
    private double remainingSeconds;
    private double elapsedSeconds;

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
     * Advances the timer by {@code delta} seconds of play. The remaining time never goes below zero,
     * and play time stops counting once the timer has expired.
     *
     * @param delta elapsed time in seconds; must not be negative
     */
    public void tick(float delta) {
        if (delta < 0f) {
            throw new IllegalArgumentException("delta must not be negative, was " + delta);
        }
        if (isExpired()) {
            return;
        }
        double used = Math.min(delta, remainingSeconds);
        remainingSeconds -= used;
        elapsedSeconds += used;
    }

    /**
     * Takes time off the countdown, e.g. as a hidden-event penalty. Never goes below zero.
     *
     * @param seconds time to remove; must not be negative
     */
    public void removeTime(float seconds) {
        if (seconds < 0f) {
            throw new IllegalArgumentException("seconds must not be negative, was " + seconds);
        }
        remainingSeconds = Math.max(0d, remainingSeconds - seconds);
    }

    /** @return {@code true} once the countdown has reached zero */
    public boolean isExpired() {
        return remainingSeconds <= 0d;
    }

    public float getRemainingSeconds() {
        return (float) remainingSeconds;
    }

    /** @return time actually spent playing (excluding pauses); not affected by time penalties */
    public float getElapsedSeconds() {
        return (float) elapsedSeconds;
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
