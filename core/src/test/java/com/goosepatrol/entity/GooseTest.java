package com.goosepatrol.entity;

import com.goosepatrol.GameConfig;
import com.goosepatrol.TestMaps;
import com.goosepatrol.item.Inventory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link Goose} behaviour and its state machine (architecture/v2/4-state-goose.puml). */
class GooseTest {

    private final GameMap map = TestMaps.empty();
    private final Player player = new Player(500f, 500f, new Inventory());

    @Test
    void startsChasing() {
        Goose goose = new Goose(100f, 500f, 2);
        assertEquals(GooseState.CHASING, goose.getState());
        assertEquals(2, goose.getHunger());
    }

    @Test
    @DisplayName("FR_GEESE_SPAWN: geese move towards the player")
    void chasingGooseMovesTowardsPlayerAtGooseSpeed() {
        Goose goose = new Goose(100f, 500f, 2);
        goose.chase(player, map, 1f);
        assertEquals(100f + GameConfig.GOOSE_SPEED, goose.getX(), 1e-3f);
        assertEquals(1f, goose.getFacingX(), 1e-4f);
    }

    @Test
    @DisplayName("FR_GEESE_BLOCK: a chasing goose closes in and crowds the player")
    void chasingGooseStopsWhenTouchingPlayer() {
        Goose goose = new Goose(450f, 500f, 2);
        for (int i = 0; i < 100; i++) {
            goose.chase(player, map, 0.1f);
        }
        float touching = goose.getRadius() + player.getRadius();
        assertEquals(touching, goose.distanceTo(player), 1e-2f);
        assertTrue(goose.isMobbing(player));
    }

    @Test
    void eatingGooseStaysStillThenResumesChasing() {
        Goose goose = new Goose(100f, 500f, 2);
        goose.feed(1);
        assertTrue(goose.isDistracted());
        assertFalse(goose.isMobbing(player));
        goose.chase(player, map, GameConfig.GOOSE_EAT_SECONDS / 2f);
        assertEquals(100f, goose.getX(), 1e-4f);
        goose.chase(player, map, GameConfig.GOOSE_EAT_SECONDS);
        assertEquals(GooseState.CHASING, goose.getState());
    }

    @Test
    void scaredGooseRunsAwayFromPlayerThenReturnsToChasing() {
        Goose goose = new Goose(400f, 500f, 2);
        float before = goose.distanceTo(player);
        goose.scare(1f);
        goose.chase(player, map, 0.5f);
        assertTrue(goose.distanceTo(player) > before);
        assertTrue(goose.isFleeing());
        goose.chase(player, map, 0.6f);
        assertEquals(GooseState.CHASING, goose.getState());
        assertFalse(goose.isFleeing());
    }

    @Test
    void longerDistractionIsNotShortenedByAShorterOne() {
        Goose goose = new Goose(100f, 500f, 3);
        goose.scare(10f);
        goose.feed(1);
        assertEquals(10f, goose.getDistractionSecondsLeft(), 1e-4f);
        assertFalse(goose.isFleeing(), "feeding switches the goose to eating");
    }

    @Test
    @DisplayName("FR_GEESE_DISPERSE: a goose disperses once fully fed and then ignores everything")
    void dispersesWhenFullyFed() {
        Goose goose = new Goose(100f, 500f, 2);
        goose.feed(5);
        assertTrue(goose.isDispersed());
        assertEquals(0, goose.getHunger());
        goose.feed(1);
        goose.scare(3f);
        goose.chase(player, map, 1f);
        goose.nudge(10f, 0f, map);
        assertEquals(GooseState.DISPERSED, goose.getState());
        assertEquals(100f, goose.getX(), 1e-4f);
        assertFalse(goose.isMobbing(player));
    }

    @Test
    void nudgeRespectsCollisions() {
        Goose goose = new Goose(GameConfig.GOOSE_RADIUS, 500f, 1);
        goose.nudge(-5f, 0f, map);
        assertEquals(GameConfig.GOOSE_RADIUS, goose.getX(), 1e-4f);
        goose.nudge(5f, 0f, map);
        assertEquals(GameConfig.GOOSE_RADIUS + 5f, goose.getX(), 1e-4f);
    }

    @Test
    void gooseOnTopOfPlayerDoesNotMove() {
        Goose goose = new Goose(500f, 500f, 1);
        goose.scare(1f);
        goose.chase(player, map, 0.1f);
        assertEquals(500f, goose.getX(), 1e-4f);
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Goose(0f, 0f, 0));
        Goose goose = new Goose(0f, 0f, 1);
        assertThrows(IllegalArgumentException.class, () -> goose.feed(0));
        assertThrows(IllegalArgumentException.class, () -> goose.scare(0f));
    }
}
