package com.bossfight.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bossfight.util.Constants;

public class Projectile {
    public enum Owner {
        PLAYER,
        BOSS
    }

    private final Owner owner;
    private final Hitbox hitbox;
    private final float velocityX;
    private final float velocityY;
    private final int damage;
    private boolean active = true;
    private float x;
    private float y;

    public Projectile(Owner owner, float x, float y, float width, float height, float velocityX, float velocityY, int damage) {
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.hitbox = new Hitbox(x, y, width, height);
    }

    public void update(float delta) {
        x += velocityX * delta;
        y += velocityY * delta;
        hitbox.setPosition(x, y);
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (owner == Owner.PLAYER) {
            shapeRenderer.setColor(Color.SKY);
        } else {
            shapeRenderer.setColor(Color.SCARLET);
        }
        shapeRenderer.rect(x, y, hitbox.getBounds().width, hitbox.getBounds().height);
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

    public Hitbox getHitbox() {
        return hitbox;
    }

    public int getDamage() {
        return damage;
    }
}
