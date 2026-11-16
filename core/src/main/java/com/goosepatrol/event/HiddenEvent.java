package com.goosepatrol.event;

import com.goosepatrol.entity.Player;
import com.goosepatrol.logic.GameWorld;

import java.util.Objects;

/**
 * An invisible trigger zone on the map [FR_EVENT_HIDDEN]. It fires the first time the player walks
 * into it [FR_EVENT_TRIGGER] and applies a positive or negative effect to the game world, at most
 * once [FR_EVENT_EFFECT].
 *
 * <p>New events are added by subclassing and implementing {@link #apply(GameWorld)},
 * {@link #isPositive()} and {@link #getMessage()}, without changing existing event classes.
 * [QR_EXTENSIBILITY]</p>
 */
public abstract class HiddenEvent {

    private final String name;
    private final float x;
    private final float y;
    private final float radius;
    private boolean triggered;

    /**
     * @param name   internal name (shown in debugging and tests)
     * @param x      centre x of the trigger zone
     * @param y      centre y of the trigger zone
     * @param radius radius of the trigger zone; must be positive
     */
    protected HiddenEvent(String name, float x, float y, float radius) {
        if (radius <= 0f) {
            throw new IllegalArgumentException("radius must be positive, was " + radius);
        }
        this.name = Objects.requireNonNull(name, "name");
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * @return {@code true} if the event hasn't fired yet and the player overlaps its trigger zone
     */
    public boolean isTriggeredBy(Player player) {
        return !triggered && player.distanceTo(x, y) <= radius + player.getRadius();
    }

    /**
     * Fires the event: marks it as triggered and applies its effect.
     *
     * @throws IllegalStateException if the event has already fired
     */
    public final void trigger(GameWorld world) {
        if (triggered) {
            throw new IllegalStateException("Hidden event '" + name + "' has already been triggered");
        }
        triggered = true;
        apply(world);
    }

    /** Applies this event's effect to the world. Called exactly once, by {@link #trigger(GameWorld)}. */
    protected abstract void apply(GameWorld world);

    /** @return {@code true} if the effect helps the player (affects how feedback is shown) */
    public abstract boolean isPositive();

    /** @return message shown to the player when the event fires */
    public abstract String getMessage();

    public boolean isTriggered() {
        return triggered;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }
}
