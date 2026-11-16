package com.goosepatrol.item;

import com.goosepatrol.entity.Goose;
import com.goosepatrol.entity.GooseState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the two starting item types. Requirement: FR_USE_ITEM. */
class ItemTest {

    @Test
    @DisplayName("FR_USE_ITEM: bread reduces hunger and distracts a hungry goose")
    void breadFeedsGoose() {
        Goose goose = new Goose(0f, 0f, 2);
        FoodItem.bread().applyTo(goose);
        assertEquals(1, goose.getHunger());
        assertEquals(GooseState.DISTRACTED, goose.getState());
        assertFalse(goose.isFleeing());
    }

    @Test
    @DisplayName("FR_GEESE_DISPERSE: bread disperses a goose whose hunger reaches zero")
    void breadDispersesFullGoose() {
        Goose goose = new Goose(0f, 0f, 1);
        FoodItem.bread().applyTo(goose);
        assertTrue(goose.isDispersed());
    }

    @Test
    @DisplayName("FR_USE_ITEM: the whistle scares a goose without feeding it")
    void whistleScaresGoose() {
        Goose goose = new Goose(0f, 0f, 2);
        ScareItem whistle = ScareItem.whistle();
        whistle.applyTo(goose);
        assertEquals(2, goose.getHunger());
        assertTrue(goose.isFleeing());
        assertEquals(whistle.getScareSeconds(), goose.getDistractionSecondsLeft(), 1e-5f);
    }

    @Test
    void factoryItemsHaveExpectedProperties() {
        FoodItem bread = FoodItem.bread();
        ScareItem whistle = ScareItem.whistle();
        assertEquals("Bread", bread.getName());
        assertEquals(FoodItem.BREAD_RANGE, bread.getRange());
        assertEquals(FoodItem.BREAD_FOOD_VALUE, bread.getFoodValue());
        assertEquals("Whistle", whistle.getName());
        assertEquals(ScareItem.WHISTLE_RANGE, whistle.getRange());
        assertEquals("fed", bread.getEffectVerb());
        assertEquals("scared", whistle.getEffectVerb());
        assertTrue(bread.getDescription().startsWith("Bread"));
        assertTrue(whistle.getDescription().startsWith("Whistle"));
        assertEquals("Bread", bread.toString());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new FoodItem("x", 0f, 1));
        assertThrows(IllegalArgumentException.class, () -> new FoodItem("x", 10f, 0));
        assertThrows(IllegalArgumentException.class, () -> new ScareItem("x", 10f, 0f));
        assertThrows(NullPointerException.class, () -> new ScareItem(null, 10f, 1f));
    }
}
