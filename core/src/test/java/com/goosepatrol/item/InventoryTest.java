package com.goosepatrol.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link Inventory}. */
class InventoryTest {

    @Test
    void emptyInventoryHasNoSelection() {
        Inventory inventory = new Inventory();
        assertTrue(inventory.isEmpty());
        assertNull(inventory.selected());
        inventory.selectNext();
        inventory.selectPrevious();
        assertEquals(0, inventory.getSelectedIndex());
    }

    @Test
    void firstItemIsSelectedByDefault() {
        Inventory inventory = new Inventory();
        Item bread = FoodItem.bread();
        inventory.add(bread);
        inventory.add(ScareItem.whistle());
        assertSame(bread, inventory.selected());
        assertEquals(2, inventory.size());
    }

    @Test
    void selectionCyclesAndWrapsAround() {
        Inventory inventory = new Inventory();
        Item a = FoodItem.bread();
        Item b = ScareItem.whistle();
        inventory.add(a);
        inventory.add(b);
        inventory.selectNext();
        assertSame(b, inventory.selected());
        inventory.selectNext();
        assertSame(a, inventory.selected());
        inventory.selectPrevious();
        assertSame(b, inventory.selected());
    }

    @Test
    void selectIgnoresOutOfRangeIndices() {
        Inventory inventory = new Inventory();
        inventory.add(FoodItem.bread());
        inventory.add(ScareItem.whistle());
        inventory.select(1);
        inventory.select(5);
        inventory.select(-1);
        assertEquals(1, inventory.getSelectedIndex());
    }

    @Test
    void removingSelectedItemKeepsSelectionOnSameSlot() {
        Inventory inventory = new Inventory();
        Item first = FoodItem.bread();
        Item second = FoodItem.bread();
        Item third = ScareItem.whistle();
        inventory.add(first);
        inventory.add(second);
        inventory.add(third);
        assertTrue(inventory.remove(first));
        assertSame(second, inventory.selected());
    }

    @Test
    void removingLastSelectedItemClampsSelection() {
        Inventory inventory = new Inventory();
        Item first = FoodItem.bread();
        Item last = ScareItem.whistle();
        inventory.add(first);
        inventory.add(last);
        inventory.select(1);
        inventory.remove(last);
        assertSame(first, inventory.selected());
        inventory.remove(first);
        assertNull(inventory.selected());
    }

    @Test
    void removingEarlierItemShiftsSelectionToKeepSameItem() {
        Inventory inventory = new Inventory();
        Item first = FoodItem.bread();
        Item second = ScareItem.whistle();
        inventory.add(first);
        inventory.add(second);
        inventory.select(1);
        inventory.remove(first);
        assertSame(second, inventory.selected());
    }

    @Test
    void removeMatchesByIdentityAndReportsMissingItems() {
        Inventory inventory = new Inventory();
        inventory.add(FoodItem.bread());
        assertFalse(inventory.remove(FoodItem.bread()));
        assertEquals(1, inventory.size());
    }

    @Test
    void itemsViewIsReadOnlyAndNullsAreRejected() {
        Inventory inventory = new Inventory();
        assertThrows(UnsupportedOperationException.class, () -> inventory.getItems().add(FoodItem.bread()));
        assertThrows(NullPointerException.class, () -> inventory.add(null));
    }
}
