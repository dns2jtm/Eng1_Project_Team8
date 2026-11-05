package com.goosepatrol.logic;

/**
 * Counters shown on the HUD: how many items have been used and how many hidden events triggered
 * [FR_ITEM_COUNTER], plus how many geese have been dispersed (which drives the win condition).
 */
public class GameStats {

    private int itemsUsed;
    private int eventsTriggered;
    private int geeseDispersed;

    public void recordItemUsed() {
        itemsUsed++;
    }

    public void recordEventTriggered() {
        eventsTriggered++;
    }

    /**
     * @param count number of geese that have just dispersed; must not be negative
     */
    public void recordGeeseDispersed(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative, was " + count);
        }
        geeseDispersed += count;
    }

    public int getItemsUsed() {
        return itemsUsed;
    }

    public int getEventsTriggered() {
        return eventsTriggered;
    }

    public int getGeeseDispersed() {
        return geeseDispersed;
    }
}
