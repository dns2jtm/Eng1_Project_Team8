package com.goosepatrol.entity;

import com.goosepatrol.GameConfig;
import com.goosepatrol.item.Inventory;

import java.util.Objects;

/**
 * The student avatar controlled by the user. Carries an {@link Inventory} of items for managing geese.
 */
public class Player extends Entity {

    private final float speed;
    private final Inventory inventory;
    private boolean mobbed;

    /** Creates a player with the default speed and radius from {@link GameConfig}. */
    public Player(float x, float y, Inventory inventory) {
        this(x, y, GameConfig.PLAYER_SPEED, inventory);
    }

    /**
     * @param speed     walking speed in world units per second; must be positive
     * @param inventory the player's items
     */
    public Player(float x, float y, float speed, Inventory inventory) {
        super(x, y, GameConfig.PLAYER_RADIUS);
        if (speed <= 0f) {
            throw new IllegalArgumentException("speed must be positive, was " + speed);
        }
        this.speed = speed;
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    /**
     * Moves the player in the given direction for one frame. The direction is normalised, so
     * diagonal movement is no faster than straight movement. Obstacles and map edges block movement.
     *
     * @param dirX  horizontal input (-1 left .. 1 right)
     * @param dirY  vertical input (-1 down .. 1 up)
     * @param delta frame time in seconds
     * @param map   map used for collision checks
     */
    public void move(float dirX, float dirY, float delta, GameMap map) {
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length == 0f || delta <= 0f) {
            return;
        }
        float distance = getCurrentSpeed() * delta;
        moveBy(dirX / length * distance, dirY / length * distance, map);
    }

    /** @return the walking speed after the "mobbed by geese" slow-down, if it applies */
    public float getCurrentSpeed() {
        return mobbed ? speed * GameConfig.PLAYER_MOBBED_SPEED_MULTIPLIER : speed;
    }

    /** Marks whether geese are currently crowding the player (which slows them down). */
    public void setMobbed(boolean mobbed) {
        this.mobbed = mobbed;
    }

    public boolean isMobbed() {
        return mobbed;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    protected boolean canOccupy(GameMap map, float x, float y) {
        return map.canPlayerOccupy(x, y, getRadius());
    }
}
