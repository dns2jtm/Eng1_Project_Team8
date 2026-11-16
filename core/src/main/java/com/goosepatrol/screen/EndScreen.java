package com.goosepatrol.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.goosepatrol.GameConfig;
import com.goosepatrol.GoosePatrolGame;
import com.goosepatrol.logic.GameSummary;
import com.goosepatrol.logic.GameTimer;

/**
 * End-of-game screen displaying win/loss status and session statistics.
 * Press R or ENTER to start a new game, or ESC to quit. [FR_END_SCREEN]
 */
public class EndScreen extends ScreenAdapter {

    private static final Color WIN = new Color(0.45f, 0.96f, 0.52f, 1f);
    private static final Color LOSE = new Color(1f, 0.40f, 0.35f, 1f);
    private static final Color PANEL_BG = new Color(0.12f, 0.14f, 0.18f, 0.95f);
    private static final Color PANEL_BORDER = new Color(0.32f, 0.37f, 0.45f, 1f);
    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_DIM = new Color(0.76f, 0.78f, 0.82f, 1f);

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.startNewGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        ScreenUtils.clear(0.08f, 0.10f, 0.13f, 1f);
        viewport.apply();

        float w = GameConfig.WORLD_WIDTH;
        float h = GameConfig.WORLD_HEIGHT;
        float cardW = 620f;
        float cardH = 360f;
        float cardX = (w - cardW) / 2f;
        float cardY = (h - cardH) / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        ShapeRenderer shapes = game.getShapes();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(PANEL_BORDER);
        shapes.rect(cardX - 2f, cardY - 2f, cardW + 4f, cardH + 4f);
        shapes.setColor(PANEL_BG);
        shapes.rect(cardX, cardY, cardW, cardH);
        shapes.end();

        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (summary.won()) {
            drawCentred(batch, "YOU ESCAPED!", h * 0.68f, 2.3f, WIN);
            drawCentred(batch, "The geese are fed and you can get on with your day.", h * 0.60f, 1.2f, TEXT);
        } else {
            drawCentred(batch, "TIME'S UP!", h * 0.68f, 2.3f, LOSE);
            drawCentred(batch, "The geese still have you surrounded on campus.", h * 0.60f, 1.2f, TEXT);
        }
        String[] stats = {
            "Time played: " + GameTimer.format(summary.elapsedSeconds()),
            "Geese dispersed: " + summary.geeseDispersed() + " / " + summary.geeseToWin(),
            "Items used: " + summary.itemsUsed(),
            "Hidden events triggered: " + summary.eventsTriggered()
        };
        float y = h * 0.51f;
        for (String line : stats) {
            drawCentred(batch, line, y, 1.25f, TEXT);
            y -= 32f;
        }
        drawCentred(batch, "Press R or ENTER to play again   |   ESC to quit", h * 0.29f, 1.2f, TEXT_DIM);
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
