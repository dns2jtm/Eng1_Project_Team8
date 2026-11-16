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
 * Holds and updates all state for a single game session: map, player, geese,
 * hidden events, timer and counters.
 *
 * <p>Kept independent of {@code Gdx.graphics} and {@code Gdx.input} so gameplay
 * rules can be tested directly with JUnit.</p>
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
     * Creates a session on the default campus map with four hidden events
     * (two positive and two negative). [FR_EVENT_COUNT, FR_EVENT_EFFECT]
     */
    public static GameWorld createDefault(Random random) {
        List<HiddenEvent> events = List.of(
            new PicnicBasketEvent(1200f, 400f),
            new GooseNestEvent(790f, 380f),
            new LuckyShortcutEvent(620f, 538f),
            new SlipperyPathEvent(420f, 300f)
        );
        return new GameWorld(GameMap.createCampus(), events, GameConfig.INITIAL_GEESE,
            GameConfig.GEESE_TO_DISPERSE_FOR_WIN, random);
    }

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

    /**
     * Advances the world by {@code delta} seconds of real frame time when running.
     * The countdown ticks once by the real frame delta [QR_TIMER_ACCURACY], while
     * movement and collisions step in small chunks so fast movement cannot skip obstacles
     * or hidden events.
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

    /** Pushes overlapping geese apart so they crowd around the player rather than stacking on one spot. */
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

    private void updateSpawning(float delta) {
        secondsUntilNextSpawn -= delta;
        while (secondsUntilNextSpawn <= 0f) {
            secondsUntilNextSpawn += GameConfig.GOOSE_SPAWN_INTERVAL_SECONDS;
            if (geese.size() < GameConfig.MAX_ACTIVE_GEESE) {
                spawnGooseAtSpawnPoint();
            }
        }
    }

    private void updateEndConditions() {
        if (stats.getGeeseDispersed() >= geeseToDisperseForWin) {
            state = GameState.WON;
        } else if (timer.isExpired()) {
            state = GameState.LOST;
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

    /** Switches between PAUSED and RUNNING; ignored after the session has ended. [FR_PAUSE] */
    public void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            started = true;
        }
    }

    /** Pauses a running session (used when the window is minimised or loses focus). */
    public void pause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        }
    }

    public void setPlayerDirection(float dirX, float dirY) {
        playerDirection.set(dirX, dirY);
    }

    /**
     * Uses the selected item on all geese within range, removes it from the inventory
     * and updates the counters. [FR_USE_ITEM, FR_ITEM_COUNTER, FR_GEESE_DISPERSE]
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

    public void selectItem(int index) {
        player.getInventory().select(index);
    }

    public void selectNextItem() {
        player.getInventory().selectNext();
    }

    public void selectPreviousItem() {
        player.getInventory().selectPrevious();
    }

    /** Spawns a goose at (x, y) with random starting hunger (used by hidden events as well). */
    public Goose spawnGooseAt(float x, float y) {
        int hungerRange = GameConfig.GOOSE_MAX_HUNGER - GameConfig.GOOSE_MIN_HUNGER + 1;
        int hunger = GameConfig.GOOSE_MIN_HUNGER + random.nextInt(hungerRange);
        Goose goose = new Goose(x, y, hunger);
        geese.add(goose);
        return goose;
    }

    public void showMessage(String text, WorldMessage.Tone tone) {
        message = new WorldMessage(text, tone);
        messageSecondsLeft = GameConfig.MESSAGE_SECONDS;
    }

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
            text += " (" + dispersed + " flew away!)";
        }
        return text;
    }

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

    public List<Goose> getGeese() {
        return Collections.unmodifiableList(geese);
    }

    public List<HiddenEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public WorldMessage getMessage() {
        return message;
    }

    public int getGeeseToDisperseForWin() {
        return geeseToDisperseForWin;
    }
}
