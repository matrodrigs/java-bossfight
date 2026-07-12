package com.bossfight.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public final class BattleInput {
    private boolean jumpQueued;
    private boolean dashQueued;
    private boolean specialQueued;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean shootHeld;

    public void poll() {
        jumpQueued |= Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        dashQueued |= Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT);
        specialQueued |= Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);

        moveLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        moveRight = Gdx.input.isKeyPressed(Input.Keys.D);
        shootHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
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
        boolean queued = jumpQueued;
        jumpQueued = false;
        return queued;
    }

    public boolean consumeDash() {
        boolean queued = dashQueued;
        dashQueued = false;
        return queued;
    }

    public boolean consumeSpecial() {
        boolean queued = specialQueued;
        specialQueued = false;
        return queued;
    }
}
