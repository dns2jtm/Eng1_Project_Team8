package com.goosepatrol.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.goosepatrol.entity.GameMap;
import com.goosepatrol.entity.Goose;
import com.goosepatrol.entity.Obstacle;
import com.goosepatrol.entity.Player;
import com.goosepatrol.event.HiddenEvent;
import com.goosepatrol.item.Item;
import com.goosepatrol.logic.GameWorld;

/**
 * Draws the campus map and everything on it, using simple shapes so no external art assets are needed.
 * Read-only: it never changes the world.
 */
public class WorldRenderer {

    private static final Color GRASS = new Color(0.42f, 0.66f, 0.33f, 1f);
    private static final Color PATH = new Color(0.86f, 0.79f, 0.62f, 1f);
    private static final Color BUILDING_WALL = new Color(0.45f, 0.42f, 0.40f, 1f);
    private static final Color BUILDING_ROOF = new Color(0.62f, 0.58f, 0.55f, 1f);
    private static final Color WATER_EDGE = new Color(0.20f, 0.42f, 0.75f, 1f);
    private static final Color WATER = new Color(0.30f, 0.55f, 0.88f, 1f);
    private static final Color LABEL = new Color(0.12f, 0.12f, 0.14f, 1f);
    private static final Color SHADOW = new Color(0f, 0f, 0f, 0.22f);
    private static final Color RANGE = new Color(1f, 1f, 1f, 0.13f);
    private static final Color EVENT_POSITIVE = new Color(0.30f, 0.90f, 0.40f, 0.40f);
    private static final Color EVENT_NEGATIVE = new Color(0.95f, 0.30f, 0.25f, 0.40f);
    private static final Color GOOSE_BODY = new Color(0.97f, 0.97f, 0.95f, 1f);
    private static final Color GOOSE_SCARED = new Color(0.78f, 0.84f, 0.95f, 1f);
    private static final Color GOOSE_HEAD = new Color(0.15f, 0.15f, 0.15f, 1f);
    private static final Color GOOSE_BEAK = new Color(1f, 0.62f, 0.10f, 1f);
    private static final Color HUNGER_PIP = new Color(0.95f, 0.45f, 0.10f, 1f);
    private static final Color CRUMB = new Color(0.90f, 0.75f, 0.40f, 1f);
    private static final Color PLAYER_BODY = new Color(0.36f, 0.20f, 0.60f, 1f);
    private static final Color PLAYER_HEAD = new Color(0.98f, 0.82f, 0.68f, 1f);
    private static final Color PLAYER_MOBBED = new Color(1f, 0.15f, 0.15f, 0.45f);

    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public WorldRenderer(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        this.shapes = shapes;
        this.batch = batch;
        this.font = font;
    }

    /** Draws the world using the given camera projection. */
    public void render(GameWorld world, Matrix4 projection) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawGround(world.getMap());
        drawObstacles(world.getMap());
        drawTriggeredEvents(world);
        drawSelectedItemRange(world.getPlayer());
        for (Goose goose : world.getGeese()) {
            drawGoose(goose);
        }
        drawPlayer(world.getPlayer());
        shapes.end();

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawObstacleLabels(world.getMap());
        batch.end();
    }

    private void drawGround(GameMap map) {
        shapes.setColor(GRASS);
        shapes.rect(0f, 0f, map.getWidth(), map.getHeight());
        shapes.setColor(PATH);
        for (Rectangle path : map.getPaths()) {
            shapes.rect(path.x, path.y, path.width, path.height);
        }
    }

    private void drawObstacles(GameMap map) {
        for (Obstacle obstacle : map.getObstacles()) {
            Rectangle b = obstacle.getBounds();
            if (obstacle.getType() == Obstacle.Type.WATER) {
                shapes.setColor(WATER_EDGE);
                shapes.ellipse(b.x, b.y, b.width, b.height);
                shapes.setColor(WATER);
                shapes.ellipse(b.x + 8f, b.y + 8f, b.width - 16f, b.height - 16f);
            } else {
                shapes.setColor(SHADOW);
                shapes.rect(b.x + 6f, b.y - 6f, b.width, b.height);
                shapes.setColor(BUILDING_WALL);
                shapes.rect(b.x, b.y, b.width, b.height);
                shapes.setColor(BUILDING_ROOF);
                shapes.rect(b.x + 6f, b.y + 6f, b.width - 12f, b.height - 12f);
            }
        }
    }

    /** Hidden events can't be seen until triggered. Afterwards a coloured marker shows where they were. */
    private void drawTriggeredEvents(GameWorld world) {
        for (HiddenEvent event : world.getEvents()) {
            if (event.isTriggered()) {
                shapes.setColor(event.isPositive() ? EVENT_POSITIVE : EVENT_NEGATIVE);
                shapes.circle(event.getX(), event.getY(), event.getRadius(), 32);
            }
        }
    }

    /** Shows the range of the selected item, so the player can judge when to use it. */
    private void drawSelectedItemRange(Player player) {
        Item selected = player.getInventory().selected();
        if (selected != null) {
            shapes.setColor(RANGE);
            shapes.circle(player.getX(), player.getY(), selected.getRange(), 48);
        }
    }

    private void drawGoose(Goose goose) {
        float x = goose.getX();
        float y = goose.getY();
        float r = goose.getRadius();
        float fx = goose.getFacingX();
        float fy = goose.getFacingY();

        shapes.setColor(SHADOW);
        shapes.circle(x + 3f, y - 3f, r, 20);
        shapes.setColor(goose.isFleeing() ? GOOSE_SCARED : GOOSE_BODY);
        shapes.circle(x, y, r, 20);
        // Black neck/head in the direction of travel, with an orange beak.
        shapes.setColor(GOOSE_HEAD);
        shapes.circle(x + fx * r * 0.9f, y + fy * r * 0.9f, r * 0.45f, 12);
        shapes.setColor(GOOSE_BEAK);
        shapes.circle(x + fx * r * 1.4f, y + fy * r * 1.4f, r * 0.22f, 8);

        if (goose.isDistracted() && !goose.isFleeing()) {
            shapes.setColor(CRUMB);
            shapes.circle(x + fx * r * 2f + 3f, y + fy * r * 2f, 2.5f, 6);
            shapes.circle(x + fx * r * 2f - 3f, y + fy * r * 2f + 2f, 2.5f, 6);
        }
        // One orange pip per unit of hunger, so the player can see how much food each goose needs.
        shapes.setColor(HUNGER_PIP);
        float pipSpacing = 7f;
        float startX = x - (goose.getHunger() - 1) * pipSpacing / 2f;
        for (int i = 0; i < goose.getHunger(); i++) {
            shapes.circle(startX + i * pipSpacing, y + r + 7f, 2.6f, 8);
        }
    }

    private void drawPlayer(Player player) {
        float x = player.getX();
        float y = player.getY();
        float r = player.getRadius();
        if (player.isMobbed()) {
            shapes.setColor(PLAYER_MOBBED);
            shapes.circle(x, y, r + 7f, 24);
        }
        shapes.setColor(SHADOW);
        shapes.circle(x + 3f, y - 3f, r, 24);
        shapes.setColor(PLAYER_BODY);
        shapes.circle(x, y, r, 24);
        shapes.setColor(PLAYER_HEAD);
        shapes.circle(x, y + 2f, r * 0.55f, 16);
    }

    private void drawObstacleLabels(GameMap map) {
        font.setColor(LABEL);
        for (Obstacle obstacle : map.getObstacles()) {
            Rectangle b = obstacle.getBounds();
            layout.setText(font, obstacle.getName());
            font.draw(batch, layout, b.x + (b.width - layout.width) / 2f, b.y + (b.height + layout.height) / 2f);
        }
        font.setColor(Color.WHITE);
    }
}
