package com.bossfight.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bossfight.MainGame;
import com.bossfight.systems.RetroTextFactory;
import com.bossfight.Constants;

public class EndScreen extends ScreenAdapter {
    private final MainGame game;
    private final boolean victory;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final RetroTextFactory textFactory;
    private final Texture titleText;
    private final Texture retryText;
    private final Texture menuText;

    public EndScreen(MainGame game, boolean victory) {
        this.game = game;
        this.victory = victory;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        textFactory = new RetroTextFactory();
        titleText = textFactory.createResultTitle(victory ? "VITÓRIA!" : "DERROTA!", victory);
        retryText = textFactory.createInstruction("R  TENTAR DE NOVO");
        menuText = textFactory.createInstruction("ENTER  VOLTAR AO MENU");
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
        drawCenteredTexture(titleText, Constants.WORLD_HEIGHT - 250f, 0.92f);
        drawCenteredTexture(retryText, Constants.WORLD_HEIGHT - 340f, 0.7f);
        drawCenteredTexture(menuText, Constants.WORLD_HEIGHT - 390f, 0.7f);
        game.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        textFactory.dispose();
    }

    private void drawCenteredTexture(Texture texture, float centerY, float scale) {
        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;
        float x = (Constants.WORLD_WIDTH - width) * 0.5f;
        float y = centerY - height * 0.5f;
        game.getBatch().draw(texture, x, y, width, height);
    }
}
