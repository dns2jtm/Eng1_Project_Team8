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
 * Renders the 2D top-down campus map, player, geese and triggered events using
 * libGDX shapes and text. Does not modify world state.
 */
public class WorldRenderer {

    private static final Color GRASS = new Color(0.42f, 0.65f, 0.34f, 1f);
    private static final Color BORDER = new Color(0.27f, 0.46f, 0.22f, 1f);
    private static final Color PATH = new Color(0.85f, 0.78f, 0.62f, 1f);
    private static final Color BUILDING_WALL = new Color(0.44f, 0.41f, 0.39f, 1f);
    private static final Color BUILDING_ROOF = new Color(0.67f, 0.63f, 0.59f, 1f);
    private static final Color WATER_EDGE = new Color(0.20f, 0.43f, 0.74f, 1f);
    private static final Color WATER = new Color(0.29f, 0.56f, 0.87f, 1f);
    private static final Color BUILDING_LABEL = new Color(0.13f, 0.13f, 0.15f, 1f);
    private static final Color WATER_LABEL = new Color(0.94f, 0.98f, 1f, 1f);
    private static final Color SHADOW = new Color(0f, 0f, 0f, 0.22f);
    private static final Color RANGE_FILL = new Color(1f, 1f, 1f, 0.12f);
    private static final Color RANGE_OUTLINE = new Color(1f, 1f, 1f, 0.32f);
    private static final Color EVENT_POSITIVE = new Color(0.28f, 0.86f, 0.38f, 0.38f);
    private static final Color EVENT_NEGATIVE = new Color(0.92f, 0.30f, 0.25f, 0.38f);
    private static final Color GOOSE_BODY = new Color(0.97f, 0.97f, 0.95f, 1f);
    private static final Color GOOSE_SCARED = new Color(0.78f, 0.85f, 0.95f, 1f);
    private static final Color GOOSE_HEAD = new Color(0.16f, 0.16f, 0.16f, 1f);
    private static final Color GOOSE_BEAK = new Color(1f, 0.60f, 0.10f, 1f);
    private static final Color HUNGER_PIP = new Color(0.96f, 0.46f, 0.10f, 1f);
    private static final Color CRUMB = new Color(0.90f, 0.75f, 0.40f, 1f);
    private static final Color PLAYER_BODY = new Color(0.35f, 0.21f, 0.62f, 1f);
    private static final Color PLAYER_HEAD = new Color(0.97f, 0.82f, 0.68f, 1f);
    private static final Color PLAYER_MOBBED = new Color(0.95f, 0.20f, 0.20f, 0.45f);

    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public WorldRenderer(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        this.shapes = shapes;
        this.batch = batch;
        this.font = font;
    }

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

        Item selected = world.getPlayer().getInventory().selected();
        if (selected != null) {
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(RANGE_OUTLINE);
            shapes.circle(world.getPlayer().getX(), world.getPlayer().getY(), selected.getRange(), 48);
            shapes.end();
        }

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawObstacleLabels(world.getMap());
        drawTriggeredEventLabels(world);
        batch.end();
    }

    private void drawGround(GameMap map) {
        float w = map.getWidth();
        float h = map.getHeight();
        shapes.setColor(GRASS);
        shapes.rect(0f, 0f, w, h);
        shapes.setColor(PATH);
        for (Rectangle path : map.getPaths()) {
            shapes.rect(path.x, path.y, path.width, path.height);
        }
        shapes.setColor(BORDER);
        float b = 4f;
        shapes.rect(0f, 0f, w, b);
        shapes.rect(0f, h - b, w, b);
        shapes.rect(0f, 0f, b, h);
        shapes.rect(w - b, 0f, b, h);
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
                shapes.rect(b.x + 5f, b.y - 5f, b.width, b.height);
                shapes.setColor(BUILDING_WALL);
                shapes.rect(b.x, b.y, b.width, b.height);
                shapes.setColor(BUILDING_ROOF);
                shapes.rect(b.x + 6f, b.y + 6f, b.width - 12f, b.height - 12f);
            }
        }
    }

    private void drawTriggeredEvents(GameWorld world) {
        for (HiddenEvent event : world.getEvents()) {
            if (event.isTriggered()) {
                shapes.setColor(event.isPositive() ? EVENT_POSITIVE : EVENT_NEGATIVE);
                shapes.circle(event.getX(), event.getY(), event.getRadius(), 32);
            }
        }
    }

    private void drawSelectedItemRange(Player player) {
        Item selected = player.getInventory().selected();
        if (selected != null) {
            shapes.setColor(RANGE_FILL);
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
        shapes.setColor(GOOSE_HEAD);
        shapes.circle(x + fx * r * 0.9f, y + fy * r * 0.9f, r * 0.45f, 12);
        shapes.setColor(GOOSE_BEAK);
        shapes.circle(x + fx * r * 1.4f, y + fy * r * 1.4f, r * 0.24f, 8);

        if (goose.isDistracted() && !goose.isFleeing()) {
            shapes.setColor(CRUMB);
            shapes.circle(x + fx * r * 2f + 3f, y + fy * r * 2f, 2.5f, 6);
            shapes.circle(x + fx * r * 2f - 3f, y + fy * r * 2f + 2f, 2.5f, 6);
        }

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
            shapes.circle(x, y, r + 6f, 24);
        }
        shapes.setColor(SHADOW);
        shapes.circle(x + 3f, y - 3f, r, 24);
        shapes.setColor(PLAYER_BODY);
        shapes.circle(x, y, r, 24);
        shapes.setColor(PLAYER_HEAD);
        shapes.circle(x, y + 2f, r * 0.55f, 16);
    }

    private void drawObstacleLabels(GameMap map) {
        for (Obstacle obstacle : map.getObstacles()) {
            Rectangle b = obstacle.getBounds();
            font.setColor(obstacle.getType() == Obstacle.Type.WATER ? WATER_LABEL : BUILDING_LABEL);
            layout.setText(font, obstacle.getName());
            font.draw(batch, layout, b.x + (b.width - layout.width) / 2f, b.y + (b.height + layout.height) / 2f);
        }
        font.setColor(Color.WHITE);
    }

    private void drawTriggeredEventLabels(GameWorld world) {
        font.getData().setScale(0.9f);
        font.setColor(Color.WHITE);
        for (HiddenEvent event : world.getEvents()) {
            if (event.isTriggered()) {
                layout.setText(font, event.getName());
                font.draw(batch, layout, event.getX() - layout.width / 2f, event.getY() + layout.height / 2f);
            }
        }
        font.getData().setScale(1f);
    }
}
