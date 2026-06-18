package com.bossfight.entities;

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

    private void renderWarning(ShapeRenderer shapeRenderer, float width, float height) {
        float progress = warningProgress();
        float pulse = (MathUtils.sin(age * 10f) + 1f) * 0.5f;
        float alpha = 0.08f + progress * 0.05f + pulse * 0.025f;
        if (height > width * 2f) {
            renderColumnWarning(shapeRenderer, width, height, alpha);
        } else {
            renderLaneWarning(shapeRenderer, width, height, alpha);
        }
    }

    private void renderLaneWarning(ShapeRenderer shapeRenderer, float width, float height, float alpha) {
        float centerY = y + height * 0.5f;

        shapeRenderer.setColor(0.18f, 0.04f, 0.02f, alpha);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(1f, 0.72f, 0.18f, 0.34f);
        shapeRenderer.rectLine(x + 8f, y + 4f, x + width - 8f, y + 4f, 2.5f);
        shapeRenderer.rectLine(x + 8f, y + height - 4f, x + width - 8f, y + height - 4f, 2.5f);

        shapeRenderer.setColor(1f, 0.82f, 0.28f, 0.28f);
        for (float markerX = x + 34f; markerX < x + width - 24f; markerX += 120f) {
            shapeRenderer.triangle(markerX, centerY - 11f,
                    markerX + 22f, centerY,
                    markerX, centerY + 11f);
        }
    }

    private void renderColumnWarning(ShapeRenderer shapeRenderer, float width, float height, float alpha) {
        float centerX = x + width * 0.5f;
        float beamWidth = Math.max(width * 1.25f, 38f);

        shapeRenderer.setColor(0.16f, 0.04f, 0.22f, alpha);
        shapeRenderer.rect(centerX - beamWidth * 0.5f, y, beamWidth, height);

        shapeRenderer.setColor(1f, 0.74f, 0.2f, 0.30f);
        shapeRenderer.rectLine(centerX - beamWidth * 0.5f, y + 8f, centerX - beamWidth * 0.5f, y + height - 8f, 2.5f);
        shapeRenderer.rectLine(centerX + beamWidth * 0.5f, y + 8f, centerX + beamWidth * 0.5f, y + height - 8f, 2.5f);

        shapeRenderer.setColor(1f, 0.82f, 0.26f, 0.28f);
        shapeRenderer.ellipse(centerX - 30f, y + 14f, 60f, 18f);
    }

    private float warningProgress() {
        if (lifetime <= 0f) {
            return 0f;
        }
        return MathUtils.clamp(age / lifetime, 0f, 1f);
    }
}
