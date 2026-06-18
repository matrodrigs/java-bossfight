package com.bossfight.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.Constants;

public class Projectile {
    public enum Owner {
        PLAYER,
        BOSS
    }

    public enum Kind {
        PLAYER_BASIC,
        PLAYER_SPECIAL,
        BOSS_SEED,
        BOSS_ACORN,
        BOSS_POLLEN,
        BOSS_THORN,
        BOSS_PETAL_BOMB,
        BOSS_WARNING
    }

    private final Owner owner;
    private final Kind kind;
    private final Hitbox hitbox;
    private final float gravity;
    private final float lifetime;
    private final int damage;
    private boolean active = true;
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float age;

    public Projectile(Owner owner, float x, float y, float width, float height, float velocityX, float velocityY, int damage) {
        this(owner, x, y, width, height, velocityX, velocityY, damage,
                owner == Owner.PLAYER ? Kind.PLAYER_BASIC : Kind.BOSS_SEED, -1f, 0f);
    }

    public Projectile(Owner owner, float x, float y, float width, float height, float velocityX, float velocityY,
                      int damage, Kind kind) {
        this(owner, x, y, width, height, velocityX, velocityY, damage, kind, -1f, 0f);
    }

    public Projectile(Owner owner, float x, float y, float width, float height, float velocityX, float velocityY,
                      int damage, Kind kind, float lifetime) {
        this(owner, x, y, width, height, velocityX, velocityY, damage, kind, lifetime, 0f);
    }

    public Projectile(Owner owner, float x, float y, float width, float height, float velocityX, float velocityY,
                      int damage, Kind kind, float lifetime, float gravity) {
        this.owner = owner;
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.lifetime = lifetime;
        this.gravity = gravity;
        this.hitbox = new Hitbox(x, y, width, height);
    }

    public void update(float delta) {
        age += delta;
        velocityY += gravity * delta;
        x += velocityX * delta;
        y += velocityY * delta;
        hitbox.setPosition(x, y);

        if (lifetime > 0f && age >= lifetime) {
            active = false;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        float width = hitbox.getBounds().width;
        float height = hitbox.getBounds().height;
        float centerX = x + width * 0.5f;
        float centerY = y + height * 0.5f;

        switch (kind) {
            case PLAYER_BASIC -> renderPlayerShot(shapeRenderer, width, height);
            case PLAYER_SPECIAL -> renderSpecialShot(shapeRenderer, width, height);
            case BOSS_ACORN -> renderAcorn(shapeRenderer, centerX, centerY, width, height);
            case BOSS_POLLEN -> renderPollen(shapeRenderer, centerX, centerY, width, height);
            case BOSS_THORN -> renderThorn(shapeRenderer, width, height);
            case BOSS_PETAL_BOMB -> renderPetalBomb(shapeRenderer, centerX, centerY, width, height);
            case BOSS_WARNING -> renderWarning(shapeRenderer, width, height);
            case BOSS_SEED -> renderSeed(shapeRenderer, centerX, centerY, width, height);
        }
    }

    public void renderWarning(ShapeRenderer shapeRenderer) {
        if (kind != Kind.BOSS_WARNING) {
            return;
        }

        renderWarning(shapeRenderer, hitbox.getBounds().width, hitbox.getBounds().height);
    }

    public boolean isOutsideWorld() {
        return x < -100f
                || x > Constants.WORLD_WIDTH + 100f
                || y < -100f
                || y > Constants.WORLD_HEIGHT + 100f;
    }

    public void deactivate() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public Owner getOwner() {
        return owner;
    }

    public Kind getKind() {
        return kind;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isSpecial() {
        return kind == Kind.PLAYER_SPECIAL;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getCenterX() {
        return x + hitbox.getBounds().width * 0.5f;
    }

    public float getCenterY() {
        return y + hitbox.getBounds().height * 0.5f;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public float getWidth() {
        return hitbox.getBounds().width;
    }

    public float getHeight() {
        return hitbox.getBounds().height;
    }

    public float getAge() {
        return age;
    }

    private void renderPlayerShot(ShapeRenderer shapeRenderer, float width, float height) {
        shapeRenderer.setColor(0.04f, 0.1f, 0.14f, 1f);
        shapeRenderer.ellipse(x - 3f, y - 3f, width + 6f, height + 6f);
        shapeRenderer.setColor(0.35f, 0.92f, 1f, 1f);
        shapeRenderer.ellipse(x, y, width, height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.ellipse(x + width * 0.18f, y + height * 0.25f, width * 0.5f, height * 0.45f);
    }

    private void renderSpecialShot(ShapeRenderer shapeRenderer, float width, float height) {
        shapeRenderer.setColor(0.2f, 0.08f, 0.02f, 1f);
        shapeRenderer.ellipse(x - 10f, y - 7f, width + 20f, height + 14f);
        shapeRenderer.setColor(1f, 0.62f, 0.12f, 1f);
        shapeRenderer.ellipse(x - 6f, y - 4f, width + 12f, height + 8f);
        shapeRenderer.setColor(1f, 0.95f, 0.38f, 1f);
        shapeRenderer.ellipse(x, y, width, height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.ellipse(x + width * 0.18f, y + height * 0.27f, width * 0.48f, height * 0.36f);
    }

    private void renderSeed(ShapeRenderer shapeRenderer, float centerX, float centerY, float width, float height) {
        float radius = Math.max(width, height) * 0.5f;
        shapeRenderer.setColor(0.22f, 0.04f, 0.04f, 1f);
        shapeRenderer.circle(centerX, centerY, radius + 4f);
        shapeRenderer.setColor(0.92f, 0.16f, 0.18f, 1f);
        shapeRenderer.circle(centerX, centerY, radius);
        shapeRenderer.setColor(1f, 0.74f, 0.24f, 1f);
        shapeRenderer.circle(centerX - radius * 0.18f, centerY + radius * 0.18f, radius * 0.32f);
    }

    private void renderAcorn(ShapeRenderer shapeRenderer, float centerX, float centerY, float width, float height) {
        shapeRenderer.setColor(0.12f, 0.06f, 0.03f, 1f);
        shapeRenderer.triangle(centerX, y - 5f, x - 5f, y + height * 0.68f, x + width + 5f, y + height * 0.68f);
        shapeRenderer.setColor(0.45f, 0.25f, 0.1f, 1f);
        shapeRenderer.triangle(centerX, y, x, y + height * 0.65f, x + width, y + height * 0.65f);
        shapeRenderer.setColor(0.22f, 0.11f, 0.05f, 1f);
        shapeRenderer.rect(x + width * 0.12f, y + height * 0.54f, width * 0.76f, height * 0.28f);
        shapeRenderer.setColor(0.82f, 0.5f, 0.16f, 1f);
        shapeRenderer.circle(centerX, centerY - height * 0.06f, Math.min(width, height) * 0.24f);
    }

    private void renderPollen(ShapeRenderer shapeRenderer, float centerX, float centerY, float width, float height) {
        float radius = Math.max(width, height) * 0.5f;
        shapeRenderer.setColor(0.22f, 0.06f, 0.28f, 0.44f);
        shapeRenderer.circle(centerX - velocityX * 0.02f, centerY - velocityY * 0.02f, radius * 1.4f);
        shapeRenderer.setColor(0.18f, 0.05f, 0.22f, 1f);
        shapeRenderer.circle(centerX, centerY, radius + 4f);
        shapeRenderer.setColor(0.8f, 0.32f, 0.96f, 1f);
        shapeRenderer.circle(centerX, centerY, radius);
        shapeRenderer.setColor(1f, 0.84f, 0.38f, 1f);
        for (int i = 0; i < 5; i++) {
            float angle = age * 9f + MathUtils.PI2 * i / 5f;
            shapeRenderer.circle(centerX + MathUtils.cos(angle) * radius * 0.58f,
                    centerY + MathUtils.sin(angle) * radius * 0.58f, radius * 0.16f);
        }
    }

    private void renderThorn(ShapeRenderer shapeRenderer, float width, float height) {
        float centerY = y + height * 0.5f;
        shapeRenderer.setColor(0.04f, 0.16f, 0.07f, 1f);
        shapeRenderer.rectLine(x, centerY, x + width, centerY + MathUtils.sin(age * 24f) * 5f, 28f);
        shapeRenderer.setColor(0.1f, 0.42f, 0.17f, 1f);
        shapeRenderer.rectLine(x, centerY, x + width, centerY + MathUtils.sin(age * 24f) * 5f, 18f);
        shapeRenderer.setColor(0.68f, 0.92f, 0.35f, 1f);
        float step = 58f;
        for (float spikeX = x + 16f; spikeX < x + width; spikeX += step) {
            float wave = MathUtils.sin(age * 18f + spikeX * 0.03f) * 3f;
            shapeRenderer.triangle(spikeX, centerY + 8f + wave, spikeX + 22f, centerY + 8f + wave, spikeX + 11f, centerY + 36f + wave);
            shapeRenderer.triangle(spikeX + 28f, centerY - 8f - wave, spikeX + 50f, centerY - 8f - wave, spikeX + 39f, centerY - 34f - wave);
        }
    }

    private void renderPetalBomb(ShapeRenderer shapeRenderer, float centerX, float centerY, float width, float height) {
        float radius = Math.max(width, height) * 0.5f;
        shapeRenderer.setColor(0.22f, 0.06f, 0.02f, 1f);
        shapeRenderer.circle(centerX, centerY, radius + 4f);
        shapeRenderer.setColor(0.94f, 0.34f, 0.08f, 1f);
        shapeRenderer.circle(centerX, centerY, radius);
        shapeRenderer.setColor(1f, 0.68f, 0.18f, 1f);
        shapeRenderer.ellipse(centerX - radius * 0.35f, centerY + radius * 0.1f, radius * 0.7f, radius * 0.42f);
    }

    private void renderWarning(ShapeRenderer shapeRenderer, float width, float height) {
        float pulse = (MathUtils.sin(age * 28f) + 1f) * 0.5f;
        shapeRenderer.setColor(1f, 0.18f + pulse * 0.18f, 0.09f, 0.26f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(1f, 0.86f, 0.32f, 0.9f);
        if (height > width * 2f) {
            for (float markerY = y + 18f; markerY < y + height; markerY += 56f) {
                shapeRenderer.rect(x + width * 0.32f, markerY, width * 0.36f, 30f);
            }
        } else {
            for (float markerX = x + 18f; markerX < x + width; markerX += 70f) {
                shapeRenderer.triangle(markerX, y + height * 0.5f,
                        markerX + 24f, y + height - 5f,
                        markerX + 24f, y + 5f);
            }
        }
    }
}
