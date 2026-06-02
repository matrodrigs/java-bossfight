package com.bossfight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bossfight.screens.BattleScreen;
import com.bossfight.screens.EndScreen;
import com.bossfight.screens.MenuScreen;
import com.bossfight.systems.AudioManager;
import com.bossfight.util.AssetManagerWrapper;

public class MainGame extends Game {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private AssetManagerWrapper assets;
    private AudioManager audioManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        assets = new AssetManagerWrapper();
        audioManager = new AudioManager(assets);

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

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public AssetManagerWrapper getAssets() {
        return assets;
    }

    public AudioManager getAudioManager() {
        return audioManager;
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
        audioManager.dispose();
        assets.dispose();
    }
}
