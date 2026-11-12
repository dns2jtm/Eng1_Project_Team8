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
 * The in-game screen. Reads keyboard input, passes commands to the {@link GameWorld}, advances it
 * once per frame and draws it using {@link WorldRenderer} and {@link Hud}.
 *
 * <p>Controls [FR_PLAYER_MOVE, FR_USE_ITEM, FR_PAUSE]: WASD / arrow keys to move, SPACE to use the selected item, 1-9 / Q / E to choose an
 * item, P / ESC to start, pause and resume.</p>
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
        world.update(delta); // GameWorld splits long frames into small steps itself

        ScreenUtils.clear(0.08f, 0.08f, 0.1f, 1f);
        viewport.apply();
        worldRenderer.render(world, viewport.getCamera().combined);
        hud.render(world, viewport.getCamera().combined);

        if (world.isFinished()) {
            game.showEndScreen(world.createSummary());
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            world.togglePause();
        }
        if (world.getState() != GameState.RUNNING) {
            world.setPlayerDirection(0f, 0f);
            return;
        }
        world.setPlayerDirection(axis(Input.Keys.D, Input.Keys.RIGHT, Input.Keys.A, Input.Keys.LEFT),
            axis(Input.Keys.W, Input.Keys.UP, Input.Keys.S, Input.Keys.DOWN));

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

    /** @return +1, -1 or 0 depending on which of the positive / negative keys are held */
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

    /** Called when the window is minimised or loses focus: pause automatically. [FR_PAUSE] */
    @Override
    public void pause() {
        world.pause();
    }
}
