package com.bossfight.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import com.bossfight.util.AssetManagerWrapper;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioManager {
    public enum Cue {
        MENU_MOVE(520f, 0.045f, 0.12f, 1.25f),
        MENU_CONFIRM(390f, 0.08f, 0.16f, 1.85f),
        MENU_BACK(260f, 0.08f, 0.13f, 0.62f),
        READY(330f, 0.16f, 0.18f, 1.55f),
        GO(760f, 0.16f, 0.2f, 0.78f),
        PLAYER_SHOOT(860f, 0.035f, 0.09f, 1.18f),
        PLAYER_SPECIAL(280f, 0.18f, 0.18f, 2.35f),
        DASH(170f, 0.07f, 0.11f, 2.1f),
        PLAYER_HIT(160f, 0.12f, 0.2f, 0.48f),
        BOSS_HIT(560f, 0.045f, 0.12f, 0.72f),
        BOSS_ROAR(90f, 0.24f, 0.18f, 0.5f),
        BOSS_ATTACK(250f, 0.09f, 0.12f, 1.45f),
        VICTORY(520f, 0.18f, 0.15f, 1.75f),
        DEFEAT(190f, 0.22f, 0.16f, 0.55f);

        private final float frequency;
        private final float duration;
        private final float volume;
        private final float sweep;

        Cue(float frequency, float duration, float volume, float sweep) {
            this.frequency = frequency;
            this.duration = duration;
            this.volume = volume;
            this.sweep = sweep;
        }
    }

    private static final int SAMPLE_RATE = 44100;

    private final AssetManagerWrapper assets;
    private final ObjectMap<String, Sound> soundCache = new ObjectMap<>();
    private final ConcurrentLinkedQueue<Tone> tones = new ConcurrentLinkedQueue<>();
    private AudioDevice proceduralDevice;
    private Thread proceduralThread;
    private volatile boolean proceduralAudioRunning;
    private Music currentMusic;

    public AudioManager(AssetManagerWrapper assets) {
        this.assets = assets;
        startProceduralAudio();
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

    public void playCue(Cue cue) {
        if (cue == null || !proceduralAudioRunning) {
            return;
        }

        tones.offer(new Tone(cue.frequency, cue.duration, cue.volume, cue.sweep));
    }

    public void dispose() {
        proceduralAudioRunning = false;
        if (proceduralThread != null) {
            proceduralThread.interrupt();
            try {
                proceduralThread.join(120L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        if (proceduralDevice != null) {
            proceduralDevice.dispose();
            proceduralDevice = null;
        }
        stopMusic();
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
    }

    private void startProceduralAudio() {
        try {
            proceduralDevice = Gdx.audio.newAudioDevice(SAMPLE_RATE, true);
            proceduralAudioRunning = true;
            proceduralThread = new Thread(this::runProceduralAudio, "bossfight-procedural-audio");
            proceduralThread.setDaemon(true);
            proceduralThread.start();
        } catch (RuntimeException exception) {
            proceduralAudioRunning = false;
            Gdx.app.log("AudioManager", "Procedural audio unavailable: " + exception.getMessage());
        }
    }

    private void runProceduralAudio() {
        while (proceduralAudioRunning) {
            Tone tone = tones.poll();
            if (tone == null) {
                sleepQuietly(4L);
                continue;
            }

            writeTone(tone);
        }
    }

    private void writeTone(Tone tone) {
        int sampleCount = Math.max(1, (int) (SAMPLE_RATE * tone.duration));
        float[] samples = new float[sampleCount];
        double phase = 0.0;

        for (int i = 0; i < sampleCount; i++) {
            float t = sampleCount == 1 ? 1f : (float) i / (sampleCount - 1);
            float frequency = tone.frequency * lerp(1f, tone.sweep, t);
            phase += Math.PI * 2.0 * frequency / SAMPLE_RATE;
            float attack = Math.min(1f, t / 0.12f);
            float decay = 1f - t;
            float envelope = attack * decay * decay;
            float sine = (float) Math.sin(phase);
            float square = sine >= 0f ? 1f : -1f;
            samples[i] = (sine * 0.72f + square * 0.28f) * envelope * tone.volume;
        }

        if (proceduralAudioRunning && proceduralDevice != null) {
            proceduralDevice.writeSamples(samples, 0, samples.length);
        }
    }

    private float lerp(float from, float to, float alpha) {
        return from + (to - from) * alpha;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class Tone {
        private final float frequency;
        private final float duration;
        private final float volume;
        private final float sweep;

        private Tone(float frequency, float duration, float volume, float sweep) {
            this.frequency = frequency;
            this.duration = duration;
            this.volume = volume;
            this.sweep = sweep;
        }
    }
}
