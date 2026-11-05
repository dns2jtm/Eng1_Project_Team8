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
 * Heads-up display drawn over the world: time remaining [FR_TIMER_EXPIRE], item and event counters
 * [FR_ITEM_COUNTER], geese dispersed, the inventory bar, feedback messages, and the pause overlay
 * [FR_START_PAUSED, FR_PAUSE]. Read-only: it never changes the world.
 */
public class Hud {

    private static final float W = GameConfig.WORLD_WIDTH;
    private static final float H = GameConfig.WORLD_HEIGHT;
    private static final float TOP_BAR_HEIGHT = 40f;
    private static final float SLOT_WIDTH = 108f;
    private static final float SLOT_HEIGHT = 46f;
    private static final float SLOT_GAP = 8f;
    private static final float SLOT_Y = 12f;
    /** Below this many seconds left, the timer turns red. */
    private static final float LOW_TIME_SECONDS = 60f;

    private static final Color PANEL = new Color(0f, 0f, 0f, 0.55f);
    private static final Color OVERLAY = new Color(0f, 0f, 0f, 0.65f);
    private static final Color SLOT = new Color(0.15f, 0.15f, 0.18f, 0.85f);
    private static final Color SLOT_SELECTED = new Color(1f, 0.82f, 0.25f, 1f);
    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_DIM = new Color(0.75f, 0.75f, 0.78f, 1f);
    private static final Color TEXT_WARNING = new Color(1f, 0.35f, 0.3f, 1f);
    private static final Color TEXT_GOOD = new Color(0.45f, 1f, 0.5f, 1f);
    private static final Color TEXT_TITLE = new Color(1f, 0.85f, 0.3f, 1f);

    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public Hud(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        this.shapes = shapes;
        this.batch = batch;
        this.font = font;
    }

    /** Draws the HUD using the given camera projection. */
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
        if (message != null) {
            layout.setText(font, message.text());
            shapes.setColor(PANEL);
            shapes.rect((W - layout.width) / 2f - 14f, H - 90f, layout.width + 28f, 32f);
        }
        if (paused) {
            shapes.setColor(OVERLAY);
            shapes.rect(0f, 0f, W, H);
        }
        shapes.end();

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawTopBar(world);
        drawSlotLabels(inventory);
        if (message != null) {
            drawCentred(message.text(), H - 68f, 1f, colourFor(message.tone()));
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

        font.getData().setScale(1.3f);
        font.setColor(timer.getRemainingSeconds() <= LOW_TIME_SECONDS ? TEXT_WARNING : TEXT);
        font.draw(batch, "Time left  " + GameTimer.format(timer.getRemainingSeconds()), 16f, textY);

        font.setColor(TEXT);
        String counters = "Items used: " + stats.getItemsUsed() + "     Events triggered: " + stats.getEventsTriggered();
        layout.setText(font, counters);
        font.draw(batch, layout, (W - layout.width) / 2f, textY);

        String dispersed = "Geese dispersed: " + stats.getGeeseDispersed() + " / " + world.getGeeseToDisperseForWin();
        layout.setText(font, dispersed);
        font.draw(batch, layout, W - layout.width - 16f, textY);
        font.getData().setScale(1f);
        font.setColor(TEXT);
    }

    private float slotsStartX(int count) {
        float total = count * SLOT_WIDTH + (count - 1) * SLOT_GAP;
        return (W - total) / 2f;
    }

    private void drawSlotBackgrounds(Inventory inventory) {
        List<Item> items = inventory.getItems();
        float x = slotsStartX(items.size());
        for (int i = 0; i < items.size(); i++) {
            float slotX = x + i * (SLOT_WIDTH + SLOT_GAP);
            if (i == inventory.getSelectedIndex()) {
                shapes.setColor(SLOT_SELECTED);
                shapes.rect(slotX - 3f, SLOT_Y - 3f, SLOT_WIDTH + 6f, SLOT_HEIGHT + 6f);
            }
            shapes.setColor(SLOT);
            shapes.rect(slotX, SLOT_Y, SLOT_WIDTH, SLOT_HEIGHT);
        }
    }

    private void drawSlotLabels(Inventory inventory) {
        List<Item> items = inventory.getItems();
        if (items.isEmpty()) {
            drawCentred("Inventory empty", SLOT_Y + 30f, 1f, TEXT_DIM);
            return;
        }
        float x = slotsStartX(items.size());
        for (int i = 0; i < items.size(); i++) {
            float slotX = x + i * (SLOT_WIDTH + SLOT_GAP);
            font.setColor(TEXT_DIM);
            if (i < 9) {
                font.draw(batch, String.valueOf(i + 1), slotX + 5f, SLOT_Y + SLOT_HEIGHT - 4f);
            }
            font.setColor(TEXT);
            layout.setText(font, items.get(i).getName());
            font.draw(batch, layout, slotX + (SLOT_WIDTH - layout.width) / 2f, SLOT_Y + 20f);
        }
        Item selected = inventory.selected();
        if (selected != null) {
            drawCentred(selected.getDescription() + "   [SPACE to use]", SLOT_Y + SLOT_HEIGHT + 22f, 1f, TEXT);
        }
    }

    private void drawPauseOverlay(boolean started) {
        drawCentred("GOOSE PATROL", H * 0.72f, 3.2f, TEXT_TITLE);
        drawCentred(started ? "Paused" : "You're surrounded by hungry geese!", H * 0.62f, 1.6f, TEXT);
        String[] lines = {
            "Disperse " + GameConfig.GEESE_TO_DISPERSE_FOR_WIN + " geese before the 5-minute timer runs out.",
            "Feed geese until they are full (orange dots show hunger) and they will fly away.",
            "Look out for hidden surprises around campus...",
            "",
            "WASD / Arrow keys - move          SPACE - use selected item",
            "1-9 / Q / E - choose item          P / ESC - pause / resume"
        };
        float y = H * 0.52f;
        for (String line : lines) {
            drawCentred(line, y, 1.15f, TEXT_DIM);
            y -= 26f;
        }
        drawCentred(started ? "Press P to resume" : "Press P to start", H * 0.18f, 1.8f, TEXT_TITLE);
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
