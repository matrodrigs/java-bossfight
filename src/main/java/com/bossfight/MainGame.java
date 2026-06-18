package com.bossfight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bossfight.screens.BattleScreen;
import com.bossfight.screens.EndScreen;
import com.bossfight.screens.MenuScreen;
import com.bossfight.systems.AudioManager;
import com.bossfight.systems.OldFilmEffect;
import com.bossfight.Constants;

public class MainGame extends Game {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private AudioManager audioManager;
    private OldFilmEffect oldFilmEffect;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        audioManager = new AudioManager();
        oldFilmEffect = new OldFilmEffect();

        showMenuScreen();
    }

    public void showMenuScreen() {
        changeScreen(new MenuScreen(this));
    }

    public void showBattleScreen() {
        changeScreen(new BattleScreen(this));
    }

    public void showEndScreen(boolean victory) {
        changeScreen(new EndScreen(this, victory));
    }

    @Override
    public void render() {
        if (isFullscreenShortcutPressed()) {
            toggleFullscreen();
            return;
        }
        oldFilmEffect.begin();
        super.render();
        oldFilmEffect.renderToScreen(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (oldFilmEffect != null) {
            oldFilmEffect.resize();
        }
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    private boolean isFullscreenShortcutPressed() {
        boolean altPressed = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
        return altPressed && Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    private void changeScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        setScreen(nextScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    @Override
    public void dispose() {
        Screen currentScreen = getScreen();
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        batch.dispose();
        shapeRenderer.dispose();
        if (oldFilmEffect != null) {
            oldFilmEffect.dispose();
        }
        audioManager.dispose();
    }
}
