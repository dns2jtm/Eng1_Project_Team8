package com.goosepatrol.event;

import com.goosepatrol.entity.Goose;
import com.goosepatrol.logic.GameWorld;

/**
 * Positive hidden event: the player finds a shortcut and every goose on the map loses track of
 * them, running off for a few seconds.
 *
 * <p>Based on the "lucky shortcut" event in Karthik's base game (commit 88ede63), which added 20
 * seconds to the timer. Adding time would let a session run past 5 minutes, which UR_TIME_LIMIT
 * forbids ("the game shall last at most 5 minutes"), so the shortcut gives the player breathing
 * room from the geese instead.</p>
 */
public class LuckyShortcutEvent extends HiddenEvent {

    /** How long every goose is thrown off the player's trail. */
    public static final float GEESE_LOSE_TRACK_SECONDS = 5f;
    private static final float TRIGGER_RADIUS = 28f;

    public LuckyShortcutEvent(float x, float y) {
        super("Lucky Shortcut", x, y, TRIGGER_RADIUS);
    }

    @Override
    protected void apply(GameWorld world) {
        for (Goose goose : world.getGeese()) {
            goose.scare(GEESE_LOSE_TRACK_SECONDS);
        }
    }

    @Override
    public boolean isPositive() {
        return true;
    }

    @Override
    public String getMessage() {
        return "Lucky shortcut! The geese lose track of you for a few seconds";
    }
}
