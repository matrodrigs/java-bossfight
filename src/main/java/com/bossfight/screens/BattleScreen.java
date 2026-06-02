package com.bossfight.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bossfight.MainGame;
import com.bossfight.entities.Boss;
import com.bossfight.entities.Player;
import com.bossfight.entities.Projectile;
import com.bossfight.systems.CollisionSystem;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.util.Constants;

public class BattleScreen extends ScreenAdapter {
    private final MainGame game;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final BitmapFont font;
    private final Player player;
    private final Boss boss;
    private final ProjectileSystem projectileSystem;
    private final CollisionSystem collisionSystem;

    public BattleScreen(MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        font = new BitmapFont();
        player = new Player();
        boss = new Boss();
        projectileSystem = new ProjectileSystem();
        collisionSystem = new CollisionSystem();
    }

    @Override
    public void render(float delta) {
        float safeDelta = Math.min(delta, 1f / 30f);
        if (!update(safeDelta)) {
            return;
        }

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        renderWorld();
        renderUi();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        font.dispose();
        projectileSystem.clear();
    }

    private boolean update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMenuScreen();
            return false;
        }

        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean dash = Gdx.input.isKeyJustPressed(Input.Keys.K)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT);
        boolean shoot = Gdx.input.isKeyPressed(Input.Keys.J)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        player.update(delta, moveLeft, moveRight, jump, dash);

        if (shoot) {
            Projectile projectile = player.tryShoot();
            if (projectile != null) {
                projectileSystem.addProjectile(projectile);
            }
        }

        boss.update(delta, projectileSystem, player);
        projectileSystem.update(delta);
        collisionSystem.resolve(player, boss, projectileSystem, delta);

        if (boss.isDefeated()) {
            game.showEndScreen(true);
            return false;
        }

        if (player.isDead()) {
            game.showEndScreen(false);
            return false;
        }

        return true;
    }

    private void renderWorld() {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.11f, 0.12f, 0.15f, 1f);
        shapeRenderer.rect(0f, 0f, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        shapeRenderer.setColor(0.18f, 0.2f, 0.23f, 1f);
        shapeRenderer.rect(0f, 0f, Constants.WORLD_WIDTH, Constants.FLOOR_Y);

        shapeRenderer.setColor(0.31f, 0.34f, 0.38f, 1f);
        shapeRenderer.rect(Constants.ARENA_LEFT, Constants.FLOOR_Y, Constants.ARENA_RIGHT - Constants.ARENA_LEFT, 5f);

        player.render(shapeRenderer);
        boss.render(shapeRenderer);
        projectileSystem.render(shapeRenderer);

        shapeRenderer.end();
    }

    private void renderUi() {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHealthBar(70f, Constants.WORLD_HEIGHT - 56f, 260f, 20f, player.getHealth(), player.getMaxHealth(), Color.CYAN);
        drawHealthBar(Constants.WORLD_WIDTH - 430f, Constants.WORLD_HEIGHT - 56f, 360f, 20f, boss.getHealth(), boss.getMaxHealth(), Color.ORANGE);
        shapeRenderer.end();

        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
        font.draw(game.getBatch(), "Player", 70f, Constants.WORLD_HEIGHT - 64f);
        font.draw(game.getBatch(), "Boss: " + boss.getStateName(), Constants.WORLD_WIDTH - 430f, Constants.WORLD_HEIGHT - 64f);
        game.getBatch().end();
    }

    private void drawHealthBar(float x, float y, float width, float height, int value, int maxValue, Color fillColor) {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        float percent = maxValue == 0 ? 0f : (float) value / maxValue;

        shapeRenderer.setColor(0.02f, 0.02f, 0.03f, 1f);
        shapeRenderer.rect(x - 2f, y - 2f, width + 4f, height + 4f);

        shapeRenderer.setColor(0.23f, 0.23f, 0.25f, 1f);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * percent, height);
    }
}
