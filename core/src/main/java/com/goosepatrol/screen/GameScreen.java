package com.goosepatrol.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.goosepatrol.GameConfig;
import com.goosepatrol.GoosePatrolGame;
import com.goosepatrol.logic.GameState;
import com.goosepatrol.logic.GameWorld;

/**
 * Main gameplay screen. Reads keyboard input, updates {@link GameWorld} once per frame
 * and draws the campus and HUD.
 */
public class GameScreen extends ScreenAdapter {

    private static final int[] SLOT_KEYS = {
        Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4, Input.Keys.NUM_5,
        Input.Keys.NUM_6, Input.Keys.NUM_7, Input.Keys.NUM_8, Input.Keys.NUM_9
    };

    private final GoosePatrolGame game;
    private final GameWorld world;
    private final Viewport viewport;
    private final WorldRenderer worldRenderer;
    private final Hud hud;

    public GameScreen(GoosePatrolGame game, GameWorld world) {
        this.game = game;
        this.world = world;
        this.viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        this.worldRenderer = new WorldRenderer(game.getShapes(), game.getBatch(), game.getFont());
        this.hud = new Hud(game.getShapes(), game.getBatch(), game.getFont());
    }

    @Override
    public void render(float delta) {
        handleInput();
        world.update(delta);

        ScreenUtils.clear(0.08f, 0.09f, 0.11f, 1f);
        viewport.apply();
        worldRenderer.render(world, viewport.getCamera().combined);
        hud.render(world, viewport.getCamera().combined);

        if (world.isFinished()) {
            game.showEndScreen(world.createSummary());
        }
    }

    private void handleInput() {
        boolean pausePressed = Gdx.input.isKeyJustPressed(Input.Keys.P)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
            || (world.getState() == GameState.PAUSED && Gdx.input.isKeyJustPressed(Input.Keys.ENTER));
        if (pausePressed) {
            world.togglePause();
            return;
        }
        if (world.getState() != GameState.RUNNING) {
            world.setPlayerDirection(0f, 0f);
            return;
        }

        world.setPlayerDirection(
            axis(Input.Keys.D, Input.Keys.RIGHT, Input.Keys.A, Input.Keys.LEFT),
            axis(Input.Keys.W, Input.Keys.UP, Input.Keys.S, Input.Keys.DOWN)
        );

        for (int i = 0; i < SLOT_KEYS.length; i++) {
            if (Gdx.input.isKeyJustPressed(SLOT_KEYS[i])) {
                world.selectItem(i);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            world.selectPreviousItem();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            world.selectNextItem();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            world.useSelectedItem();
        }
    }

    private static float axis(int positiveKey, int positiveAlt, int negativeKey, int negativeAlt) {
        float value = 0f;
        if (Gdx.input.isKeyPressed(positiveKey) || Gdx.input.isKeyPressed(positiveAlt)) {
            value += 1f;
        }
        if (Gdx.input.isKeyPressed(negativeKey) || Gdx.input.isKeyPressed(negativeAlt)) {
            value -= 1f;
        }
        return value;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        world.pause();
    }
}
