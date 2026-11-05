package com.goosepatrol.item;

import com.goosepatrol.entity.Goose;

/**
 * An item that feeds geese. Each goose in range loses {@code foodValue} hunger; geese that are now
 * full disperse, and the rest stop to eat for a moment.
 */
public class FoodItem extends Item {

    /** Range of the starting "Bread" item. */
    public static final float BREAD_RANGE = 120f;
    /** Food value of the starting "Bread" item. */
    public static final int BREAD_FOOD_VALUE = 1;

    private final int foodValue;

    /**
     * @param foodValue hunger removed from each goose in range; must be positive
     */
    public FoodItem(String name, float range, int foodValue) {
        super(name, range);
        if (foodValue <= 0) {
            throw new IllegalArgumentException("foodValue must be positive, was " + foodValue);
        }
        this.foodValue = foodValue;
    }

    /** Creates the "Bread" starting item. */
    public static FoodItem bread() {
        return new FoodItem("Bread", BREAD_RANGE, BREAD_FOOD_VALUE);
    }

    @Override
    public void applyTo(Goose goose) {
        goose.feed(foodValue);
    }

    @Override
    public String getEffectVerb() {
        return "fed";
    }

    @Override
    public String getDescription() {
        return getName() + ": feeds nearby geese. Full geese fly away.";
    }

    public int getFoodValue() {
        return foodValue;
    }
}
