package com.goosepatrol.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.goosepatrol.GameConfig;
import com.goosepatrol.item.Inventory;
import com.goosepatrol.item.Item;
import com.goosepatrol.logic.GameState;
import com.goosepatrol.logic.GameStats;
import com.goosepatrol.logic.GameTimer;
import com.goosepatrol.logic.GameWorld;
import com.goosepatrol.logic.WorldMessage;

import java.util.List;

/**
 * Heads-up display drawn over the map: countdown timer, counters, inventory bar,
 * short feedback messages and the start/pause overlay.
 */
public class Hud {

    private static final float W = GameConfig.WORLD_WIDTH;
    private static final float H = GameConfig.WORLD_HEIGHT;
    private static final float TOP_BAR_HEIGHT = 40f;
    private static final float MAX_SLOT_WIDTH = 108f;
    private static final float SLOT_HEIGHT = 44f;
    private static final float SLOT_GAP = 8f;
    private static final float SLOT_Y = 12f;
    private static final float LOW_TIME_SECONDS = 60f;

    private static final Color PANEL = new Color(0.08f, 0.10f, 0.13f, 0.80f);
    private static final Color OVERLAY = new Color(0f, 0f, 0f, 0.58f);
    private static final Color DIALOG_BG = new Color(0.11f, 0.13f, 0.17f, 0.98f);
    private static final Color DIALOG_BORDER = new Color(0.35f, 0.40f, 0.48f, 1f);
    private static final Color SLOT = new Color(0.14f, 0.16f, 0.20f, 0.88f);
    private static final Color SLOT_SELECTED = new Color(0.98f, 0.80f, 0.24f, 1f);
    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_DIM = new Color(0.78f, 0.80f, 0.84f, 1f);
    private static final Color TEXT_WARNING = new Color(1f, 0.38f, 0.32f, 1f);
    private static final Color TEXT_GOOD = new Color(0.45f, 0.95f, 0.52f, 1f);
    private static final Color TEXT_TITLE = new Color(0.98f, 0.84f, 0.30f, 1f);

    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public Hud(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        this.shapes = shapes;
        this.batch = batch;
        this.font = font;
    }

    public void render(GameWorld world, Matrix4 projection) {
        boolean paused = world.getState() == GameState.PAUSED;
        WorldMessage message = world.getMessage();
        Inventory inventory = world.getPlayer().getInventory();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(PANEL);
        shapes.rect(0f, H - TOP_BAR_HEIGHT, W, TOP_BAR_HEIGHT);
        drawSlotBackgrounds(inventory);
        if (message != null && !paused) {
            layout.setText(font, message.text());
            shapes.setColor(PANEL);
            shapes.rect((W - layout.width) / 2f - 14f, H - 84f, layout.width + 28f, 30f);
        }
        if (paused) {
            shapes.setColor(OVERLAY);
            shapes.rect(0f, 0f, W, H);
            float cardW = 620f;
            float cardH = 365f;
            float cardX = (W - cardW) / 2f;
            float cardY = (H - cardH) / 2f + 6f;
            shapes.setColor(DIALOG_BORDER);
            shapes.rect(cardX - 2f, cardY - 2f, cardW + 4f, cardH + 4f);
            shapes.setColor(DIALOG_BG);
            shapes.rect(cardX, cardY, cardW, cardH);
        }
        shapes.end();

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawTopBar(world);
        drawSlotLabels(inventory);
        if (message != null && !paused) {
            drawCentred(message.text(), H - 64f, 1f, colourFor(message.tone()));
        }
        if (paused) {
            drawPauseOverlay(world.hasStarted());
        }
        batch.end();
    }

    private void drawTopBar(GameWorld world) {
        GameTimer timer = world.getTimer();
        GameStats stats = world.getStats();
        float textY = H - 13f;

        font.getData().setScale(1.25f);
        font.setColor(timer.getRemainingSeconds() <= LOW_TIME_SECONDS ? TEXT_WARNING : TEXT);
        font.draw(batch, "Time left: " + GameTimer.format(timer.getRemainingSeconds()), 18f, textY);

        font.setColor(TEXT);
        String counters = "Items used: " + stats.getItemsUsed() + "     Events triggered: " + stats.getEventsTriggered();
        layout.setText(font, counters);
        font.draw(batch, layout, (W - layout.width) / 2f, textY);

        String dispersed = "Geese dispersed: " + stats.getGeeseDispersed() + " / " + world.getGeeseToDisperseForWin();
        layout.setText(font, dispersed);
        font.draw(batch, layout, W - layout.width - 18f, textY);
        font.getData().setScale(1f);
        font.setColor(TEXT);
    }

    private static float slotWidth(int count) {
        if (count <= 0) {
            return MAX_SLOT_WIDTH;
        }
        return Math.min(MAX_SLOT_WIDTH, (W - 64f - (count - 1) * SLOT_GAP) / count);
    }

    private static float slotsStartX(int count, float width) {
        float total = count * width + (count - 1) * SLOT_GAP;
        return (W - total) / 2f;
    }

    private void drawSlotBackgrounds(Inventory inventory) {
        List<Item> items = inventory.getItems();
        if (items.isEmpty()) {
            return;
        }
        float width = slotWidth(items.size());
        float x = slotsStartX(items.size(), width);
        for (int i = 0; i < items.size(); i++) {
            float slotX = x + i * (width + SLOT_GAP);
            if (i == inventory.getSelectedIndex()) {
                shapes.setColor(SLOT_SELECTED);
                shapes.rect(slotX - 3f, SLOT_Y - 3f, width + 6f, SLOT_HEIGHT + 6f);
            }
            shapes.setColor(SLOT);
            shapes.rect(slotX, SLOT_Y, width, SLOT_HEIGHT);
        }
    }

    private void drawSlotLabels(Inventory inventory) {
        List<Item> items = inventory.getItems();
        if (items.isEmpty()) {
            drawCentred("Inventory empty", SLOT_Y + 28f, 1f, TEXT_DIM);
            return;
        }
        float width = slotWidth(items.size());
        float x = slotsStartX(items.size(), width);
        for (int i = 0; i < items.size(); i++) {
            float slotX = x + i * (width + SLOT_GAP);
            font.setColor(TEXT_DIM);
            if (i < 9) {
                font.draw(batch, String.valueOf(i + 1), slotX + 6f, SLOT_Y + SLOT_HEIGHT - 5f);
            }
            font.setColor(TEXT);
            layout.setText(font, items.get(i).getName());
            font.draw(batch, layout, slotX + (width - layout.width) / 2f, SLOT_Y + 20f);
        }
        Item selected = inventory.selected();
        if (selected != null) {
            drawCentred(selected.getDescription() + "   [SPACE to use]", SLOT_Y + SLOT_HEIGHT + 21f, 1f, TEXT);
        }
    }

    private void drawPauseOverlay(boolean started) {
        drawCentred("GOOSE PATROL", H * 0.69f, 2.2f, TEXT_TITLE);
        drawCentred(started ? "Game Paused" : "Escape the hungry campus geese!", H * 0.61f, 1.3f, TEXT);
        String[] lines = {
            "Disperse " + GameConfig.GEESE_TO_DISPERSE_FOR_WIN + " geese before the 5-minute timer runs out.",
            "Feed geese Bread until full (orange dots show hunger) so they fly away.",
            "Explore campus paths to find hidden surprises.",
            "",
            "WASD / Arrow keys : Move          SPACE : Use selected item",
            "1-9 / Q / E : Select item         P / ESC : Pause or resume"
        };
        float y = H * 0.53f;
        for (String line : lines) {
            drawCentred(line, y, 1.05f, TEXT_DIM);
            y -= 25f;
        }
        drawCentred(started ? "Press P or ENTER to resume" : "Press ENTER or P to start", H * 0.30f, 1.35f, TEXT_TITLE);
    }

    private void drawCentred(String text, float y, float scale, Color colour) {
        font.getData().setScale(scale);
        font.setColor(colour);
        layout.setText(font, text);
        font.draw(batch, layout, (W - layout.width) / 2f, y);
        font.getData().setScale(1f);
        font.setColor(TEXT);
    }

    private static Color colourFor(WorldMessage.Tone tone) {
        return switch (tone) {
            case POSITIVE -> TEXT_GOOD;
            case NEGATIVE -> TEXT_WARNING;
            case NEUTRAL -> TEXT;
        };
    }
}
