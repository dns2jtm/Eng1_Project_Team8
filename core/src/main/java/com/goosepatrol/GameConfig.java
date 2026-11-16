package com.goosepatrol;

/**
 * Gameplay and window constants shared across the game.
 * Requirement IDs in brackets refer to Req1.
 */
public final class GameConfig {

    /** Width of the playable campus map in world units (matches default window width). */
    public static final float WORLD_WIDTH = 1280f;
    /** Height of the playable campus map in world units (matches default window height). */
    public static final float WORLD_HEIGHT = 720f;

    /** Length of a game session: 5 real-world minutes. [FR_TIMER, FR_TIMER_EXPIRE, UR_TIME_LIMIT] */
    public static final float GAME_DURATION_SECONDS = 5 * 60f;

    /** Maximum length of one physics sub-step so collisions and event checks stay stable on slow frames. */
    public static final float MAX_SIMULATION_STEP_SECONDS = 0.05f;

    /** Player walking speed in world units per second. */
    public static final float PLAYER_SPEED = 170f;
    /** Collision radius of the player. */
    public static final float PLAYER_RADIUS = 14f;
    /** Speed multiplier while at least one chasing goose is touching the player. [FR_GEESE_BLOCK] */
    public static final float PLAYER_MOBBED_SPEED_MULTIPLIER = 0.45f;

    /** Goose chasing speed in world units per second. */
    public static final float GOOSE_SPEED = 85f;
    /** Goose speed when fleeing from a scare item. */
    public static final float GOOSE_FLEE_SPEED = 140f;
    /** Collision radius of a goose. */
    public static final float GOOSE_RADIUS = 12f;
    /** Extra distance beyond edge contact within which a goose still counts as crowding the player. */
    public static final float GOOSE_CONTACT_TOLERANCE = 6f;
    /** Seconds a goose stays busy eating after being fed (if still hungry). */
    public static final float GOOSE_EAT_SECONDS = 2.5f;
    /** Minimum starting hunger for a newly spawned goose. */
    public static final int GOOSE_MIN_HUNGER = 1;
    /** Maximum starting hunger for a newly spawned goose. */
    public static final int GOOSE_MAX_HUNGER = 2;

    /** Number of geese spawned at the start of a session. [FR_GEESE_SPAWN] */
    public static final int INITIAL_GEESE = 5;
    /** Maximum active geese before periodic spawning pauses. [FR_GEESE_SPAWN] */
    public static final int MAX_ACTIVE_GEESE = 9;
    /** Seconds between periodic goose spawns. [FR_GEESE_SPAWN] */
    public static final float GOOSE_SPAWN_INTERVAL_SECONDS = 20f;
    /** Minimum distance from the player when choosing a spawn point. */
    public static final float MIN_SPAWN_DISTANCE_FROM_PLAYER = 300f;

    /** Number of geese the player needs to disperse to win. [FR_WIN_CONDITION, QR_BALANCE] */
    public static final int GEESE_TO_DISPERSE_FOR_WIN = 8;

    /** Seconds feedback messages stay visible on the HUD. */
    public static final float MESSAGE_SECONDS = 3.5f;

    private GameConfig() {
    }
}
