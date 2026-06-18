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
import com.bossfight.systems.IrisTransition;
import com.bossfight.systems.OldFilmEffect;

public class MainGame extends Game {
    private static final String MENU_MUSIC_PATH = "audio/music/menu_theme.mp3";
    private static final String VINYL_NOISE_PATH = "audio/music/vinyl_noise_loop.mp3";
    private static final String VICTORY_MUSIC_PATH = "audio/music/victory_theme.mp3";
    private static final String DEFEAT_MUSIC_PATH = "audio/music/defeat_theme.mp3";
    private static final float MENU_MUSIC_VOLUME = 0.07f;
    private static final float END_MUSIC_VOLUME = 0.07f;
    private static final float VINYL_NOISE_BASE_VOLUME = 0.03f;
    private static final float VINYL_NOISE_IRIS_VOLUME = 0.1f;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private AudioManager audioManager;
    private OldFilmEffect oldFilmEffect;
    private final IrisTransition irisTransition = new IrisTransition();

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        audioManager = new AudioManager();
        oldFilmEffect = new OldFilmEffect();
        audioManager.playAmbience(VINYL_NOISE_PATH, VINYL_NOISE_BASE_VOLUME);
        ensureCursorHidden();

        showMenuScreen();
    }

    public void showMenuScreen() {
        showScreen(IrisTransition.Target.MENU, false);
    }

    public void showBattleScreen() {
        showScreen(IrisTransition.Target.BATTLE, true);
    }

    public void showEndScreen(boolean victory) {
        showScreen(victory ? IrisTransition.Target.END_VICTORY : IrisTransition.Target.END_DEFEAT, false);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        ensureCursorHidden();
        if (isFullscreenShortcutPressed()) {
            toggleFullscreen();
            ensureCursorHidden();
            return;
        }
        oldFilmEffect.begin();
        super.render();
        updateIrisTransition(Math.min(delta, 1f / 30f));
        oldFilmEffect.renderToScreen(delta, getIrisApertureProgress());
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

    public boolean isIrisTransitionActive() {
        return irisTransition.isActive();
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

    private void updateIrisTransition(float delta) {
        if (!irisTransition.isActive()) {
            return;
        }

        irisTransition.update(delta);
        if (irisTransition.isTargetScreenPending()) {
            IrisTransition.Target target = irisTransition.getTarget();
            beforeIrisTargetScreenChange(target);
            changeScreen(createIrisTargetScreen());
            afterIrisTargetScreenChange(target);
            irisTransition.markTargetScreenShown();
        }

        if (irisTransition.isOpeningComplete()) {
            finishIrisTransition();
        }
    }

    private void showScreen(IrisTransition.Target target, boolean boostVinyl) {
        if (getScreen() == null) {
            changeScreen(createScreen(target, false));
            afterIrisTargetScreenChange(target);
            return;
        }

        startIrisTransition(target, boostVinyl);
    }

    private void startIrisTransition(IrisTransition.Target target, boolean boostVinyl) {
        if (!irisTransition.start(target, boostVinyl)) {
            return;
        }

        audioManager.setAmbienceVolume(boostVinyl ? VINYL_NOISE_IRIS_VOLUME : VINYL_NOISE_BASE_VOLUME);
    }

    private Screen createIrisTargetScreen() {
        return createScreen(irisTransition.getTarget(), true);
    }

    private Screen createScreen(IrisTransition.Target target, boolean introPausedForTransition) {
        return switch (target) {
            case MENU -> new MenuScreen(this);
            case BATTLE -> new BattleScreen(this, introPausedForTransition);
            case END_VICTORY -> new EndScreen(this, true);
            case END_DEFEAT -> new EndScreen(this, false);
        };
    }

    private float getIrisApertureProgress() {
        return irisTransition.getApertureProgress();
    }

    private void startBattleIntroAfterIris() {
        Screen currentScreen = getScreen();
        if (currentScreen instanceof BattleScreen battleScreen) {
            battleScreen.startIntroAfterTransition();
        }
    }

    private void finishIrisTransition() {
        IrisTransition.Target finishedTarget = irisTransition.getTarget();
        boolean restoreVinylVolume = irisTransition.boostsVinyl();
        irisTransition.cancel();

        if (restoreVinylVolume) {
            audioManager.setAmbienceVolume(VINYL_NOISE_BASE_VOLUME);
        }

        if (finishedTarget == IrisTransition.Target.BATTLE) {
            startBattleIntroAfterIris();
        }
    }

    private void beforeIrisTargetScreenChange(IrisTransition.Target target) {
        if (target == IrisTransition.Target.BATTLE) {
            audioManager.stopMusic();
        }
    }

    private void afterIrisTargetScreenChange(IrisTransition.Target target) {
        if (target == IrisTransition.Target.MENU) {
            audioManager.playMusic(MENU_MUSIC_PATH, true, MENU_MUSIC_VOLUME);
        } else if (target == IrisTransition.Target.END_VICTORY) {
            audioManager.playMusic(VICTORY_MUSIC_PATH, true, END_MUSIC_VOLUME);
        } else if (target == IrisTransition.Target.END_DEFEAT) {
            audioManager.playMusic(DEFEAT_MUSIC_PATH, true, END_MUSIC_VOLUME);
        }
    }

    private void changeScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        setScreen(nextScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    private void ensureCursorHidden() {
        if (!Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(true);
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
