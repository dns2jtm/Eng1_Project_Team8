package com.goosepatrol.entity;

import com.badlogic.gdx.math.Rectangle;

import java.util.Objects;

/**
 * An axis-aligned rectangular area of the campus that blocks movement, such as a building or the lake.
 * Immutable. [FR_MAP_BOUNDS]
 */
public final class Obstacle {

    /** Kind of obstacle; determines who it blocks and how it is drawn. */
    public enum Type {
        /** Blocks both the player and geese. */
        BUILDING(true, true),
        /** Blocks the player, but geese can swim across it. */
        WATER(true, false);

        private final boolean blocksPlayer;
        private final boolean blocksGeese;

        Type(boolean blocksPlayer, boolean blocksGeese) {
            this.blocksPlayer = blocksPlayer;
            this.blocksGeese = blocksGeese;
        }

        public boolean blocksPlayer() {
            return blocksPlayer;
        }

        public boolean blocksGeese() {
            return blocksGeese;
        }
    }

    private final String name;
    private final Type type;
    private final Rectangle bounds;

    /**
     * @param name   label shown on the map, e.g. "Library"
     * @param type   kind of obstacle
     * @param x      left edge
     * @param y      bottom edge
     * @param width  width (must be positive)
     * @param height height (must be positive)
     */
    public Obstacle(String name, Type type, float x, float y, float width, float height) {
        if (width <= 0f || height <= 0f) {
            throw new IllegalArgumentException("obstacle size must be positive");
        }
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.bounds = new Rectangle(x, y, width, height);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    /** @return a defensive copy of the obstacle's bounds */
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    /**
     * Circle–rectangle overlap test. Touching the edge exactly does not count as overlapping,
     * so entities can walk flush against walls.
     *
     * @return {@code true} if a circle centred at (cx, cy) with the given radius intrudes into this obstacle
     */
    public boolean overlapsCircle(float cx, float cy, float radius) {
        float closestX = Math.max(bounds.x, Math.min(cx, bounds.x + bounds.width));
        float closestY = Math.max(bounds.y, Math.min(cy, bounds.y + bounds.height));
        float dx = cx - closestX;
        float dy = cy - closestY;
        boolean centreInside = bounds.contains(cx, cy);
        return centreInside || dx * dx + dy * dy < radius * radius;
    }
}
