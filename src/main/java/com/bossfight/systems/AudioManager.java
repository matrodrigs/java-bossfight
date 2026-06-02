package com.bossfight.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import com.bossfight.util.AssetManagerWrapper;

public class AudioManager {
    private final AssetManagerWrapper assets;
    private final ObjectMap<String, Sound> soundCache = new ObjectMap<>();
    private Music currentMusic;

    public AudioManager(AssetManagerWrapper assets) {
        this.assets = assets;
    }

    public void playMusic(String path, boolean looping) {
        if (!assets.exists(path)) {
            return;
        }

        stopMusic();
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentMusic.setLooping(looping);
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    public void playSound(String path) {
        if (!assets.exists(path)) {
            return;
        }

        Sound sound = soundCache.get(path);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(path));
            soundCache.put(path, sound);
        }
        sound.play();
    }

    public void dispose() {
        stopMusic();
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
    }
}
