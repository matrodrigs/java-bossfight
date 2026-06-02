package com.bossfight.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bossfight.MainGame;
import com.bossfight.systems.AnimationSystem;
import com.bossfight.util.Constants;

public class MenuScreen extends ScreenAdapter {
    private final MainGame game;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final AnimationSystem animationSystem;
    private float elapsed;

    public MenuScreen(MainGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        font = new BitmapFont();
        layout = new GlyphLayout();
        animationSystem = new AnimationSystem();
    }

    @Override
    public void render(float delta) {
        elapsed += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.showBattleScreen();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        Gdx.gl.glClearColor(0.07f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.getShapeRenderer().setProjectionMatrix(camera.combined);
        game.getShapeRenderer().begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        drawMenuBackground();
        game.getShapeRenderer().end();

        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        drawCentered("JAVA BOSSFIGHT", Constants.WORLD_HEIGHT - 210f, 3.2f, Color.WHITE);
        drawCentered("Pressione ENTER para iniciar", Constants.WORLD_HEIGHT - 310f, 1.35f, Color.SKY);
        drawCentered("A/D mover  |  W pular  |  J atirar  |  K dash", Constants.WORLD_HEIGHT - 365f, 1.1f, Color.LIGHT_GRAY);
        drawCentered("Esc sai do jogo", Constants.WORLD_HEIGHT - 420f, 1f, Color.GRAY);
        game.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        font.dispose();
    }

    private void drawMenuBackground() {
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        shapeRenderer.setColor(0.13f, 0.16f, 0.2f, 1f);
        shapeRenderer.rect(0f, 0f, Constants.WORLD_WIDTH, Constants.FLOOR_Y);

        shapeRenderer.setColor(0.28f, 0.32f, 0.38f, 1f);
        shapeRenderer.rect(Constants.ARENA_LEFT, Constants.FLOOR_Y, Constants.ARENA_RIGHT - Constants.ARENA_LEFT, 4f);

        float pulseSize = animationSystem.pulse(elapsed, 34f, 46f, 4f);
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.circle(Constants.WORLD_WIDTH * 0.5f, Constants.WORLD_HEIGHT - 500f, pulseSize);
    }

    private void drawCentered(String text, float y, float scale, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, text);
        float x = (Constants.WORLD_WIDTH - layout.width) * 0.5f;
        font.draw(game.getBatch(), layout, x, y);
    }
}
