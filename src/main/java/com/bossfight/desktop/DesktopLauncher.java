package com.bossfight.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.bossfight.MainGame;
import com.bossfight.Constants;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(Constants.GAME_TITLE);
        config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        config.setForegroundFPS(Constants.TARGET_FPS);
        config.useVsync(true);
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void created(Lwjgl3Window window) {
                requestInitialFocus(window);
            }
        });

        new Lwjgl3Application(new MainGame(), config);
    }

    private static void requestInitialFocus(Lwjgl3Window window) {
        window.postRunnable(() -> {
            window.focusWindow();
            window.postRunnable(window::focusWindow);
        });
    }
}
