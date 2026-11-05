package com.goosepatrol.entity;

import com.goosepatrol.GameConfig;
import com.goosepatrol.TestMaps;
import com.goosepatrol.item.Inventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link Player} movement. */
class PlayerTest {

    private static final float DELTA = 0.1f;

    @Test
    void movesAtConfiguredSpeed() {
        Player player = new Player(500f, 500f, 100f, new Inventory());
        player.move(1f, 0f, DELTA, TestMaps.empty());
        assertEquals(510f, player.getX(), 1e-4f);
        assertEquals(500f, player.getY(), 1e-4f);
    }

    @Test
    void diagonalMovementIsNotFaster() {
        Player player = new Player(500f, 500f, 100f, new Inventory());
        player.move(1f, 1f, DELTA, TestMaps.empty());
        float travelled = player.distanceTo(500f, 500f);
        assertEquals(10f, travelled, 1e-3f);
    }

    @Test
    void noInputOrZeroDeltaMeansNoMovement() {
        Player player = new Player(500f, 500f, new Inventory());
        player.move(0f, 0f, DELTA, TestMaps.empty());
        player.move(1f, 0f, 0f, TestMaps.empty());
        assertEquals(500f, player.getX());
    }

    @Test
    void isBlockedByBuildingsAndSlidesAlongThem() {
        GameMap map = TestMaps.withBuildingAndWater();
        Player player = new Player(580f, 500f, 100f, new Inventory());
        player.move(1f, 1f, DELTA, map); // building is directly to the right
        assertEquals(580f, player.getX(), 1e-4f);
        assertTrue(player.getY() > 500f, "should slide along the wall");
    }

    @Test
    void isBlockedByWaterAndMapEdges() {
        GameMap map = TestMaps.withBuildingAndWater();
        Player nearWater = new Player(420f, 500f, 100f, new Inventory());
        nearWater.move(-1f, 0f, DELTA, map);
        assertEquals(420f, nearWater.getX(), 1e-4f);

        Player nearEdge = new Player(GameConfig.PLAYER_RADIUS, 100f, 100f, new Inventory());
        nearEdge.move(-1f, 0f, DELTA, map);
        assertEquals(GameConfig.PLAYER_RADIUS, nearEdge.getX(), 1e-4f);
    }

    @Test
    void beingMobbedSlowsThePlayer() {
        Player player = new Player(500f, 500f, 100f, new Inventory());
        player.setMobbed(true);
        assertTrue(player.isMobbed());
        assertEquals(100f * GameConfig.PLAYER_MOBBED_SPEED_MULTIPLIER, player.getCurrentSpeed(), 1e-4f);
        player.move(1f, 0f, DELTA, TestMaps.empty());
        assertEquals(500f + 10f * GameConfig.PLAYER_MOBBED_SPEED_MULTIPLIER, player.getX(), 1e-4f);
    }

    @Test
    void rejectsInvalidConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new Player(0f, 0f, 0f, new Inventory()));
        assertThrows(NullPointerException.class, () -> new Player(0f, 0f, null));
    }
}
