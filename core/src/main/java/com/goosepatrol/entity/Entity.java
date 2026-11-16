package com.goosepatrol.entity;

import com.badlogic.gdx.math.Vector2;

/**
 * Base class for anything that occupies a circular area of the {@link GameMap}
 * (currently the {@link Player} and each {@link Goose}).
 *
 * <p>Subclasses decide which parts of the map they are allowed to stand on by implementing
 * {@link #canOccupy(GameMap, float, float)}; movement and collision resolution are shared here.</p>
 */
public abstract class Entity {

    private final Vector2 position;
    private final float radius;

    /**
     * @param x      initial x coordinate of the entity's centre
     * @param y      initial y coordinate of the entity's centre
     * @param radius collision radius; must be positive
     */
    protected Entity(float x, float y, float radius) {
        if (radius <= 0f) {
            throw new IllegalArgumentException("radius must be positive, was " + radius);
        }
        this.position = new Vector2(x, y);
        this.radius = radius;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getRadius() {
        return radius;
    }

    /** @return distance between the centres of this entity and {@code other} */
    public float distanceTo(Entity other) {
        return position.dst(other.position);
    }

    /** @return distance between the centre of this entity and the point (x, y) */
    public float distanceTo(float x, float y) {
        return position.dst(x, y);
    }

    /**
     * Whether this entity could have its centre at (x, y) on the given map.
     * Each subclass decides which obstacles block it.
     */
    protected abstract boolean canOccupy(GameMap map, float x, float y);

    /**
     * Moves the entity by (dx, dy). Each axis is resolved separately, so an entity walking
     * diagonally into a wall slides along it instead of stopping dead.
     *
     * @return {@code true} if the entity moved along at least one axis
     */
    protected boolean moveBy(float dx, float dy, GameMap map) {
        boolean moved = false;
        if (dx != 0f && canOccupy(map, position.x + dx, position.y)) {
            position.x += dx;
            moved = true;
        }
        if (dy != 0f && canOccupy(map, position.x, position.y + dy)) {
            position.y += dy;
            moved = true;
        }
        return moved;
    }
}
