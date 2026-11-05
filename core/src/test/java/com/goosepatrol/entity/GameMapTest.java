package com.goosepatrol.entity;

import com.goosepatrol.GameConfig;
import com.goosepatrol.TestMaps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link GameMap} and {@link Obstacle}: the map has clear limits. */
class GameMapTest {

    private final GameMap map = TestMaps.withBuildingAndWater();

    @Test
    @DisplayName("Map limits: points outside the map are not walkable")
    void outsideBoundsIsNotWalkable() {
        assertTrue(map.isWalkable(500f, 100f));
        assertFalse(map.isWalkable(-1f, 100f));
        assertFalse(map.isWalkable(100f, 1000.5f));
        assertFalse(map.canPlayerOccupy(5f, 100f, 10f));
        assertFalse(map.canGooseOccupy(995f, 100f, 10f));
    }

    @Test
    void buildingsBlockPlayerAndGeese() {
        assertFalse(map.isWalkable(650f, 500f));
        assertFalse(map.canPlayerOccupy(590f, 500f, 14f));
        assertFalse(map.canGooseOccupy(650f, 500f, 12f));
    }

    @Test
    void waterBlocksPlayerButNotGeese() {
        assertFalse(map.canPlayerOccupy(350f, 500f, 14f));
        assertTrue(map.canGooseOccupy(350f, 500f, 12f));
    }

    @Test
    void touchingAnObstacleEdgeIsAllowed() {
        assertTrue(map.canPlayerOccupy(586f, 500f, 14f));
    }

    @Test
    void circleNearCornerOverlapsOnlyWhenCloseEnough() {
        Obstacle box = new Obstacle("Box", Obstacle.Type.BUILDING, 0f, 0f, 10f, 10f);
        assertTrue(box.overlapsCircle(13f, 13f, 5f));   // distance to corner ~4.24 < 5
        assertFalse(box.overlapsCircle(14f, 14f, 5f));  // distance to corner ~5.66 > 5
        assertTrue(box.overlapsCircle(5f, 5f, 0f));     // centre inside
    }

    @Test
    void accessorsReturnDefensiveCopies() {
        map.getPlayerStart().set(0f, 0f);
        assertEquals(500f, map.getPlayerStart().x);
        map.getGooseSpawnPoints().get(0).set(1f, 1f);
        assertEquals(50f, map.getGooseSpawnPoints().get(0).x);
        map.getPaths().get(0).x = 99f;
        assertEquals(0f, map.getPaths().get(0).x);
        map.getObstacles().get(0).getBounds().x = 0f;
        assertEquals(600f, map.getObstacles().get(0).getBounds().x);
        assertThrows(UnsupportedOperationException.class, () -> map.getObstacles().clear());
    }

    @Test
    void rejectsInvalidMaps() {
        assertThrows(IllegalArgumentException.class,
            () -> new Obstacle("x", Obstacle.Type.BUILDING, 0f, 0f, 0f, 1f));
        assertThrows(IllegalArgumentException.class,
            () -> new GameMap(0f, 10f, java.util.List.of(), java.util.List.of(),
                new com.badlogic.gdx.math.Vector2(), java.util.List.of(new com.badlogic.gdx.math.Vector2())));
        assertThrows(IllegalArgumentException.class,
            () -> new GameMap(10f, 10f, java.util.List.of(), java.util.List.of(),
                new com.badlogic.gdx.math.Vector2(), java.util.List.of()));
    }

    @Test
    @DisplayName("Default campus: player start and every spawn point are valid positions")
    void campusMapIsConsistent() {
        GameMap campus = GameMap.createCampus();
        assertEquals(GameConfig.WORLD_WIDTH, campus.getWidth());
        assertEquals(GameConfig.WORLD_HEIGHT, campus.getHeight());
        assertTrue(campus.canPlayerOccupy(campus.getPlayerStart().x, campus.getPlayerStart().y,
            GameConfig.PLAYER_RADIUS));
        campus.getGooseSpawnPoints().forEach(p ->
            assertTrue(campus.canGooseOccupy(p.x, p.y, GameConfig.GOOSE_RADIUS), "spawn " + p));
        assertTrue(campus.getObstacles().stream().anyMatch(o -> o.getType() == Obstacle.Type.WATER));
        assertTrue(campus.getObstacles().stream().anyMatch(o -> o.getType() == Obstacle.Type.BUILDING));
    }
}
