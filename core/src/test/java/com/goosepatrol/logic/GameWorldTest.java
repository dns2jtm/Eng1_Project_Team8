package com.goosepatrol.logic;

import com.goosepatrol.GameConfig;
import com.goosepatrol.TestMaps;
import com.goosepatrol.entity.Goose;
import com.goosepatrol.entity.Player;
import com.goosepatrol.event.GooseNestEvent;
import com.goosepatrol.event.HiddenEvent;
import com.goosepatrol.event.PicnicBasketEvent;
import com.goosepatrol.event.SlipperyPathEvent;
import com.goosepatrol.item.FoodItem;
import com.goosepatrol.item.Inventory;
import com.goosepatrol.item.Item;
import com.goosepatrol.item.ScareItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link GameWorld}, covering the game state machine (3-state-game.puml) and the
 * use-item and per-frame sequence diagrams (5-seq-use-item.puml, 6-seq-update.puml).
 */
class GameWorldTest {

    /** World on an empty map with no geese or events, needing {@code toWin} dispersed geese to win. */
    private static GameWorld emptyWorld(int toWin) {
        return new GameWorld(TestMaps.empty(), List.of(), 0, toWin, new Random(42));
    }

    private static GameWorld runningWorld() {
        GameWorld world = emptyWorld(100);
        world.togglePause();
        return world;
    }

    // --- Game state: start paused / pause / resume ---------------------------------------------

    @Test
    @DisplayName("FR_START_PAUSED: a new game starts paused and hasn't started")
    void startsPaused() {
        GameWorld world = GameWorld.createDefault(new Random(1));
        assertEquals(GameState.PAUSED, world.getState());
        assertFalse(world.hasStarted());
        assertFalse(world.isFinished());
    }

    @Test
    @DisplayName("FR_PAUSE: nothing updates while paused")
    void nothingUpdatesWhilePaused() {
        GameWorld world = GameWorld.createDefault(new Random(1));
        Goose goose = world.getGeese().get(0);
        float gooseX = goose.getX();
        float playerX = world.getPlayer().getX();
        world.setPlayerDirection(1f, 0f);
        world.update(10f);
        assertEquals(GameConfig.GAME_DURATION_SECONDS, world.getTimer().getRemainingSeconds());
        assertEquals(gooseX, goose.getX());
        assertEquals(playerX, world.getPlayer().getX());
    }

    @Test
    @DisplayName("FR_PAUSE: the game can be paused and resumed at any time while running")
    void togglePauseSwitchesBetweenPausedAndRunning() {
        GameWorld world = emptyWorld(1);
        world.togglePause();
        assertEquals(GameState.RUNNING, world.getState());
        assertTrue(world.hasStarted());
        world.update(1f);
        world.togglePause();
        assertEquals(GameState.PAUSED, world.getState());
        world.update(5f);
        assertEquals(GameConfig.GAME_DURATION_SECONDS - 1f, world.getTimer().getRemainingSeconds(), 1e-4f);
        assertTrue(world.hasStarted(), "started stays true after re-pausing");
    }

    @Test
    void pauseOnlyAffectsARunningGame() {
        GameWorld world = emptyWorld(1);
        world.pause();
        assertEquals(GameState.PAUSED, world.getState());
        world.togglePause();
        world.pause();
        assertEquals(GameState.PAUSED, world.getState());
    }

    // --- Timer and losing ----------------------------------------------------------------------

    @Test
    @DisplayName("FR_TIMER_EXPIRE: the game is lost when the 5-minute timer runs out")
    void losesWhenTimerExpires() {
        GameWorld world = runningWorld();
        world.update(GameConfig.GAME_DURATION_SECONDS - 0.5f);
        assertEquals(GameState.RUNNING, world.getState());
        world.update(1f);
        assertEquals(GameState.LOST, world.getState());
        assertTrue(world.isFinished());
        assertTrue(world.getTimer().isExpired());
    }

    @Test
    void finishedGameIgnoresPauseAndCommands() {
        GameWorld world = runningWorld();
        world.update(GameConfig.GAME_DURATION_SECONDS);
        world.togglePause();
        assertEquals(GameState.LOST, world.getState());
        assertFalse(world.useSelectedItem());
    }

    @Test
    void rejectsNegativeDelta() {
        assertThrows(IllegalArgumentException.class, () -> runningWorld().update(-0.1f));
    }

    // --- Player movement -----------------------------------------------------------------------

    @Test
    @DisplayName("FR_PLAYER_MOVE: the player moves in the input direction while the game is running")
    void playerMovesOnlyWhileRunning() {
        GameWorld world = runningWorld();
        Player player = world.getPlayer();
        float startX = player.getX();
        world.setPlayerDirection(1f, 0f);
        world.update(0.5f);
        assertEquals(startX + GameConfig.PLAYER_SPEED * 0.5f, player.getX(), 1e-3f);
    }

    @Test
    @DisplayName("FR_GEESE_BLOCK: geese touching the player slow them down")
    void playerIsSlowedWhileMobbedByAChasingGoose() {
        GameWorld world = runningWorld();
        Player player = world.getPlayer();
        world.spawnGooseAt(player.getX(), player.getY() - (GameConfig.PLAYER_RADIUS + GameConfig.GOOSE_RADIUS));
        world.update(0.01f);
        assertTrue(player.isMobbed());
    }

    // --- Starting inventory and using items ----------------------------------------------------

    @Test
    @DisplayName("FR_INITIAL_INVENTORY: the starting inventory holds two kinds of item")
    void startingInventoryHasTwoItemTypes() {
        Inventory inventory = emptyWorld(1).getPlayer().getInventory();
        assertEquals(GameWorld.STARTING_BREAD + GameWorld.STARTING_WHISTLES, inventory.size());
        long food = inventory.getItems().stream().filter(i -> i instanceof FoodItem).count();
        long scare = inventory.getItems().stream().filter(i -> i instanceof ScareItem).count();
        assertEquals(GameWorld.STARTING_BREAD, food);
        assertEquals(GameWorld.STARTING_WHISTLES, scare);
    }

    @Test
    @DisplayName("FR_USE_ITEM / FR_ITEM_COUNTER: using an item affects geese in range, removes it and counts it")
    void usingItemAffectsOnlyGeeseInRange() {
        GameWorld world = runningWorld();
        Player player = world.getPlayer();
        Item bread = player.getInventory().selected();
        Goose near = world.spawnGooseAt(player.getX() + bread.getRange() - 1f, player.getY());
        Goose far = world.spawnGooseAt(player.getX() + bread.getRange() + 50f, player.getY());
        int hungerNear = near.getHunger();
        int hungerFar = far.getHunger();
        int itemsBefore = player.getInventory().size();

        assertTrue(world.useSelectedItem());

        assertEquals(itemsBefore - 1, player.getInventory().size());
        assertEquals(1, world.getStats().getItemsUsed());
        assertEquals(hungerFar, far.getHunger());
        assertTrue(near.isDispersed() || near.getHunger() == hungerNear - 1);
        assertNotNull(world.getMessage());
    }

    @Test
    @DisplayName("FR_GEESE_DISPERSE: fully fed geese are removed and counted")
    void dispersedGeeseAreRemovedAndCounted() {
        GameWorld world = runningWorld();
        Player player = world.getPlayer();
        Goose goose = world.spawnGooseAt(player.getX() + 20f, player.getY());
        while (!goose.isDispersed()) {
            world.selectItem(0); // bread is first in the hotbar
            assertTrue(world.useSelectedItem());
        }
        assertFalse(world.getGeese().contains(goose));
        assertEquals(1, world.getStats().getGeeseDispersed());
        assertTrue(world.getMessage().text().contains("flew away"));
        assertEquals(WorldMessage.Tone.POSITIVE, world.getMessage().tone());
    }

    @Test
    void usingItemWithNoGeeseInRangeStillUsesItUp() {
        GameWorld world = runningWorld();
        int before = world.getPlayer().getInventory().size();
        assertTrue(world.useSelectedItem());
        assertEquals(before - 1, world.getPlayer().getInventory().size());
        assertEquals(1, world.getStats().getItemsUsed());
        assertTrue(world.getMessage().text().contains("wasted"));
        assertEquals(WorldMessage.Tone.NEUTRAL, world.getMessage().tone());
    }

    @Test
    void cannotUseItemsWhilePausedOrWithEmptyInventory() {
        GameWorld world = emptyWorld(1);
        assertFalse(world.useSelectedItem(), "paused");
        world.togglePause();
        int total = world.getPlayer().getInventory().size();
        for (int i = 0; i < total; i++) {
            assertTrue(world.useSelectedItem());
        }
        assertFalse(world.useSelectedItem(), "empty");
        assertEquals(total, world.getStats().getItemsUsed());
    }

    @Test
    void itemSelectionIsForwardedToInventory() {
        GameWorld world = emptyWorld(1);
        Inventory inventory = world.getPlayer().getInventory();
        world.selectItem(GameWorld.STARTING_BREAD);
        assertTrue(inventory.selected() instanceof ScareItem);
        world.selectNextItem();
        assertEquals(GameWorld.STARTING_BREAD + 1, inventory.getSelectedIndex());
        world.selectPreviousItem();
        world.selectPreviousItem();
        assertTrue(inventory.selected() instanceof FoodItem);
    }

    @Test
    void feedbackMessageExpires() {
        GameWorld world = runningWorld();
        world.useSelectedItem();
        assertNotNull(world.getMessage());
        world.update(GameConfig.MESSAGE_SECONDS + 0.1f);
        assertNull(world.getMessage());
    }

    // --- Winning ---------------------------------------------------------------------------------

    @Test
    @DisplayName("FR_WIN_CONDITION: the game is won once enough geese are dispersed")
    void winsWhenEnoughGeeseDispersed() {
        GameWorld world = emptyWorld(2);
        world.togglePause();
        Player player = world.getPlayer();
        Goose a = world.spawnGooseAt(player.getX() + 20f, player.getY());
        Goose b = world.spawnGooseAt(player.getX() - 20f, player.getY());
        while (!(a.isDispersed() && b.isDispersed())) {
            world.selectItem(0);
            world.useSelectedItem();
        }
        assertEquals(GameState.WON, world.getState());
        GameSummary summary = world.createSummary();
        assertTrue(summary.won());
        assertEquals(2, summary.geeseDispersed());
        assertEquals(2, summary.geeseToWin());
    }

    // --- Hidden events ---------------------------------------------------------------------------

    @Test
    @DisplayName("FR_EVENT_TRIGGER / FR_EVENT_COUNTER: walking into a hidden event triggers it once and counts it")
    void hiddenEventsTriggerOnceAndAreCounted() {
        PicnicBasketEvent picnic = new PicnicBasketEvent(560f, 500f);
        GameWorld world = new GameWorld(TestMaps.empty(), List.of(picnic), 0, 100, new Random(3));
        world.togglePause();
        int itemsBefore = world.getPlayer().getInventory().size();
        world.setPlayerDirection(1f, 0f);
        for (int i = 0; i < 20; i++) {
            world.update(0.05f);
        }
        assertTrue(picnic.isTriggered());
        assertEquals(1, world.getStats().getEventsTriggered());
        assertEquals(itemsBefore + PicnicBasketEvent.BREAD_REWARD, world.getPlayer().getInventory().size());
        assertEquals(WorldMessage.Tone.POSITIVE, world.getMessage().tone());
    }

    @Test
    void negativeEventSpawnsGeeseAndShowsWarning() {
        HiddenEvent nest = new GooseNestEvent(500f, 500f);
        GameWorld world = new GameWorld(TestMaps.empty(), List.of(nest), 0, 100, new Random(3));
        world.togglePause();
        world.update(0.01f);
        assertEquals(GooseNestEvent.GEESE_SPAWNED, world.getGeese().size());
        assertEquals(WorldMessage.Tone.NEGATIVE, world.getMessage().tone());
        assertEquals(1, world.getEvents().size());
    }

    @Test
    @DisplayName("FR_EVENT_COUNT: the default map has at least 2 hidden events, positive and negative")
    void defaultWorldHasHiddenEventsThatStartUntriggered() {
        GameWorld world = GameWorld.createDefault(new Random(1));
        assertEquals(4, world.getEvents().size());
        world.getEvents().forEach(e -> assertFalse(e.isTriggered()));
        assertTrue(world.getEvents().stream().anyMatch(HiddenEvent::isPositive));
        assertTrue(world.getEvents().stream().anyMatch(e -> !e.isPositive()));
    }

    @Test
    @DisplayName("FR_EVENT_TRIGGER: a long frame is simulated in small steps, so fast movement can't skip an event")
    void longFrameDoesNotSkipHiddenEvents() {
        PicnicBasketEvent picnic = new PicnicBasketEvent(560f, 500f);
        GameWorld world = new GameWorld(TestMaps.empty(), List.of(picnic), 0, 100, new Random(3));
        world.togglePause();
        world.setPlayerDirection(1f, 0f);
        world.update(1f); // one 1-second frame: the player passes straight through the event
        assertTrue(world.getPlayer().getX() > 560f + picnic.getRadius() + GameConfig.PLAYER_RADIUS);
        assertTrue(picnic.isTriggered());
    }

    @Test
    @DisplayName("QR_TIMER_ACCURACY: a slow frame still advances the timer by the full real time")
    void slowFrameAdvancesTimerByFullRealTime() {
        GameWorld world = runningWorld();
        world.update(0.75f);
        assertEquals(0.75f, world.getTimer().getElapsedSeconds(), 1e-4f);
        assertEquals(GameConfig.GAME_DURATION_SECONDS - 0.75f, world.getTimer().getRemainingSeconds(), 1e-4f);
    }

    @Test
    void slipperyPathEndsTheGameEarlyButReportsRealPlayTime() {
        SlipperyPathEvent slip = new SlipperyPathEvent(500f, 500f);
        GameWorld world = new GameWorld(TestMaps.empty(), List.of(slip), 0, 100, new Random(3));
        world.togglePause();
        world.update(0.1f); // the player starts on the event, so it fires in the first frame
        assertEquals(GameConfig.GAME_DURATION_SECONDS - SlipperyPathEvent.TIME_PENALTY_SECONDS - 0.1f,
            world.getTimer().getRemainingSeconds(), 1e-3f);
        world.update(GameConfig.GAME_DURATION_SECONDS);
        assertEquals(GameState.LOST, world.getState());
        assertEquals(GameConfig.GAME_DURATION_SECONDS - SlipperyPathEvent.TIME_PENALTY_SECONDS,
            world.createSummary().elapsedSeconds(), 0.06f);
    }

    // --- Spawning ------------------------------------------------------------------------------

    @Test
    @DisplayName("FR_GEESE_SPAWN: geese spawn at start, then periodically up to the limit")
    void geeseSpawnPeriodicallyUpToLimit() {
        GameWorld world = GameWorld.createDefault(new Random(7));
        assertEquals(GameConfig.INITIAL_GEESE, world.getGeese().size());
        world.togglePause();
        world.update(GameConfig.GOOSE_SPAWN_INTERVAL_SECONDS + 0.01f);
        assertEquals(GameConfig.INITIAL_GEESE + 1, world.getGeese().size());
        for (int i = 0; i < 20 && !world.isFinished(); i++) {
            world.update(GameConfig.GOOSE_SPAWN_INTERVAL_SECONDS);
        }
        assertTrue(world.getGeese().size() <= GameConfig.MAX_ACTIVE_GEESE);
    }

    @Test
    void geeseSpawnAwayFromThePlayer() {
        GameWorld world = GameWorld.createDefault(new Random(11));
        Player player = world.getPlayer();
        world.getGeese().forEach(goose ->
            assertTrue(goose.distanceTo(player) >= GameConfig.MIN_SPAWN_DISTANCE_FROM_PLAYER));
    }

    @Test
    void spawnedGeeseHaveHungerWithinConfiguredRange() {
        GameWorld world = emptyWorld(1);
        for (int i = 0; i < 50; i++) {
            int hunger = world.spawnGooseAt(10f + i, 10f).getHunger();
            assertTrue(hunger >= GameConfig.GOOSE_MIN_HUNGER && hunger <= GameConfig.GOOSE_MAX_HUNGER);
        }
    }

    @Test
    void geeseDoNotStackOnTopOfEachOther() {
        GameWorld world = runningWorld();
        Goose a = world.spawnGooseAt(100f, 100f);
        Goose b = world.spawnGooseAt(100f, 100f);
        a.scare(5f);
        b.scare(5f);
        for (int i = 0; i < 30; i++) {
            world.update(0.02f);
        }
        assertTrue(a.distanceTo(b) >= 2f * GameConfig.GOOSE_RADIUS - 0.5f);
    }

    // --- Construction and read-only views ----------------------------------------------------

    @Test
    void rejectsInvalidConstruction() {
        assertThrows(IllegalArgumentException.class,
            () -> new GameWorld(TestMaps.empty(), List.of(), -1, 1, new Random()));
        assertThrows(IllegalArgumentException.class,
            () -> new GameWorld(TestMaps.empty(), List.of(), 0, 0, new Random()));
        assertThrows(NullPointerException.class,
            () -> new GameWorld(TestMaps.empty(), List.of(), 0, 1, null));
    }

    @Test
    void exposesReadOnlyCollections() {
        GameWorld world = emptyWorld(1);
        assertThrows(UnsupportedOperationException.class, () -> world.getGeese().clear());
        assertThrows(UnsupportedOperationException.class, () -> world.getEvents().clear());
        assertNotNull(world.getMap());
    }
}
