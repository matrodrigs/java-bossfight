package com.bossfight.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class AssetManagerWrapper {
    private final AssetManager assetManager = new AssetManager();

    public boolean exists(String path) {
        return Gdx.files.internal(path).exists();
    }

    public void loadTexture(String path) {
        if (exists(path) && !assetManager.isLoaded(path)) {
            assetManager.load(path, Texture.class);
        }
    }

    public void loadSound(String path) {
        if (exists(path) && !assetManager.isLoaded(path)) {
            assetManager.load(path, Sound.class);
        }
    }

    public void loadMusic(String path) {
        if (exists(path) && !assetManager.isLoaded(path)) {
            assetManager.load(path, Music.class);
        }
    }

    public void finishLoading() {
        assetManager.finishLoading();
    }

    public Texture getTexture(String path) {
        return assetManager.get(path, Texture.class);
    }

    public Sound getSound(String path) {
        return assetManager.get(path, Sound.class);
    }

    public Music getMusic(String path) {
        return assetManager.get(path, Music.class);
    }

    public void unload(String path) {
        if (assetManager.isLoaded(path)) {
            assetManager.unload(path);
        }
    }

    public void dispose() {
        assetManager.dispose();
    }
}
