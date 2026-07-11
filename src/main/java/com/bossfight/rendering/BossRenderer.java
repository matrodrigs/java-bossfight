package com.bossfight.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.bossfight.boss.Boss;
import com.bossfight.boss.BossVisualState;
import com.bossfight.config.Constants;
import com.bossfight.entities.Player;

public final class BossRenderer implements Disposable {
    private static final int SHEET_COLUMNS = 4;
    private static final int SHEET_ROWS = 2;
    private static final int FORWARD_FRAME_INDEX = 2;
    private static final int MAGIC_FRAME_INDEX = 3;
    private static final int FORWARD_RIGHT_TRIM = 24;
    private static final int MAGIC_LEFT_EXTENSION = 20;
    private static final float FRAME_ASPECT_RATIO = 3f / 4f;

    private final Texture spriteSheet;
    private final TextureRegion[] frames;
    private final Texture inbetweenSheet;
    private final TextureRegion[] inbetweenFrames;

    public BossRenderer() {
        spriteSheet = loadTexture("sprites/boss/flower_boss_sheet.png");
        frames = splitFrames(spriteSheet);
        inbetweenSheet = loadTexture("sprites/boss/flower_boss_inbetweens.png");
        inbetweenFrames = splitFrames(inbetweenSheet);
    }

    public void renderShadow(ShapeRenderer shapeRenderer, Boss boss) {
        float width = 330f;
        shapeRenderer.setColor(0.04f, 0.02f, 0.02f, 0.38f);
        shapeRenderer.ellipse(boss.getCenterX() - width * 0.5f - 16f, Constants.FLOOR_Y - 16f, width, 42f);
    }

    public void renderTelegraphGlow(ShapeRenderer shapeRenderer, Boss boss) {
        if (!boss.isTelegraphing() || boss.isDefeated()) {
            return;
        }

        float alpha = boss.getTelegraphAlpha();
        float radius = 118f + (1f - alpha) * 55f;
        float centerX = boss.getCenterX() - 30f;
        float centerY = Constants.FLOOR_Y + 360f;
        shapeRenderer.setColor(1f, 0.58f, 0.12f, 0.16f * alpha);
        shapeRenderer.circle(centerX, centerY, radius);
        shapeRenderer.setColor(1f, 0.94f, 0.38f, 0.16f * alpha);
        shapeRenderer.circle(centerX, centerY, radius * 0.58f);
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, Boss boss, Player player,
                       boolean fightStarted, float elapsed) {
        BossVisualState state = boss.getVisualState();
        boolean defeated = boss.isDefeated();
        boolean vineStrike = state == BossVisualState.VINE_STRIKE;
        boolean magicHands = state == BossVisualState.MAGIC_HANDS;
        boolean pollenRain = state == BossVisualState.POLLEN_RAIN;
        boolean pollenBreath = state == BossVisualState.POLLEN_BREATH;
        boolean phaseTransition = state == BossVisualState.ENRAGING;
        TextureRegion frame = selectFrame(boss, fightStarted, elapsed, state);
        boolean forwardIdleFrame = frame == frames[FORWARD_FRAME_INDEX]
                || frame == inbetweenFrames[FORWARD_FRAME_INDEX];

        float stateTime = boss.getVisualStateTime();
        float breath = defeated ? 0f : MathUtils.sin(stateTime * 3.4f);
        float windup = boss.isTelegraphing() ? smoothStep(1f - boss.getTelegraphAlpha()) : 0f;
        float actionTime = 1f - boss.getActionImpulse();
        float actionKick = boss.getActionImpulse() > 0f ? MathUtils.sin(actionTime * MathUtils.PI) : 0f;
        float actionFollowThrough = boss.getActionImpulse() > 0f
                ? MathUtils.sin(actionTime * MathUtils.PI2) * (1f - actionTime)
                : 0f;
        float hitTime = 1f - boss.getHitReaction();
        float hitKick = boss.getHitReaction() > 0f ? MathUtils.sin(hitTime * MathUtils.PI) : 0f;
        float attackPulse = !defeated && (vineStrike || magicHands || pollenRain || pollenBreath || phaseTransition)
                ? MathUtils.sin(stateTime * 9.5f)
                : 0f;
        float visualHeight = 506f + breath * 5f + (boss.isPhaseTwo() ? 18f : 0f);
        visualHeight += vineStrike ? windup * 18f : 0f;
        visualHeight += phaseTransition ? windup * 24f + actionKick * 12f : 0f;
        visualHeight -= defeated ? smoothStep(MathUtils.clamp(stateTime / 0.42f, 0f, 1f)) * 24f : 0f;
        float visualWidth = visualHeight * frame.getRegionWidth() / frame.getRegionHeight();

        float x = boss.getCenterX() - visualWidth * 0.5f - 18f;
        if (forwardIdleFrame) {
            float uncroppedVisualWidth = visualHeight * FRAME_ASPECT_RATIO;
            x -= (uncroppedVisualWidth - visualWidth) * 0.5f;
        }
        x += vineStrike
                ? attackPulse * 5f - 18f - windup * 18f - actionKick * 26f + actionFollowThrough * 11f
                : 0f;
        x += magicHands
                ? MathUtils.sin(stateTime * 7f) * 5f - windup * 10f - actionKick * 8f
                + actionFollowThrough * 5f
                : 0f;
        x += pollenBreath
                ? -windup * 18f + attackPulse * 4f - actionKick * 18f + actionFollowThrough * 8f
                : 0f;
        x += hitKick * 14f;

        float y = Constants.FLOOR_Y - 30f;
        y += breath * 2.5f;
        y += pollenRain ? MathUtils.sin(stateTime * 8.5f) * 6f + actionKick * 5f : 0f;
        y += phaseTransition ? attackPulse * 6f : 0f;
        y -= defeated ? smoothStep(MathUtils.clamp(stateTime / 0.42f, 0f, 1f)) * 16f : 0f;

        float scaleX = 1f + breath * 0.014f + (magicHands ? attackPulse * 0.02f : 0f)
                + actionKick * (vineStrike || pollenBreath ? 0.035f : 0.018f)
                + actionFollowThrough * 0.012f
                - hitKick * 0.025f;
        float scaleY = 1f - breath * 0.01f + (vineStrike ? attackPulse * 0.028f : 0f)
                - actionKick * (vineStrike || pollenBreath ? 0.045f : 0.018f)
                - actionFollowThrough * 0.01f
                + hitKick * 0.035f;
        float rotation = MathUtils.sin(stateTime * 2.1f) * 0.8f
                + attackPulse * (pollenRain || phaseTransition ? 1.4f : 0.45f)
                + actionKick * (vineStrike ? -2.4f : 1.2f)
                + actionFollowThrough * (vineStrike ? 2.2f : -1.4f)
                + hitKick * 2.8f;
        boolean flipX = pollenBreath && player.getCenterX() < boss.getCenterX();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (hitKick > 0.05f) {
            batch.setColor(1f, 0.66f + hitKick * 0.26f, 0.66f + hitKick * 0.26f, 1f);
        } else if (boss.isPhaseTwo()) {
            batch.setColor(1f, 0.92f, 0.92f, 1f);
        }
        if (flipX) {
            frame.flip(true, false);
        }
        batch.draw(frame,
                x, y,
                visualWidth * 0.48f, 44f,
                visualWidth, visualHeight,
                scaleX, scaleY,
                rotation);
        if (flipX) {
            frame.flip(true, false);
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        spriteSheet.dispose();
        inbetweenSheet.dispose();
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private TextureRegion[] splitFrames(Texture sheet) {
        if (sheet.getWidth() % SHEET_COLUMNS != 0 || sheet.getHeight() % SHEET_ROWS != 0) {
            throw new IllegalArgumentException("Boss sprite sheet must use an exact 4x2 grid");
        }

        int frameWidth = sheet.getWidth() / SHEET_COLUMNS;
        int frameHeight = sheet.getHeight() / SHEET_ROWS;
        TextureRegion[] result = new TextureRegion[SHEET_COLUMNS * SHEET_ROWS];
        int index = 0;

        for (int row = 0; row < SHEET_ROWS; row++) {
            for (int column = 0; column < SHEET_COLUMNS; column++) {
                int frameIndex = row * SHEET_COLUMNS + column;
                int frameX = column * frameWidth;
                int regionWidth = frameWidth;

                if (frameIndex == FORWARD_FRAME_INDEX) {
                    regionWidth -= FORWARD_RIGHT_TRIM;
                } else if (frameIndex == MAGIC_FRAME_INDEX) {
                    frameX -= MAGIC_LEFT_EXTENSION;
                    regionWidth += MAGIC_LEFT_EXTENSION;
                }

                result[index++] = createBleedSafeRegion(
                        sheet, frameX, row * frameHeight, regionWidth, frameHeight);
            }
        }
        return result;
    }

    private TextureRegion createBleedSafeRegion(Texture sheet, int x, int y, int width, int height) {
        float halfTexelU = 0.5f / sheet.getWidth();
        float halfTexelV = 0.5f / sheet.getHeight();
        TextureRegion region = new TextureRegion(sheet, x, y, width, height);
        region.setRegion(
                region.getU() + halfTexelU,
                region.getV() + halfTexelV,
                region.getU2() - halfTexelU,
                region.getV2() - halfTexelV);
        return region;
    }

    private TextureRegion selectFrame(Boss boss, boolean fightStarted, float elapsed, BossVisualState state) {
        if (!fightStarted) {
            return ((int) (elapsed * 2.2f) & 1) == 0 ? frames[0] : inbetweenFrames[0];
        }
        if (boss.isDefeated()) {
            return boss.getVisualStateTime() < 0.18f ? frames[7] : inbetweenFrames[7];
        }
        return switch (state) {
            case ENRAGING, MAGIC_HANDS -> attackAnimationFrame(boss, 3, 11f);
            case VINE_STRIKE -> attackAnimationFrame(boss, 4, 14f);
            case POLLEN_BREATH -> attackAnimationFrame(boss, 5, 13f);
            case POLLEN_RAIN -> attackAnimationFrame(boss, 6, 12f);
            case DEFEATED -> inbetweenFrames[7];
            case IDLE -> idleAnimationFrame(boss);
        };
    }

    private TextureRegion idleAnimationFrame(Boss boss) {
        int frame = (int) (boss.getVisualStateTime() * 7.5f) % 4;
        return switch (frame) {
            case 0 -> frames[1];
            case 1 -> inbetweenFrames[1];
            case 2 -> frames[2];
            default -> inbetweenFrames[2];
        };
    }

    private TextureRegion attackAnimationFrame(Boss boss, int index, float framesPerSecond) {
        if (boss.isTelegraphing() && boss.getActionImpulse() <= 0f) {
            return inbetweenFrames[index];
        }
        return ((int) (boss.getVisualStateTime() * framesPerSecond) & 1) == 0
                ? frames[index]
                : inbetweenFrames[index];
    }

    private float smoothStep(float value) {
        float t = MathUtils.clamp(value, 0f, 1f);
        return t * t * (3f - 2f * t);
    }
}
