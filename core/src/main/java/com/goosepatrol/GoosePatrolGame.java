package com.goosepatrol;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.goosepatrol.logic.GameSummary;
import com.goosepatrol.logic.GameWorld;
import com.goosepatrol.screen.EndScreen;
import com.goosepatrol.screen.GameScreen;

import java.util.Random;

/**
 * Main libGDX game class. Creates the shared rendering objects and switches
 * between the gameplay screen and the end screen.
 */
public class GoosePatrolGame extends Game {

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);
        startNewGame();
    }

    /** Starts a new game session (begins paused). */
    public void startNewGame() {
        changeScreen(new GameScreen(this, GameWorld.createDefault(new Random())));
    }

    /** Switches to the end screen with the session summary. */
    public void showEndScreen(GameSummary summary) {
        changeScreen(new EndScreen(this, summary));
    }

    private void changeScreen(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) {
            previous.dispose();
        }
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapes() {
        return shapes;
    }

    public BitmapFont getFont() {
        return font;
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        if (current != null) {
            current.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}
