package com.bossfight.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public final class BattleInput {
    private static final float ACTION_BUFFER_DURATION = 0.14f;

    private float jumpBufferTimer;
    private float dashBufferTimer;
    private float specialBufferTimer;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean shootHeld;

    public void poll(float delta) {
        jumpBufferTimer = updateBuffer(jumpBufferTimer, delta,
                Gdx.input.isKeyJustPressed(Input.Keys.W)
                        || Gdx.input.isKeyJustPressed(Input.Keys.UP)
                        || Gdx.input.isKeyJustPressed(Input.Keys.SPACE));
        dashBufferTimer = updateBuffer(dashBufferTimer, delta,
                Gdx.input.isKeyJustPressed(Input.Keys.K)
                        || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
                        || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT));
        specialBufferTimer = updateBuffer(specialBufferTimer, delta,
                Gdx.input.isKeyJustPressed(Input.Keys.G)
                        || Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)
                        || Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT));

        moveLeft = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        moveRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        shootHeld = Gdx.input.isKeyPressed(Input.Keys.F)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    public boolean isMoveLeftHeld() {
        return moveLeft;
    }

    public boolean isMoveRightHeld() {
        return moveRight;
    }

    public boolean isShootHeld() {
        return shootHeld;
    }

    public boolean consumeJump() {
        boolean buffered = jumpBufferTimer > 0f;
        jumpBufferTimer = 0f;
        return buffered;
    }

    public boolean consumeDash() {
        boolean buffered = dashBufferTimer > 0f;
        dashBufferTimer = 0f;
        return buffered;
    }

    public boolean consumeSpecial() {
        boolean buffered = specialBufferTimer > 0f;
        specialBufferTimer = 0f;
        return buffered;
    }

    private float updateBuffer(float timer, float delta, boolean pressed) {
        if (pressed) {
            return ACTION_BUFFER_DURATION;
        }
        return Math.max(0f, timer - delta);
    }
}
