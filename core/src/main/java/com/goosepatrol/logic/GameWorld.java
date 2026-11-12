package com.goosepatrol.logic;

import com.badlogic.gdx.math.Vector2;
import com.goosepatrol.GameConfig;
import com.goosepatrol.entity.GameMap;
import com.goosepatrol.entity.Goose;
import com.goosepatrol.entity.Player;
import com.goosepatrol.event.GooseNestEvent;
import com.goosepatrol.event.HiddenEvent;
import com.goosepatrol.event.LuckyShortcutEvent;
import com.goosepatrol.event.PicnicBasketEvent;
import com.goosepatrol.event.SlipperyPathEvent;
import com.goosepatrol.item.FoodItem;
import com.goosepatrol.item.Inventory;
import com.goosepatrol.item.Item;
import com.goosepatrol.item.ScareItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Owns and updates all game state for one session: the map, player, geese, hidden events, timer and
 * statistics.
 *
 * <p>This class has no rendering or input code (it never touches {@code Gdx.graphics} or
 * {@code Gdx.input}), so the whole game can be unit-tested without a window. The presentation layer
 * ({@code GameScreen}) turns key presses into calls such as {@link #setPlayerDirection(float, float)},
 * {@link #useSelectedItem()} and {@link #togglePause()}, and calls {@link #update(float)} once per frame.</p>
 *
 * <p>The update order follows the "one frame of gameplay" sequence diagram
 * (architecture/v2/6-seq-update.puml).</p>
 */
public class GameWorld {

    /** Bread items in the starting inventory. */
    public static final int STARTING_BREAD = 4;
    /** Whistle items in the starting inventory. */
    public static final int STARTING_WHISTLES = 3;

    private final GameMap map;
    private final Player player;
    private final List<Goose> geese = new ArrayList<>();
    private final List<HiddenEvent> events;
    private final GameTimer timer = new GameTimer(GameConfig.GAME_DURATION_SECONDS);
    private final GameStats stats = new GameStats();
    private final Random random;
    private final int geeseToDisperseForWin;
    private final Vector2 playerDirection = new Vector2();

    private GameState state = GameState.PAUSED; // [FR_START_PAUSED]
    private boolean started;
    private float secondsUntilNextSpawn = GameConfig.GOOSE_SPAWN_INTERVAL_SECONDS;
    private WorldMessage message;
    private float messageSecondsLeft;

    /**
     * Creates a world with the default campus map, hidden events and balance values.
     * There are four hidden events, two positive and two negative; A1 needs at least two.
     * [FR_EVENT_COUNT, FR_EVENT_EFFECT]
     *
     * @param random source of randomness (pass a seeded instance for reproducible sessions)
     */
    public static GameWorld createDefault(Random random) {
        List<HiddenEvent> events = List.of(
            new PicnicBasketEvent(1200f, 400f),
            new GooseNestEvent(790f, 380f),
            new LuckyShortcutEvent(620f, 620f),
            new SlipperyPathEvent(420f, 300f)
        );
        return new GameWorld(GameMap.createCampus(), events, GameConfig.INITIAL_GEESE,
            GameConfig.GEESE_TO_DISPERSE_FOR_WIN, random);
    }

    /**
     * Creates a world. Each parameter can be swapped out, which keeps the class easy to test.
     *
     * @param map                   the map to play on
     * @param events                hidden events placed on the map
     * @param initialGeese          number of geese spawned straight away
     * @param geeseToDisperseForWin dispersed geese needed to win; must be positive
     * @param random                source of randomness for spawning
     */
    public GameWorld(GameMap map, List<HiddenEvent> events, int initialGeese, int geeseToDisperseForWin,
                     Random random) {
        if (initialGeese < 0) {
            throw new IllegalArgumentException("initialGeese must not be negative, was " + initialGeese);
        }
        if (geeseToDisperseForWin <= 0) {
            throw new IllegalArgumentException("geeseToDisperseForWin must be positive");
        }
        this.map = Objects.requireNonNull(map, "map");
        this.events = new ArrayList<>(events);
        this.random = Objects.requireNonNull(random, "random");
        this.geeseToDisperseForWin = geeseToDisperseForWin;

        Vector2 start = map.getPlayerStart();
        this.player = new Player(start.x, start.y, createStartingInventory());
        for (int i = 0; i < initialGeese; i++) {
            spawnGooseAtSpawnPoint();
        }
    }

    /** The starting inventory: two kinds of item, several of each. [FR_INITIAL_INVENTORY] */
    private static Inventory createStartingInventory() {
        Inventory inventory = new Inventory();
        for (int i = 0; i < STARTING_BREAD; i++) {
            inventory.add(FoodItem.bread());
        }
        for (int i = 0; i < STARTING_WHISTLES; i++) {
            inventory.add(ScareItem.whistle());
        }
        return inventory;
    }

    // ------------------------------------------------------------------------------------------
    // Frame update
    // ------------------------------------------------------------------------------------------

    /**
     * Advances the game by {@code delta} seconds of real time. Does nothing unless the game is
     * {@link GameState#RUNNING}. [FR_PAUSE, QR_PAUSE_RESPONSE]
     *
     * <p>The timer is advanced by the exact real frame time, so it follows real time even when a
     * frame is slow [QR_TIMER_ACCURACY]. Movement, collisions, hidden events and spawning are then
     * simulated in steps of at most {@link GameConfig#MAX_SIMULATION_STEP_SECONDS}, so they stay
     * stable and nothing can move far enough in one step to jump over an event or an obstacle.</p>
     *
     * @param delta real time since the last frame, in seconds; must not be negative
     */
    public void update(float delta) {
        if (delta < 0f) {
            throw new IllegalArgumentException("delta must not be negative, was " + delta);
        }
        if (state != GameState.RUNNING) {
            return;
        }
        timer.tick(delta);
        tickMessage(delta);
        float remaining = delta;
        while (remaining > 0f) {
            float step = Math.min(remaining, GameConfig.MAX_SIMULATION_STEP_SECONDS);
            simulateStep(step);
            remaining -= step;
        }
        updateEndConditions();
    }

    /** Simulates one step of movement and interaction, following the frame-update sequence diagram. */
    private void simulateStep(float delta) {
        updateGeese(delta);
        updatePlayer(delta);
        checkHiddenEvents();
        updateSpawning(delta);
    }

    private void updateGeese(float delta) {
        for (Goose goose : geese) {
            goose.chase(player, map, delta);
        }
        separateGeese();
    }

    /** Pushes overlapping geese apart so they surround the player instead of stacking up. */
    private void separateGeese() {
        float minDistance = 2f * GameConfig.GOOSE_RADIUS;
        for (int i = 0; i < geese.size(); i++) {
            Goose a = geese.get(i);
            for (int j = i + 1; j < geese.size(); j++) {
                Goose b = geese.get(j);
                float dx = b.getX() - a.getX();
                float dy = b.getY() - a.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance >= minDistance) {
                    continue;
                }
                if (distance < 1e-4f) {
                    // Exactly on top of each other: pick an arbitrary direction to separate along.
                    dx = 1f;
                    dy = 0f;
                    distance = 1f;
                }
                float push = (minDistance - distance) / 2f;
                float nx = dx / distance;
                float ny = dy / distance;
                a.nudge(-nx * push, -ny * push, map);
                b.nudge(nx * push, ny * push, map);
            }
        }
    }

    /** Geese touching the player slow them down, then the player moves. [FR_GEESE_BLOCK, FR_PLAYER_MOVE] */
    private void updatePlayer(float delta) {
        boolean mobbed = false;
        for (Goose goose : geese) {
            if (goose.isMobbing(player)) {
                mobbed = true;
                break;
            }
        }
        player.setMobbed(mobbed);
        player.move(playerDirection.x, playerDirection.y, delta, map);
    }

    /** Fires any hidden event the player has just walked into. [FR_EVENT_TRIGGER, FR_EVENT_COUNTER] */
    private void checkHiddenEvents() {
        for (HiddenEvent event : events) {
            if (event.isTriggeredBy(player)) {
                event.trigger(this);
                stats.recordEventTriggered();
                showMessage(event.getMessage(),
                    event.isPositive() ? WorldMessage.Tone.POSITIVE : WorldMessage.Tone.NEGATIVE);
            }
        }
    }

    /** Spawns a new goose at regular intervals, up to the active-goose limit. [FR_GEESE_SPAWN] */
    private void updateSpawning(float delta) {
        secondsUntilNextSpawn -= delta;
        while (secondsUntilNextSpawn <= 0f) {
            secondsUntilNextSpawn += GameConfig.GOOSE_SPAWN_INTERVAL_SECONDS;
            if (geese.size() < GameConfig.MAX_ACTIVE_GEESE) {
                spawnGooseAtSpawnPoint();
            }
        }
    }

    /** Checks for a win (enough geese dispersed) or a loss (timer expired). */
    private void updateEndConditions() {
        if (stats.getGeeseDispersed() >= geeseToDisperseForWin) {
            state = GameState.WON; // [FR_WIN_CONDITION]
        } else if (timer.isExpired()) {
            state = GameState.LOST; // [FR_TIMER_EXPIRE]
        }
    }

    private void tickMessage(float delta) {
        if (message != null) {
            messageSecondsLeft -= delta;
            if (messageSecondsLeft <= 0f) {
                message = null;
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Player commands (called by the presentation layer)
    // ------------------------------------------------------------------------------------------

    /**
     * Switches between PAUSED and RUNNING. Has no effect once the game has ended. [FR_PAUSE]
     */
    public void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            started = true;
        }
    }

    /** Pauses the game if it is running (e.g. when the window loses focus). */
    public void pause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        }
    }

    /**
     * Sets the direction the player is trying to move in. It is applied on later frames until changed.
     *
     * @param dirX horizontal input (-1 .. 1)
     * @param dirY vertical input (-1 .. 1)
     */
    public void setPlayerDirection(float dirX, float dirY) {
        playerDirection.set(dirX, dirY);
    }

    /**
     * Uses the selected inventory item on every goose within its range, then removes the item and
     * counts it as used. Follows the "use an item" sequence diagram.
     * [FR_USE_ITEM, FR_ITEM_COUNTER, FR_GEESE_DISPERSE]
     *
     * @return {@code true} if an item was used; {@code false} if the game isn't running or the
     *         inventory is empty
     */
    public boolean useSelectedItem() {
        if (state != GameState.RUNNING) {
            return false;
        }
        Inventory inventory = player.getInventory();
        Item item = inventory.selected();
        if (item == null) {
            return false;
        }
        int affected = 0;
        for (Goose goose : geese) {
            if (goose.distanceTo(player) <= item.getRange()) {
                item.applyTo(goose);
                affected++;
            }
        }
        inventory.remove(item);
        stats.recordItemUsed();
        int dispersed = removeDispersedGeese();
        showMessage(describeItemUse(item, affected, dispersed),
            affected > 0 ? WorldMessage.Tone.POSITIVE : WorldMessage.Tone.NEUTRAL);
        updateEndConditions();
        return true;
    }

    /** Selects the inventory slot at {@code index} (0-based). */
    public void selectItem(int index) {
        player.getInventory().select(index);
    }

    public void selectNextItem() {
        player.getInventory().selectNext();
    }

    public void selectPreviousItem() {
        player.getInventory().selectPrevious();
    }

    // ------------------------------------------------------------------------------------------
    // Operations used by hidden events
    // ------------------------------------------------------------------------------------------

    /**
     * Adds a goose at (x, y) with random hunger. This ignores the active-goose limit; hidden events
     * use it on purpose.
     *
     * @return the new goose
     */
    public Goose spawnGooseAt(float x, float y) {
        int hungerRange = GameConfig.GOOSE_MAX_HUNGER - GameConfig.GOOSE_MIN_HUNGER + 1;
        int hunger = GameConfig.GOOSE_MIN_HUNGER + random.nextInt(hungerRange);
        Goose goose = new Goose(x, y, hunger);
        geese.add(goose);
        return goose;
    }

    /** Shows a feedback message on the HUD for {@link GameConfig#MESSAGE_SECONDS}. */
    public void showMessage(String text, WorldMessage.Tone tone) {
        message = new WorldMessage(text, tone);
        messageSecondsLeft = GameConfig.MESSAGE_SECONDS;
    }

    // ------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------

    /** Spawns a goose at a random spawn point, preferring points far enough from the player. */
    private void spawnGooseAtSpawnPoint() {
        List<Vector2> points = map.getGooseSpawnPoints();
        List<Vector2> farEnough = new ArrayList<>();
        for (Vector2 point : points) {
            if (player.distanceTo(point.x, point.y) >= GameConfig.MIN_SPAWN_DISTANCE_FROM_PLAYER) {
                farEnough.add(point);
            }
        }
        List<Vector2> candidates = farEnough.isEmpty() ? points : farEnough;
        Vector2 point = candidates.get(random.nextInt(candidates.size()));
        spawnGooseAt(point.x, point.y);
    }

    /** Removes dispersed geese from the map and counts them. [FR_GEESE_DISPERSE] */
    private int removeDispersedGeese() {
        int removed = 0;
        Iterator<Goose> iterator = geese.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isDispersed()) {
                iterator.remove();
                removed++;
            }
        }
        stats.recordGeeseDispersed(removed);
        return removed;
    }

    private static String describeItemUse(Item item, int affected, int dispersed) {
        if (affected == 0) {
            return item.getName() + " wasted: no geese in range!";
        }
        String text = item.getName() + " " + item.getEffectVerb() + " " + affected
            + (affected == 1 ? " goose" : " geese");
        if (dispersed > 0) {
            text += " - " + dispersed + " flew away!";
        }
        return text;
    }

    // ------------------------------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------------------------------

    /** @return a snapshot of the session for the end screen */
    public GameSummary createSummary() {
        return new GameSummary(state == GameState.WON, timer.getElapsedSeconds(), stats.getItemsUsed(),
            stats.getEventsTriggered(), stats.getGeeseDispersed(), geeseToDisperseForWin);
    }

    public GameState getState() {
        return state;
    }

    public boolean isFinished() {
        return state.isFinished();
    }

    /** @return {@code true} once the player has unpaused the game for the first time */
    public boolean hasStarted() {
        return started;
    }

    public GameTimer getTimer() {
        return timer;
    }

    public GameStats getStats() {
        return stats;
    }

    public GameMap getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }

    /** @return read-only view of the geese currently on the map */
    public List<Goose> getGeese() {
        return Collections.unmodifiableList(geese);
    }

    /** @return read-only view of all hidden events, triggered or not */
    public List<HiddenEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /** @return the current feedback message, or {@code null} if there isn't one */
    public WorldMessage getMessage() {
        return message;
    }

    public int getGeeseToDisperseForWin() {
        return geeseToDisperseForWin;
    }
}
