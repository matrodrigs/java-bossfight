package com.bossfight.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.util.Constants;

public class Player {
    private final Hitbox hitbox;
    private final int maxHealth;
    private int health;
    private int facingDirection = 1;
    private float x;
    private float y;
    private float velocityY;
    private float shootCooldown;
    private float dashCooldown;
    private float dashTimer;
    private int dashDirection = 1;

    public Player() {
        x = Constants.PLAYER_START_X;
        y = Constants.PLAYER_START_Y;
        maxHealth = Constants.PLAYER_MAX_HEALTH;
        health = maxHealth;
        hitbox = new Hitbox(x, y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
    }

    public void update(float delta, boolean moveLeft, boolean moveRight, boolean jumpPressed, boolean dashPressed) {
        shootCooldown = Math.max(0f, shootCooldown - delta);
        dashCooldown = Math.max(0f, dashCooldown - delta);

        if (dashTimer > 0f) {
            dashTimer -= delta;
            x += dashDirection * Constants.PLAYER_DASH_SPEED * delta;
        } else {
            updateHorizontalMovement(delta, moveLeft, moveRight);
            updateJumpAndGravity(delta, jumpPressed);
            tryStartDash(dashPressed);
        }

        keepInsideArena();
        hitbox.setPosition(x, y);
    }

    public Projectile tryShoot() {
        if (shootCooldown > 0f) {
            return null;
        }

        shootCooldown = Constants.PLAYER_SHOOT_COOLDOWN;
        float projectileX = facingDirection > 0
                ? x + Constants.PLAYER_WIDTH
                : x - Constants.PLAYER_PROJECTILE_WIDTH;
        float projectileY = y + Constants.PLAYER_HEIGHT * 0.55f;
        float velocityX = facingDirection * Constants.PLAYER_PROJECTILE_SPEED;

        return new Projectile(
                Projectile.Owner.PLAYER,
                projectileX,
                projectileY,
                Constants.PLAYER_PROJECTILE_WIDTH,
                Constants.PLAYER_PROJECTILE_HEIGHT,
                velocityX,
                0f,
                Constants.PLAYER_PROJECTILE_DAMAGE
        );
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(x, y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);

        shapeRenderer.setColor(Color.WHITE);
        float eyeX = facingDirection > 0 ? x + 30f : x + 10f;
        shapeRenderer.circle(eyeX, y + 60f, 4f);
    }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public float getCenterX() {
        return x + Constants.PLAYER_WIDTH * 0.5f;
    }

    public float getCenterY() {
        return y + Constants.PLAYER_HEIGHT * 0.5f;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    private void updateHorizontalMovement(float delta, boolean moveLeft, boolean moveRight) {
        float velocityX = 0f;

        if (moveLeft) {
            velocityX -= Constants.PLAYER_SPEED;
            facingDirection = -1;
        }
        if (moveRight) {
            velocityX += Constants.PLAYER_SPEED;
            facingDirection = 1;
        }

        x += velocityX * delta;
    }

    private void updateJumpAndGravity(float delta, boolean jumpPressed) {
        if (jumpPressed && isOnGround()) {
            velocityY = Constants.PLAYER_JUMP_SPEED;
        }

        velocityY += Constants.GRAVITY * delta;
        y += velocityY * delta;
    }

    private void tryStartDash(boolean dashPressed) {
        if (!dashPressed || dashCooldown > 0f) {
            return;
        }

        dashTimer = Constants.PLAYER_DASH_DURATION;
        dashCooldown = Constants.PLAYER_DASH_COOLDOWN;
        dashDirection = facingDirection;
    }

    private void keepInsideArena() {
        x = MathUtils.clamp(x, Constants.ARENA_LEFT, Constants.ARENA_RIGHT - Constants.PLAYER_WIDTH);

        if (y < Constants.FLOOR_Y) {
            y = Constants.FLOOR_Y;
            velocityY = 0f;
        }
    }

    private boolean isOnGround() {
        return y <= Constants.FLOOR_Y + 0.1f;
    }
}
