package com.goosepatrol.event;

import com.goosepatrol.logic.GameWorld;

/**
 * Negative hidden event: the player slips on a muddy path by the lake and loses time off the
 * countdown. Based on the "slippery path" event in Karthik's base game (commit 88ede63).
 */
public class SlipperyPathEvent extends HiddenEvent {

    /** Seconds removed from the countdown. */
    public static final float TIME_PENALTY_SECONDS = 15f;
    private static final float TRIGGER_RADIUS = 28f;

    public SlipperyPathEvent(float x, float y) {
        super("Slippery Path", x, y, TRIGGER_RADIUS);
    }

    @Override
    protected void apply(GameWorld world) {
        world.getTimer().removeTime(TIME_PENALTY_SECONDS);
    }

    @Override
    public boolean isPositive() {
        return false;
    }

    @Override
    public String getMessage() {
        return "You slipped on a muddy path! -" + (int) TIME_PENALTY_SECONDS + " seconds";
    }
}
