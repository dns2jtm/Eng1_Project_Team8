package com.goosepatrol;

/**
 * Central place for the tunable gameplay constants.
 *
 * <p>Keeping balance values here (rather than scattered as magic numbers) makes the game easy to
 * re-balance and lets the documentation refer to a single source of truth. Requirement IDs in
 * square brackets show which requirement each value supports.</p>
 */
public final class GameConfig {

    /** Width of the playable world, in world units (1 unit = 1 pixel at the default window size). */
    public static final float WORLD_WIDTH = 1280f;
    /** Height of the playable world, in world units. */
    public static final float WORLD_HEIGHT = 720f;

    /** Length of a game session: 5 real-world minutes. [FR_TIMER_EXPIRE] */
    public static final float GAME_DURATION_SECONDS = 5 * 60f;

    /** Player walking speed in world units per second. */
    public static final float PLAYER_SPEED = 170f;
    /** Collision radius of the player avatar. */
    public static final float PLAYER_RADIUS = 14f;
    /** Speed multiplier applied while at least one chasing goose is touching the player. */
    public static final float PLAYER_MOBBED_SPEED_MULTIPLIER = 0.45f;

    /** Goose chasing speed in world units per second (slower than the player, but they swarm). */
    public static final float GOOSE_SPEED = 85f;
    /** Goose speed when scared away from the player. */
    public static final float GOOSE_FLEE_SPEED = 140f;
    /** Collision radius of a goose. */
    public static final float GOOSE_RADIUS = 12f;
    /** Extra distance (beyond touching) within which a goose still counts as mobbing the player. */
    public static final float GOOSE_CONTACT_TOLERANCE = 6f;
    /** Seconds a goose stays busy eating after being fed (if it is still hungry). */
    public static final float GOOSE_EAT_SECONDS = 2.5f;
    /** Inclusive range of starting hunger for a newly spawned goose. */
    public static final int GOOSE_MIN_HUNGER = 1;
    /** Inclusive range of starting hunger for a newly spawned goose. */
    public static final int GOOSE_MAX_HUNGER = 2;

    /** Number of geese present when the game starts. [FR_GEESE_SPAWN] */
    public static final int INITIAL_GEESE = 5;
    /** Periodic spawning stops while this many geese are already on the map. [FR_GEESE_SPAWN] */
    public static final int MAX_ACTIVE_GEESE = 9;
    /** Seconds between periodic goose spawns. [FR_GEESE_SPAWN] */
    public static final float GOOSE_SPAWN_INTERVAL_SECONDS = 20f;
    /** New geese never spawn closer than this to the player (unless no spawn point is far enough). */
    public static final float MIN_SPAWN_DISTANCE_FROM_PLAYER = 300f;

    /** Number of geese the player must disperse to win. [FR_WIN_CONDITION] */
    public static final int GEESE_TO_DISPERSE_FOR_WIN = 8;

    /** How long feedback messages (item use, hidden events) stay on screen. */
    public static final float MESSAGE_SECONDS = 3.5f;

    private GameConfig() {
        // Constants holder: not instantiable.
    }
}
