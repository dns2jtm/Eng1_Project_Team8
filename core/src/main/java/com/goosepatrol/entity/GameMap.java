package com.goosepatrol.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.goosepatrol.GameConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Bounded 2D campus map containing buildings, water, walkways and spawn points.
 * Buildings block everyone; water blocks the player while allowing geese across.
 * [UR_CAMPUS_MAP, FR_MAP_BOUNDS, FR_CAMPUS_THEME]
 */
public class GameMap {

    private final float width;
    private final float height;
    private final List<Obstacle> obstacles;
    private final List<Rectangle> paths;
    private final Vector2 playerStart;
    private final List<Vector2> gooseSpawnPoints;

    /**
     * @param width            map width in world units; must be positive
     * @param height           map height in world units; must be positive
     * @param obstacles        buildings and water areas on the map
     * @param paths            decorative walkways
     * @param playerStart      player starting coordinates
     * @param gooseSpawnPoints coordinates where geese can spawn; must not be empty
     */
    public GameMap(float width, float height, List<Obstacle> obstacles, List<Rectangle> paths,
                   Vector2 playerStart, List<Vector2> gooseSpawnPoints) {
        if (width <= 0f || height <= 0f) {
            throw new IllegalArgumentException("map size must be positive");
        }
        if (gooseSpawnPoints.isEmpty()) {
            throw new IllegalArgumentException("at least one goose spawn point is required");
        }
        this.width = width;
        this.height = height;
        this.obstacles = List.copyOf(obstacles);
        this.paths = copyRectangles(paths);
        this.playerStart = new Vector2(Objects.requireNonNull(playerStart, "playerStart"));
        this.gooseSpawnPoints = copyVectors(gooseSpawnPoints);
    }

    /**
     * Creates the default campus map with five named university buildings, a central lake
     * and connecting footpaths. [FR_CAMPUS_THEME]
     */
    public static GameMap createCampus() {
        List<Obstacle> obstacles = List.of(
            new Obstacle("Library", Obstacle.Type.BUILDING, 150f, 470f, 230f, 150f),
            new Obstacle("Computer Science", Obstacle.Type.BUILDING, 880f, 470f, 250f, 160f),
            new Obstacle("Central Hall", Obstacle.Type.BUILDING, 560f, 105f, 200f, 120f),
            new Obstacle("Students' Union", Obstacle.Type.BUILDING, 110f, 105f, 200f, 115f),
            new Obstacle("Dining Hall", Obstacle.Type.BUILDING, 1000f, 105f, 170f, 125f),
            new Obstacle("Campus Lake", Obstacle.Type.WATER, 440f, 320f, 300f, 120f)
        );
        List<Rectangle> paths = List.of(
            new Rectangle(0f, 260f, GameConfig.WORLD_WIDTH, 40f),
            new Rectangle(800f, 0f, 40f, GameConfig.WORLD_HEIGHT),
            new Rectangle(380f, 520f, 500f, 36f),
            new Rectangle(240f, 220f, 36f, 250f)
        );
        List<Vector2> spawns = List.of(
            new Vector2(30f, 40f),
            new Vector2(1250f, 40f),
            new Vector2(30f, 650f),
            new Vector2(1250f, 650f),
            new Vector2(640f, 655f),
            new Vector2(640f, 30f)
        );
        return new GameMap(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, obstacles, paths,
            new Vector2(300f, 280f), spawns);
    }

    /** Returns true if a point (radius 0) is within the map bounds and clear of player-blocking obstacles. */
    public boolean isWalkable(float x, float y) {
        return canPlayerOccupy(x, y, 0f);
    }

    /** Returns true if the player circle of the given radius fits at (x, y). */
    public boolean canPlayerOccupy(float x, float y, float radius) {
        if (!isInsideBounds(x, y, radius)) {
            return false;
        }
        for (Obstacle obstacle : obstacles) {
            if (obstacle.getType().blocksPlayer() && obstacle.overlapsCircle(x, y, radius)) {
                return false;
            }
        }
        return true;
    }

    /** Returns true if a goose circle of the given radius fits at (x, y). Geese can cross water. */
    public boolean canGooseOccupy(float x, float y, float radius) {
        if (!isInsideBounds(x, y, radius)) {
            return false;
        }
        for (Obstacle obstacle : obstacles) {
            if (obstacle.getType().blocksGeese() && obstacle.overlapsCircle(x, y, radius)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInsideBounds(float x, float y, float radius) {
        return x - radius >= 0f && x + radius <= width && y - radius >= 0f && y + radius <= height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public List<Rectangle> getPaths() {
        return copyRectangles(paths);
    }

    public Vector2 getPlayerStart() {
        return new Vector2(playerStart);
    }

    public List<Vector2> getGooseSpawnPoints() {
        return copyVectors(gooseSpawnPoints);
    }

    private static List<Rectangle> copyRectangles(List<Rectangle> source) {
        List<Rectangle> copy = new ArrayList<>(source.size());
        for (Rectangle rectangle : source) {
            copy.add(new Rectangle(rectangle));
        }
        return Collections.unmodifiableList(copy);
    }

    private static List<Vector2> copyVectors(List<Vector2> source) {
        List<Vector2> copy = new ArrayList<>(source.size());
        for (Vector2 vector : source) {
            copy.add(new Vector2(vector));
        }
        return Collections.unmodifiableList(copy);
    }
}
