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
import com.bossfight.config.Constants;
import com.bossfight.entities.Player;

public final class PlayerRenderer implements Disposable {
    private static final float FOOT_OFFSET = 11f;
    private static final float RUN_BOB = 1.45f;

    private enum Pose {
        HURT,
        DASH,
        SPECIAL,
        AIR_SHOOT,
        RUN_SHOOT,
        SHOOT,
        JUMP,
        RUN,
        IDLE
    }

    private final Texture spriteSheet;
    private final TextureRegion shootFrame;
    private final TextureRegion jumpFrame;
    private final TextureRegion runFrame;
    private final TextureRegion runAltFrame;
    private final TextureRegion hurtFrame;
    private final TextureRegion airShootFrame;
    private final TextureRegion runShootFrame;
    private final TextureRegion specialFrame;
    private final TextureRegion idleFrame;

    public PlayerRenderer() {
        spriteSheet = loadTexture("sprites/player/clock_player_sheet.png");
        shootFrame = frame(11, 272, 271, 271);
        jumpFrame = frame(379, 255, 234, 265);
        runFrame = frame(695, 277, 225, 260);
        runAltFrame = frame(58, 964, 225, 260);
        hurtFrame = frame(953, 289, 267, 291);
        airShootFrame = frame(314, 659, 273, 245);
        runShootFrame = frame(636, 690, 263, 250);
        specialFrame = frame(996, 682, 195, 260);
        idleFrame = frame(58, 684, 191, 254);
    }

    public void renderShadow(ShapeRenderer shapeRenderer, Player player) {
        float airHeight = Math.max(0f, player.getY() - Constants.FLOOR_Y);
        float groundCloseness = MathUtils.clamp(1f - airHeight / 280f, 0.34f, 1f);
        float width = 72f * groundCloseness;
        float height = 15f * groundCloseness;
        float alpha = 0.28f * groundCloseness;
        shapeRenderer.setColor(0.04f, 0.025f, 0.018f, alpha);
        shapeRenderer.ellipse(player.getCenterX() - width * 0.5f, Constants.FLOOR_Y - 9f, width, height);
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, Player player) {
        if (!player.shouldRenderSprite()) {
            return;
        }

        Pose pose = selectPose(player);
        TextureRegion frame = frameForPose(player, pose);
        float poseHeight = heightForPose(pose);
        float poseWidth = poseHeight * frame.getRegionWidth() / frame.getRegionHeight();
        float idleBreath = pose == Pose.IDLE ? MathUtils.sin(player.getAnimationTime() * 2.2f) : 0f;
        boolean groundStepPose = isGroundStepPose(pose) && player.isGrounded();
        float step = groundStepPose ? MathUtils.sin(player.getAnimationTime() * 13f) : 0f;
        float stepBounce = groundStepPose ? Math.abs(step) : 0f;
        float runBob = groundStepPose ? stepBounce * RUN_BOB : 0f;
        float squashX = pose == Pose.DASH
                ? 1.06f
                : (groundStepPose ? 1f + stepBounce * 0.014f : 1f + idleBreath * 0.004f);
        float squashY = pose == Pose.DASH
                ? 0.96f
                : (groundStepPose ? 1f - stepBounce * 0.01f : 1f - idleBreath * 0.003f);
        float rotation = pose == Pose.HURT
                ? -player.getFacingDirection() * 5f
                : (groundStepPose ? step * 0.6f : 0f);
        float drawX = player.getCenterX() - poseWidth * 0.5f;
        float drawY = player.getY() - FOOT_OFFSET + runBob;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (pose == Pose.HURT) {
            batch.setColor(1f, 0.88f, 0.86f, 1f);
        }
        batch.draw(spriteSheet,
                drawX, drawY,
                poseWidth * 0.5f, FOOT_OFFSET,
                poseWidth, poseHeight,
                squashX, squashY,
                rotation,
                frame.getRegionX(), frame.getRegionY(),
                frame.getRegionWidth(), frame.getRegionHeight(),
                shouldFlip(player, pose), false);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        spriteSheet.dispose();
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private TextureRegion frame(int x, int y, int width, int height) {
        return new TextureRegion(spriteSheet, x, y, width, height);
    }

    private Pose selectPose(Player player) {
        if (player.isHurtPoseActive()) {
            return Pose.HURT;
        }
        if (player.isSpecialPoseActive()) {
            return Pose.SPECIAL;
        }
        if (player.isShootPoseActive()) {
            if (player.isAirborne()) {
                return Pose.AIR_SHOOT;
            }
            if (player.isMovingHorizontally()) {
                return Pose.RUN_SHOOT;
            }
            return Pose.SHOOT;
        }
        if (player.isDashing()) {
            return Pose.DASH;
        }
        if (player.isAirborne()) {
            return Pose.JUMP;
        }
        if (player.isMovingHorizontally()) {
            return Pose.RUN;
        }
        return Pose.IDLE;
    }

    private TextureRegion frameForPose(Player player, Pose pose) {
        return switch (pose) {
            case HURT -> hurtFrame;
            case DASH, RUN -> ((int) (player.getAnimationTime() * 8f) & 1) == 0 ? runFrame : runAltFrame;
            case SPECIAL -> specialFrame;
            case AIR_SHOOT -> airShootFrame;
            case RUN_SHOOT -> runShootFrame;
            case SHOOT -> shootFrame;
            case JUMP -> jumpFrame;
            case IDLE -> idleFrame;
        };
    }

    private float heightForPose(Pose pose) {
        return switch (pose) {
            case HURT, AIR_SHOOT -> 128f;
            case DASH, RUN, RUN_SHOOT -> 122f;
            case SPECIAL, IDLE -> 126f;
            case SHOOT -> 120f;
            case JUMP -> 132f;
        };
    }

    private boolean shouldFlip(Player player, Pose pose) {
        if (pose == Pose.HURT
                || pose == Pose.SHOOT
                || pose == Pose.AIR_SHOOT
                || pose == Pose.RUN_SHOOT
                || pose == Pose.JUMP) {
            return player.getFacingDirection() > 0;
        }
        return player.getFacingDirection() < 0;
    }

    private boolean isGroundStepPose(Pose pose) {
        return pose == Pose.RUN || pose == Pose.DASH || pose == Pose.RUN_SHOOT;
    }
}
