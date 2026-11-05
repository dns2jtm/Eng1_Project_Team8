package com.goosepatrol.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The player's items, shown as a hotbar. One slot is always selected while the inventory isn't empty.
 */
public class Inventory {

    private final List<Item> items = new ArrayList<>();
    private int selectedIndex;

    /** Adds an item to the end of the hotbar. */
    public void add(Item item) {
        items.add(Objects.requireNonNull(item, "item"));
    }

    /**
     * Removes the given item (matched by identity). The selection stays on the same slot where
     * possible, so using several items of one type in a row needs no re-selecting.
     *
     * @return {@code true} if the item was in the inventory
     */
    public boolean remove(Item item) {
        int index = indexOfIdentity(item);
        if (index < 0) {
            return false;
        }
        items.remove(index);
        if (index < selectedIndex) {
            selectedIndex--;
        }
        clampSelection();
        return true;
    }

    /** @return the selected item, or {@code null} if the inventory is empty */
    public Item selected() {
        return items.isEmpty() ? null : items.get(selectedIndex);
    }

    /** Selects the slot at {@code index}. Out-of-range indices are ignored. */
    public void select(int index) {
        if (index >= 0 && index < items.size()) {
            selectedIndex = index;
        }
    }

    /** Moves the selection one slot right, wrapping round to the first slot. */
    public void selectNext() {
        if (!items.isEmpty()) {
            selectedIndex = (selectedIndex + 1) % items.size();
        }
    }

    /** Moves the selection one slot left, wrapping round to the last slot. */
    public void selectPrevious() {
        if (!items.isEmpty()) {
            selectedIndex = (selectedIndex - 1 + items.size()) % items.size();
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** @return a read-only view of the items, in hotbar order */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    private int indexOfIdentity(Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

    private void clampSelection() {
        if (items.isEmpty()) {
            selectedIndex = 0;
        } else if (selectedIndex >= items.size()) {
            selectedIndex = items.size() - 1;
        }
    }
}
