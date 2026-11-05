package com.goosepatrol.event;

import com.goosepatrol.logic.GameWorld;

/**
 * Negative hidden event: the player disturbs a goose nest and angry geese appear around it.
 * These geese are added even if the usual spawn limit has been reached.
 */
public class GooseNestEvent extends HiddenEvent {

    /** Number of geese that appear when the nest is disturbed. */
    public static final int GEESE_SPAWNED = 3;
    private static final float TRIGGER_RADIUS = 28f;
    /** Distance from the nest centre at which the geese appear. */
    private static final float SPAWN_OFFSET = 40f;

    public GooseNestEvent(float x, float y) {
        super("Goose Nest", x, y, TRIGGER_RADIUS);
    }

    @Override
    protected void apply(GameWorld world) {
        for (int i = 0; i < GEESE_SPAWNED; i++) {
            double angle = 2 * Math.PI * i / GEESE_SPAWNED;
            float spawnX = getX() + (float) Math.cos(angle) * SPAWN_OFFSET;
            float spawnY = getY() + (float) Math.sin(angle) * SPAWN_OFFSET;
            world.spawnGooseAt(spawnX, spawnY);
        }
    }

    @Override
    public boolean isPositive() {
        return false;
    }

    @Override
    public String getMessage() {
        return "You disturbed a goose nest! " + GEESE_SPAWNED + " angry geese appear!";
    }
}
