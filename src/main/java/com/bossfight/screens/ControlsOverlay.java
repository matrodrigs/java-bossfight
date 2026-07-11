package com.bossfight.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.config.Constants;
import com.bossfight.systems.RetroTextFactory;
import com.bossfight.systems.TextureDraw;

final class ControlsOverlay {
    private static final String[] KEYS = {"A  D", "ESPAÇO", "SHIFT", "MOUSE 1", "MOUSE 2"};
    private static final String[] ACTIONS = {"MOVER", "PULAR", "DASH", "ATIRAR", "ESPECIAL"};
    private static final float[] KEY_CAP_WIDTHS = {82f, 122f, 104f, 122f, 122f};
    private static final float DISPLAY_DURATION = 5.4f;
    private static final float FADE_IN_DURATION = 0.32f;
    private static final float FADE_OUT_START = 4.25f;
    private static final float FADE_OUT_DURATION = DISPLAY_DURATION - FADE_OUT_START;
    private static final float PANEL_WIDTH = 1040f;
    private static final float PANEL_HEIGHT = 116f;
    private static final float PANEL_X = (Constants.WORLD_WIDTH - PANEL_WIDTH) * 0.5f;
    private static final float PANEL_Y = 574f;
    private static final float HEADER_CENTER_X = PANEL_X + 102f;
    private static final float SEPARATOR_X = PANEL_X + 202f;
    private static final float FIRST_CONTROL_X = PANEL_X + 280f;
    private static final float CONTROL_GAP = 168f;
    private static final float KEY_CENTER_Y = PANEL_Y + 71f;
    private static final float ACTION_CENTER_Y = PANEL_Y + 30f;

    private static boolean shownThisSession;

    private final Texture titleText;
    private final Texture subtitleText;
    private final Texture[] keyTexts = new Texture[KEYS.length];
    private final Texture[] actionTexts = new Texture[ACTIONS.length];
    private float displayTimer = DISPLAY_DURATION;

    ControlsOverlay(RetroTextFactory textFactory) {
        titleText = textFactory.createMenuOption("COMANDOS", true);
        subtitleText = textFactory.createInstruction("DO DUELO");
        for (int i = 0; i < KEYS.length; i++) {
            keyTexts[i] = textFactory.createInstructionKey(KEYS[i]);
            actionTexts[i] = textFactory.createInstruction(ACTIONS[i]);
        }
    }

    void showOnce() {
        if (shownThisSession) {
            return;
        }
        shownThisSession = true;
        displayTimer = 0f;
    }

    void update(float delta) {
        displayTimer = Math.min(DISPLAY_DURATION, displayTimer + delta);
    }

    void render(SpriteBatch batch, ShapeRenderer shapeRenderer, OrthographicCamera camera, float gameElapsed) {
        float alpha = alpha();
        if (alpha <= 0f) {
            return;
        }

        float entrance = smoothStep(MathUtils.clamp(displayTimer / FADE_IN_DURATION, 0f, 1f));
        float yOffset = (1f - entrance) * 18f + MathUtils.sin(gameElapsed * 2.8f) * 1.2f;
        float panelY = PANEL_Y + yOffset;

        drawCard(shapeRenderer, camera, panelY, alpha);
        drawText(batch, camera, panelY, alpha);
    }

    private void drawCard(ShapeRenderer shapeRenderer, OrthographicCamera camera, float panelY, float alpha) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        roundedRect(shapeRenderer, PANEL_X + 7f, panelY - 7f, PANEL_WIDTH, PANEL_HEIGHT, 12f,
                0.025f, 0.018f, 0.012f, alpha * 0.44f);
        roundedRect(shapeRenderer, PANEL_X, panelY, PANEL_WIDTH, PANEL_HEIGHT, 12f,
                0.23f, 0.075f, 0.045f, alpha * 0.98f);
        roundedRect(shapeRenderer, PANEL_X + 5f, panelY + 5f, PANEL_WIDTH - 10f, PANEL_HEIGHT - 10f, 9f,
                0.94f, 0.82f, 0.57f, alpha * 0.97f);
        roundedRect(shapeRenderer, PANEL_X + 11f, panelY + 11f, PANEL_WIDTH - 22f, PANEL_HEIGHT - 22f, 6f,
                0.32f, 0.11f, 0.065f, alpha * 0.95f);
        roundedRect(shapeRenderer, PANEL_X + 14f, panelY + 14f, PANEL_WIDTH - 28f, PANEL_HEIGHT - 28f, 5f,
                0.965f, 0.88f, 0.69f, alpha * 0.98f);

        shapeRenderer.setColor(0.37f, 0.12f, 0.07f, alpha * 0.72f);
        shapeRenderer.rect(SEPARATOR_X, panelY + 19f, 3f, PANEL_HEIGHT - 38f);

        for (int i = 0; i < KEY_CAP_WIDTHS.length; i++) {
            float centerX = FIRST_CONTROL_X + i * CONTROL_GAP;
            float capWidth = KEY_CAP_WIDTHS[i];
            roundedRect(shapeRenderer, centerX - capWidth * 0.5f + 2f, panelY + 51f,
                    capWidth, 39f, 8f, 0.17f, 0.055f, 0.035f, alpha * 0.42f);
            roundedRect(shapeRenderer, centerX - capWidth * 0.5f, panelY + 55f,
                    capWidth, 37f, 8f, 0.36f, 0.12f, 0.07f, alpha * 0.96f);
            roundedRect(shapeRenderer, centerX - capWidth * 0.5f + 4f, panelY + 59f,
                    capWidth - 8f, 27f, 5f, 0.98f, 0.79f, 0.29f, alpha * 0.94f);
        }

        float ornamentY = panelY + PANEL_HEIGHT * 0.5f;
        shapeRenderer.setColor(0.78f, 0.38f, 0.07f, alpha * 0.9f);
        shapeRenderer.circle(PANEL_X + 15f, ornamentY, 4f);
        shapeRenderer.circle(PANEL_X + PANEL_WIDTH - 15f, ornamentY, 4f);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawText(SpriteBatch batch, OrthographicCamera camera, float panelY, float alpha) {
        float yDelta = panelY - PANEL_Y;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, alpha);
        TextureDraw.centeredWithin(batch, titleText, HEADER_CENTER_X, PANEL_Y + 72f + yDelta, 0.43f, 178f);
        TextureDraw.centeredWithin(batch, subtitleText, HEADER_CENTER_X, PANEL_Y + 35f + yDelta, 0.31f, 150f);

        for (int i = 0; i < keyTexts.length; i++) {
            float centerX = FIRST_CONTROL_X + i * CONTROL_GAP;
            TextureDraw.centeredWithin(batch, keyTexts[i], centerX, KEY_CENTER_Y + yDelta, 0.34f,
                    KEY_CAP_WIDTHS[i] - 8f);
            TextureDraw.centeredWithin(batch, actionTexts[i], centerX, ACTION_CENTER_Y + yDelta, 0.32f, 132f);
        }

        batch.setColor(Color.WHITE);
        batch.end();
    }

    private float alpha() {
        if (displayTimer >= DISPLAY_DURATION) {
            return 0f;
        }
        float fadeIn = MathUtils.clamp(displayTimer / FADE_IN_DURATION, 0f, 1f);
        float fadeOut = displayTimer <= FADE_OUT_START
                ? 1f
                : 1f - MathUtils.clamp((displayTimer - FADE_OUT_START) / FADE_OUT_DURATION, 0f, 1f);
        return smoothStep(fadeIn) * smoothStep(fadeOut);
    }

    private float smoothStep(float value) {
        return value * value * (3f - 2f * value);
    }

    private void roundedRect(ShapeRenderer shapeRenderer, float x, float y, float width, float height, float radius,
                             float red, float green, float blue, float alpha) {
        float safeRadius = Math.min(radius, Math.min(width, height) * 0.5f);
        shapeRenderer.setColor(red, green, blue, alpha);
        shapeRenderer.rect(x + safeRadius, y, width - safeRadius * 2f, height);
        shapeRenderer.rect(x, y + safeRadius, width, height - safeRadius * 2f);
        shapeRenderer.circle(x + safeRadius, y + safeRadius, safeRadius);
        shapeRenderer.circle(x + width - safeRadius, y + safeRadius, safeRadius);
        shapeRenderer.circle(x + safeRadius, y + height - safeRadius, safeRadius);
        shapeRenderer.circle(x + width - safeRadius, y + height - safeRadius, safeRadius);
    }
}
