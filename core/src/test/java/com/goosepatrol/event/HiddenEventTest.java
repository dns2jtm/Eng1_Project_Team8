package com.goosepatrol.event;

import com.goosepatrol.TestMaps;
import com.goosepatrol.entity.Player;
import com.goosepatrol.item.FoodItem;
import com.goosepatrol.item.Inventory;
import com.goosepatrol.logic.GameWorld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link HiddenEvent} and the two concrete events. Requirement: FR_EVENT_TRIGGER. */
class HiddenEventTest {

    private static GameWorld emptyWorld() {
        return new GameWorld(TestMaps.empty(), List.of(), 0, 10, new Random(1));
    }

    @Test
    @DisplayName("FR_EVENT_TRIGGER: an event only triggers when the player overlaps its zone")
    void triggersOnlyWithinRadius() {
        HiddenEvent event = new PicnicBasketEvent(500f, 500f);
        Player far = new Player(600f, 500f, new Inventory());
        Player edge = new Player(500f + event.getRadius() + far.getRadius(), 500f, new Inventory());
        Player inside = new Player(505f, 500f, new Inventory());
        assertFalse(event.isTriggeredBy(far));
        assertTrue(event.isTriggeredBy(edge));
        assertTrue(event.isTriggeredBy(inside));
    }

    @Test
    @DisplayName("FR_EVENT_TRIGGER: an event triggers only once")
    void triggersOnlyOnce() {
        GameWorld world = emptyWorld();
        HiddenEvent event = new PicnicBasketEvent(world.getPlayer().getX(), world.getPlayer().getY());
        assertTrue(event.isTriggeredBy(world.getPlayer()));
        event.trigger(world);
        assertTrue(event.isTriggered());
        assertFalse(event.isTriggeredBy(world.getPlayer()));
        assertThrows(IllegalStateException.class, () -> event.trigger(world));
    }

    @Test
    void picnicBasketAddsBreadToInventory() {
        GameWorld world = emptyWorld();
        Inventory inventory = world.getPlayer().getInventory();
        int before = inventory.size();
        HiddenEvent event = new PicnicBasketEvent(0f, 0f);
        event.trigger(world);
        assertEquals(before + PicnicBasketEvent.BREAD_REWARD, inventory.size());
        assertTrue(inventory.getItems().get(inventory.size() - 1) instanceof FoodItem);
        assertTrue(event.isPositive());
        assertTrue(event.getMessage().contains("picnic"));
        assertEquals("Picnic Basket", event.getName());
    }

    @Test
    void gooseNestSpawnsAngryGeeseAroundTheNest() {
        GameWorld world = emptyWorld();
        GooseNestEvent event = new GooseNestEvent(200f, 200f);
        event.trigger(world);
        assertEquals(GooseNestEvent.GEESE_SPAWNED, world.getGeese().size());
        world.getGeese().forEach(goose -> assertEquals(40f, goose.distanceTo(200f, 200f), 1e-3f));
        assertFalse(event.isPositive());
        assertTrue(event.getMessage().contains("nest"));
    }

    @Test
    void rejectsInvalidRadius() {
        assertThrows(IllegalArgumentException.class, () -> new HiddenEvent("bad", 0f, 0f, 0f) {
            @Override
            protected void apply(GameWorld world) {
            }

            @Override
            public boolean isPositive() {
                return true;
            }

            @Override
            public String getMessage() {
                return "";
            }
        });
    }
}
