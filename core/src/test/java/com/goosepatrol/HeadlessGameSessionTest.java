package com.goosepatrol;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.goosepatrol.logic.GameState;
import com.goosepatrol.logic.GameSummary;
import com.goosepatrol.logic.GameWorld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration smoke test. Boots a real libGDX application with the headless backend (no window or
 * GPU) and plays complete sessions on the libGDX application thread, as the real game loop would.
 */
class HeadlessGameSessionTest {

    /** Simulated frame rate for the scripted sessions. */
    private static final float FRAME = 1f / 60f;

    private static HeadlessApplication application;

    @BeforeAll
    static void startHeadlessApplication() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 60;
        application = new HeadlessApplication(new ApplicationAdapter() { }, config);
    }

    @AfterAll
    static void stopHeadlessApplication() {
        application.exit();
    }

    /** Runs {@code task} on the libGDX application thread and waits for its result. */
    private static <T> T onGdxThread(java.util.function.Supplier<T> task) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Gdx.app.postRunnable(() -> {
            try {
                result.complete(task.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("FR_TIMER_EXPIRE: an idle player loses after exactly 5 minutes of play")
    void idleSessionEndsInLossAfterFiveMinutes() throws Exception {
        assertNotNull(Gdx.app, "headless libGDX application should be running");
        GameSummary summary = onGdxThread(() -> {
            GameWorld world = GameWorld.createDefault(new Random(2024));
            world.togglePause();
            int frames = 0;
            while (!world.isFinished()) {
                world.update(FRAME);
                frames++;
            }
            assertEquals(GameState.LOST, world.getState());
            // 18000 frames at 60 FPS; allow half a second for float rounding over 18000 subtractions.
            assertEquals(5 * 60 * 60, frames, 30, "about 18000 frames at 60 FPS");
            return world.createSummary();
        });
        assertEquals(false, summary.won());
        assertEquals(GameConfig.GAME_DURATION_SECONDS, summary.elapsedSeconds(), 0.05f);
        assertEquals(0, summary.itemsUsed());
    }

    @Test
    @DisplayName("QR_STABILITY: scripted player wanders campus, pauses and uses items. Session completes without errors")
    void scriptedSessionRunsToCompletion() throws Exception {
        GameWorld finished = onGdxThread(() -> {
            GameWorld world = GameWorld.createDefault(new Random(99));
            world.togglePause();
            Random input = new Random(5);
            float dirX = 1f;
            float dirY = 0f;
            int frame = 0;
            while (!world.isFinished()) {
                if (frame % 90 == 0) {
                    dirX = input.nextInt(3) - 1;
                    dirY = input.nextInt(3) - 1;
                }
                world.setPlayerDirection(dirX, dirY);
                if (frame % 600 == 300) {
                    world.useSelectedItem();
                }
                if (frame == 1000) {
                    world.togglePause();
                    world.update(30f); // ignored: paused
                    world.togglePause();
                }
                world.update(FRAME);
                frame++;
            }
            return world;
        });
        GameSummary summary = finished.createSummary();
        assertTrue(summary.itemsUsed() > 0);
        assertTrue(summary.elapsedSeconds() <= GameConfig.GAME_DURATION_SECONDS + 1e-3f);
        assertTrue(summary.won() || finished.getTimer().isExpired());
    }

    @Test
    @DisplayName("QR_STABILITY: three consecutive full 5-minute sessions run to completion with zero crashes")
    void threeConsecutiveFullSessionsRunWithoutError() throws Exception {
        int completedSessions = onGdxThread(() -> {
            int completed = 0;
            for (int session = 0; session < 3; session++) {
                GameWorld world = GameWorld.createDefault(new Random(500L + session));
                world.togglePause();
                Random input = new Random(100L + session);
                float dirX = 0f;
                float dirY = 1f;
                int frame = 0;
                while (!world.isFinished()) {
                    if (frame % 120 == 0) {
                        dirX = input.nextInt(3) - 1;
                        dirY = input.nextInt(3) - 1;
                    }
                    world.setPlayerDirection(dirX, dirY);
                    if (frame == 2400) {
                        // Select Whistle (slot 4) and use it once so session plays full 5 minutes without dispersing 8 geese
                        world.selectItem(4);
                        world.useSelectedItem();
                    }
                    world.update(FRAME);
                    frame++;
                }
                GameSummary summary = world.createSummary();
                assertTrue(summary.elapsedSeconds() <= GameConfig.GAME_DURATION_SECONDS + 1e-3f);
                assertTrue(summary.won() || world.getTimer().isExpired());
                completed++;
            }
            return completed;
        });
        assertEquals(3, completedSessions);
    }
}
