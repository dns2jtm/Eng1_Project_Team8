package com.goosepatrol.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.goosepatrol.GameConfig;
import com.goosepatrol.GoosePatrolGame;
import com.goosepatrol.logic.GameSummary;
import com.goosepatrol.logic.GameTimer;

/**
 * Results screen shown when a session ends, showing the result and statistics. Press R to play
 * again or ESC to quit. [FR_END_SCREEN; the score part is A2]
 */
public class EndScreen extends ScreenAdapter {

    private static final Color WIN = new Color(0.45f, 1f, 0.5f, 1f);
    private static final Color LOSE = new Color(1f, 0.4f, 0.35f, 1f);
    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_DIM = new Color(0.75f, 0.75f, 0.78f, 1f);

    private final GoosePatrolGame game;
    private final GameSummary summary;
    private final Viewport viewport;
    private final GlyphLayout layout = new GlyphLayout();

    public EndScreen(GoosePatrolGame game, GameSummary summary) {
        this.game = game;
        this.summary = summary;
        this.viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.startNewGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        ScreenUtils.clear(0.08f, 0.10f, 0.12f, 1f);
        viewport.apply();
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        float h = GameConfig.WORLD_HEIGHT;
        if (summary.won()) {
            drawCentred(batch, "YOU ESCAPED!", h * 0.75f, 3.2f, WIN);
            drawCentred(batch, "The geese are fed and you can get on with your day.", h * 0.65f, 1.4f, TEXT);
        } else {
            drawCentred(batch, "TIME'S UP!", h * 0.75f, 3.2f, LOSE);
            drawCentred(batch, "The geese still have you surrounded in the middle of campus.", h * 0.65f, 1.4f, TEXT);
        }
        String[] stats = {
            "Time played: " + GameTimer.format(summary.elapsedSeconds()),
            "Geese dispersed: " + summary.geeseDispersed() + " / " + summary.geeseToWin(),
            "Items used: " + summary.itemsUsed(),
            "Hidden events triggered: " + summary.eventsTriggered()
        };
        float y = h * 0.50f;
        for (String line : stats) {
            drawCentred(batch, line, y, 1.4f, TEXT);
            y -= 34f;
        }
        drawCentred(batch, "Press R to play again   |   ESC to quit", h * 0.15f, 1.4f, TEXT_DIM);
        batch.end();
    }

    private void drawCentred(SpriteBatch batch, String text, float y, float scale, Color colour) {
        BitmapFont font = game.getFont();
        font.getData().setScale(scale);
        font.setColor(colour);
        layout.setText(font, text);
        font.draw(batch, layout, (GameConfig.WORLD_WIDTH - layout.width) / 2f, y);
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
