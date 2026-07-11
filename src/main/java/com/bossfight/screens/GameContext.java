package com.bossfight.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bossfight.systems.AudioManager;

public interface GameContext {
    SpriteBatch getBatch();

    ShapeRenderer getShapeRenderer();

    AudioManager getAudioManager();

    boolean isIrisTransitionActive();

    void showMenuScreen();

    void showBattleScreen();

    void showEndScreen(boolean victory);
}
