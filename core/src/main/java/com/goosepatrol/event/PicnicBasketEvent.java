package com.goosepatrol.event;

import com.goosepatrol.item.FoodItem;
import com.goosepatrol.logic.GameWorld;

/**
 * Positive hidden event: the player finds an abandoned picnic basket and gets extra bread.
 */
public class PicnicBasketEvent extends HiddenEvent {

    /** Number of Bread items added to the player's inventory. */
    public static final int BREAD_REWARD = 2;
    private static final float TRIGGER_RADIUS = 28f;

    public PicnicBasketEvent(float x, float y) {
        super("Picnic Basket", x, y, TRIGGER_RADIUS);
    }

    @Override
    protected void apply(GameWorld world) {
        for (int i = 0; i < BREAD_REWARD; i++) {
            world.getPlayer().getInventory().add(FoodItem.bread());
        }
    }

    @Override
    public boolean isPositive() {
        return true;
    }

    @Override
    public String getMessage() {
        return "You found an abandoned picnic basket! +" + BREAD_REWARD + " Bread";
    }
}
