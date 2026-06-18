package com.bossfight.entities;

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
    private final boolean removeOnHit;
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
        this(owner, x, y, width, height, velocityX, velocityY, damage, kind, lifetime, gravity, true);
    }

    public Projectile(Owner owner, float x, float y, float width, float height, float velocityX, float velocityY,
                      int damage, Kind kind, float lifetime, float gravity, boolean removeOnHit) {
        this.owner = owner;
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.lifetime = lifetime;
        this.gravity = gravity;
        this.removeOnHit = removeOnHit;
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

    public boolean shouldRemoveOnHit() {
        return removeOnHit;
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

    public float getWarningProgress() {
        if (lifetime <= 0f) {
            return 0f;
        }
        return MathUtils.clamp(age / lifetime, 0f, 1f);
    }
}
