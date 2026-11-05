package com.goosepatrol.item;

import com.goosepatrol.entity.Goose;

import java.util.Objects;

/**
 * A single-use item the player can use to manage geese. Using an item applies its effect to every
 * goose within {@link #getRange()} of the player, then the item is used up. [FR_USE_ITEM]
 *
 * <p>New item types are added by subclassing and implementing {@link #applyTo(Goose)}.</p>
 */
public abstract class Item {

    private final String name;
    private final float range;

    /**
     * @param name  name shown in the inventory bar
     * @param range radius (world units) around the player within which geese are affected; must be positive
     */
    protected Item(String name, float range) {
        if (range <= 0f) {
            throw new IllegalArgumentException("range must be positive, was " + range);
        }
        this.name = Objects.requireNonNull(name, "name");
        this.range = range;
    }

    /** Applies this item's effect to one goose. */
    public abstract void applyTo(Goose goose);

    /** @return past-tense verb describing the effect, used in feedback such as "Bread fed 3 geese" */
    public abstract String getEffectVerb();

    /** @return one-line description shown in the HUD when the item is selected */
    public abstract String getDescription();

    public String getName() {
        return name;
    }

    public float getRange() {
        return range;
    }

    @Override
    public String toString() {
        return name;
    }
}
