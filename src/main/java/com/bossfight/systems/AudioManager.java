package com.bossfight.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Music;

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
        BOSS_VINE_CHARGE(145f, 0.16f, 0.13f, 1.9f, 0.12f),
        BOSS_VINE_STRIKE(190f, 0.14f, 0.2f, 0.28f, 0.42f),
        BOSS_MAGIC_CHARGE(360f, 0.18f, 0.11f, 1.75f, 0.05f),
        BOSS_MAGIC_VOLLEY(760f, 0.1f, 0.15f, 0.62f, 0.08f),
        BOSS_POLLEN_CHARGE(250f, 0.22f, 0.1f, 1.3f, 0.22f),
        BOSS_POLLEN_DROP(125f, 0.16f, 0.16f, 0.5f, 0.3f),
        BOSS_DEFEAT_EXPLOSION(96f, 0.085f, 0.21f, 0.24f, 0.82f),
        DEFEAT(190f, 0.22f, 0.16f, 0.55f);

        private final float frequency;
        private final float duration;
        private final float volume;
        private final float sweep;
        private final float noiseMix;

        Cue(float frequency, float duration, float volume, float sweep) {
            this(frequency, duration, volume, sweep, 0f);
        }

        Cue(float frequency, float duration, float volume, float sweep, float noiseMix) {
            this.frequency = frequency;
            this.duration = duration;
            this.volume = volume;
            this.sweep = sweep;
            this.noiseMix = noiseMix;
        }
    }

    private static final int SAMPLE_RATE = 44100;

    private final ConcurrentLinkedQueue<Tone> tones = new ConcurrentLinkedQueue<>();
    private AudioDevice proceduralDevice;
    private Thread proceduralThread;
    private volatile boolean proceduralAudioRunning;
    private Music currentMusic;
    private Music currentVoice;

    public AudioManager() {
        startProceduralAudio();
    }

    public void playMusic(String path, boolean looping, float volume) {
        if (!exists(path)) {
            return;
        }

        stopMusic();
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentMusic.setLooping(looping);
        currentMusic.setVolume(Math.max(0f, Math.min(1f, volume)));
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    public void playVoice(String path, float volume) {
        if (!exists(path)) {
            return;
        }

        stopVoice();
        currentVoice = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentVoice.setLooping(false);
        currentVoice.setVolume(Math.max(0f, Math.min(1f, volume)));
        currentVoice.play();
    }

    public void stopVoice() {
        if (currentVoice != null) {
            currentVoice.stop();
            currentVoice.dispose();
            currentVoice = null;
        }
    }

    public void playCue(Cue cue) {
        if (cue == null || !proceduralAudioRunning) {
            return;
        }

        tones.offer(createTone(cue));
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
        stopVoice();
    }

    private boolean exists(String path) {
        return Gdx.files.internal(path).exists();
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

    private Tone createTone(Cue cue) {
        return new Tone(cue.frequency, cue.duration, cue.volume, cue.sweep, cue.noiseMix);
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
        int noiseState = 0x6D2B79F5 ^ Float.floatToIntBits(tone.frequency);

        for (int i = 0; i < sampleCount; i++) {
            float t = sampleCount == 1 ? 1f : (float) i / (sampleCount - 1);
            float frequency = tone.frequency * lerp(1f, tone.sweep, t);
            phase += Math.PI * 2.0 * frequency / SAMPLE_RATE;
            float attack = Math.min(1f, t / 0.12f);
            float decay = 1f - t;
            float envelope = attack * decay * decay;
            float sine = (float) Math.sin(phase);
            float square = sine >= 0f ? 1f : -1f;
            noiseState = noiseState * 1664525 + 1013904223;
            float noise = ((noiseState >>> 8) / 16777215f) * 2f - 1f;
            float tonal = sine * 0.72f + square * 0.28f;
            float mixedWave = tonal * (1f - tone.noiseMix) + noise * tone.noiseMix;
            samples[i] = mixedWave * envelope * tone.volume;
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
        private final float noiseMix;

        private Tone(float frequency, float duration, float volume, float sweep, float noiseMix) {
            this.frequency = frequency;
            this.duration = duration;
            this.volume = volume;
            this.sweep = sweep;
            this.noiseMix = noiseMix;
        }
    }
}
