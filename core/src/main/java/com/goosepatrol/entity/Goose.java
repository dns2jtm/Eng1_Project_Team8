package com.goosepatrol.entity;

import com.goosepatrol.GameConfig;

/**
 * A hungry goose. It chases the player until it is distracted (fed or scared) and leaves once
 * its hunger reaches zero.
 *
 * <p>Behaviour follows the goose state machine: CHASING -> DISTRACTED -> CHASING, and
 * CHASING/DISTRACTED -> DISPERSED once fully fed.</p>
 */
public class Goose extends Entity {

    private int hunger;
    private GooseState state = GooseState.CHASING;
    private float distractionSecondsLeft;
    private boolean fleeing;
    private float facingX = 1f;
    private float facingY;

    /**
     * @param x      initial x
     * @param y      initial y
     * @param hunger units of food needed before the goose disperses; must be at least 1
     */
    public Goose(float x, float y, int hunger) {
        super(x, y, GameConfig.GOOSE_RADIUS);
        if (hunger < 1) {
            throw new IllegalArgumentException("hunger must be at least 1, was " + hunger);
        }
        this.hunger = hunger;
    }

    /**
     * Updates the goose for one frame. A chasing goose walks towards the target and stops when it
     * touches them. A distracted goose either stays put (eating) or runs away (scared) until its
     * distraction wears off.
     *
     * @param target the player
     * @param map    map used for collision checks
     * @param delta  frame time in seconds
     */
    public void chase(Player target, GameMap map, float delta) {
        if (state == GooseState.DISPERSED || delta <= 0f) {
            return;
        }
        if (state == GooseState.DISTRACTED) {
            if (fleeing) {
                stepTowards(target, -GameConfig.GOOSE_FLEE_SPEED * delta, map);
            }
            distractionSecondsLeft -= delta;
            if (distractionSecondsLeft <= 0f) {
                distractionSecondsLeft = 0f;
                fleeing = false;
                state = GooseState.CHASING;
            }
            return;
        }
        float stopDistance = getRadius() + target.getRadius();
        float gap = distanceTo(target) - stopDistance;
        if (gap > 0f) {
            stepTowards(target, Math.min(GameConfig.GOOSE_SPEED * delta, gap), map);
        }
    }

    /**
     * Gives the goose food. Its hunger drops by {@code amount}. If it is now full it disperses;
     * otherwise it stops to eat for a short time.
     *
     * @param amount units of food; must be positive
     */
    public void feed(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, was " + amount);
        }
        if (state == GooseState.DISPERSED) {
            return;
        }
        hunger = Math.max(0, hunger - amount);
        if (hunger == 0) {
            state = GooseState.DISPERSED;
            fleeing = false;
            distractionSecondsLeft = 0f;
        } else {
            distract(GameConfig.GOOSE_EAT_SECONDS, false);
        }
    }

    /**
     * Scares the goose so it runs away from the player for a time. Scaring doesn't reduce hunger.
     *
     * @param seconds how long the goose stays scared; must be positive
     */
    public void scare(float seconds) {
        if (seconds <= 0f) {
            throw new IllegalArgumentException("seconds must be positive, was " + seconds);
        }
        if (state == GooseState.DISPERSED) {
            return;
        }
        distract(seconds, true);
    }

    /**
     * Pushes the goose by (dx, dy), respecting collisions. Used by the world to keep geese from
     * stacking on top of each other.
     */
    public void nudge(float dx, float dy, GameMap map) {
        if (state != GooseState.DISPERSED) {
            moveBy(dx, dy, map);
        }
    }

    /**
     * @return {@code true} if this goose is chasing and close enough to crowd the player
     *         (which slows the player down)
     */
    public boolean isMobbing(Player player) {
        return state == GooseState.CHASING
            && distanceTo(player) <= getRadius() + player.getRadius() + GameConfig.GOOSE_CONTACT_TOLERANCE;
    }

    private void distract(float seconds, boolean scared) {
        state = GooseState.DISTRACTED;
        fleeing = scared;
        distractionSecondsLeft = Math.max(distractionSecondsLeft, seconds);
    }

    /** Moves {@code step} units towards the target (a negative step moves away from it). */
    private void stepTowards(Entity target, float step, GameMap map) {
        float dx = target.getX() - getX();
        float dy = target.getY() - getY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 1e-4f) {
            return;
        }
        dx /= length;
        dy /= length;
        float sign = Math.signum(step);
        facingX = dx * sign;
        facingY = dy * sign;
        moveBy(dx * step, dy * step, map);
    }

    public int getHunger() {
        return hunger;
    }

    public GooseState getState() {
        return state;
    }

    public boolean isDispersed() {
        return state == GooseState.DISPERSED;
    }

    public boolean isDistracted() {
        return state == GooseState.DISTRACTED;
    }

    /** @return {@code true} if the goose is distracted because it was scared (not because it is eating) */
    public boolean isFleeing() {
        return fleeing;
    }

    public float getDistractionSecondsLeft() {
        return distractionSecondsLeft;
    }

    /** @return x component of the unit direction the goose last moved in (used for drawing) */
    public float getFacingX() {
        return facingX;
    }

    /** @return y component of the unit direction the goose last moved in (used for drawing) */
    public float getFacingY() {
        return facingY;
    }

    @Override
    protected boolean canOccupy(GameMap map, float x, float y) {
        return map.canGooseOccupy(x, y, getRadius());
    }
}
