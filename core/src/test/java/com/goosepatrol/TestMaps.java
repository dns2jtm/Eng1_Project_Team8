package com.goosepatrol;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.goosepatrol.entity.GameMap;
import com.goosepatrol.entity.Obstacle;

import java.util.List;

/** Shared fixtures for tests: small, predictable maps. */
public final class TestMaps {

    private TestMaps() {
    }

    /** A 1000x1000 map with no obstacles, the player in the middle and one spawn point in a corner. */
    public static GameMap empty() {
        return new GameMap(1000f, 1000f, List.of(), List.of(), new Vector2(500f, 500f),
            List.of(new Vector2(50f, 50f)));
    }

    /**
     * A 1000x1000 map with a building at x 600-700 (y 400-600) and water at x 300-400 (y 400-600).
     * The player starts at (500, 500), between them.
     */
    public static GameMap withBuildingAndWater() {
        List<Obstacle> obstacles = List.of(
            new Obstacle("Building", Obstacle.Type.BUILDING, 600f, 400f, 100f, 200f),
            new Obstacle("Water", Obstacle.Type.WATER, 300f, 400f, 100f, 200f)
        );
        return new GameMap(1000f, 1000f, obstacles, List.of(new Rectangle(0f, 0f, 10f, 10f)),
            new Vector2(500f, 500f), List.of(new Vector2(50f, 50f), new Vector2(950f, 950f)));
    }
}
