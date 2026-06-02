package com.bossfight.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bossfight.MainGame;
import com.bossfight.util.Constants;

public class EndScreen extends ScreenAdapter {
    private final MainGame game;
    private final boolean victory;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final BitmapFont font;
    private final GlyphLayout layout;

    public EndScreen(MainGame game, boolean victory) {
        this.game = game;
        this.victory = victory;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        font = new BitmapFont();
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.showBattleScreen();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMenuScreen();
            return;
        }

        if (victory) {
            Gdx.gl.glClearColor(0.06f, 0.12f, 0.09f, 1f);
        } else {
            Gdx.gl.glClearColor(0.13f, 0.06f, 0.07f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        drawCentered(victory ? "VITORIA" : "DERROTA", Constants.WORLD_HEIGHT - 260f, 3f, Color.WHITE);
        drawCentered("R tenta novamente", Constants.WORLD_HEIGHT - 340f, 1.2f, Color.LIGHT_GRAY);
        drawCentered("ENTER volta ao menu", Constants.WORLD_HEIGHT - 390f, 1.2f, Color.LIGHT_GRAY);
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

    private void drawCentered(String text, float y, float scale, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, text);
        float x = (Constants.WORLD_WIDTH - layout.width) * 0.5f;
        font.draw(game.getBatch(), layout, x, y);
    }
}
