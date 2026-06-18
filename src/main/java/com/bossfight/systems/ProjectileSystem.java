package com.bossfight.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bossfight.entities.Projectile;

public class ProjectileSystem {
    private final Array<Projectile> playerProjectiles = new Array<>();
    private final Array<Projectile> bossProjectiles = new Array<>();
    private final Texture playerPea;
    private final Texture playerSpecial;
    private final Texture bossSeed;
    private final Texture bossAcorn;
    private final Texture bossPollen;
    private final Texture bossThorn;
    private final Texture bossPetalBomb;

    public ProjectileSystem() {
        playerPea = load("sprites/projectiles/player_pea.png");
        playerSpecial = load("sprites/projectiles/player_special.png");
        bossSeed = load("sprites/projectiles/boss_seed.png");
        bossAcorn = load("sprites/projectiles/boss_acorn.png");
        bossPollen = load("sprites/projectiles/boss_pollen.png");
        bossThorn = load("sprites/projectiles/boss_thorn.png");
        bossPetalBomb = load("sprites/projectiles/boss_petal_bomb.png");
    }

    public void addProjectile(Projectile projectile) {
        if (projectile.getOwner() == Projectile.Owner.PLAYER) {
            playerProjectiles.add(projectile);
        } else {
            bossProjectiles.add(projectile);
        }
    }

    public void update(float delta) {
        updateProjectiles(playerProjectiles, delta);
        updateProjectiles(bossProjectiles, delta);
    }

    public void renderWarnings(ShapeRenderer shapeRenderer) {
        for (Projectile projectile : playerProjectiles) {
            projectile.renderWarning(shapeRenderer);
        }
        for (Projectile projectile : bossProjectiles) {
            projectile.renderWarning(shapeRenderer);
        }
    }

    public void renderSprites(SpriteBatch batch) {
        renderSprites(batch, playerProjectiles);
        renderSprites(batch, bossProjectiles);
    }

    public void clear() {
        playerProjectiles.clear();
        bossProjectiles.clear();
    }

    public void dispose() {
        clear();
        playerPea.dispose();
        playerSpecial.dispose();
        bossSeed.dispose();
        bossAcorn.dispose();
        bossPollen.dispose();
        bossThorn.dispose();
        bossPetalBomb.dispose();
    }

    public Array<Projectile> getPlayerProjectiles() {
        return playerProjectiles;
    }

    public Array<Projectile> getBossProjectiles() {
        return bossProjectiles;
    }

    private void updateProjectiles(Array<Projectile> projectiles, float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);

            if (!projectile.isActive() || projectile.isOutsideWorld()) {
                projectiles.removeIndex(i);
            }
        }
    }

    private Texture load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private void renderSprites(SpriteBatch batch, Array<Projectile> projectiles) {
        for (Projectile projectile : projectiles) {
            switch (projectile.getKind()) {
                case PLAYER_BASIC -> drawTextureAt(batch, playerPea,
                        projectile.getCenterX(),
                        projectile.getCenterY(),
                        58f + MathUtils.sin(projectile.getAge() * 15f) * 1.2f,
                        27f + MathUtils.sin(projectile.getAge() * 15f) * 0.6f,
                        MathUtils.sin(projectile.getAge() * 15f) * 2f,
                        projectile.getVelocityX() < 0f,
                        1f,
                        1f,
                        1f,
                        1f);
                case PLAYER_SPECIAL -> drawTextureAt(batch, playerSpecial,
                        projectile.getCenterX(),
                        projectile.getCenterY(),
                        158f + MathUtils.sin(projectile.getAge() * 10f) * 4f,
                        139f + MathUtils.sin(projectile.getAge() * 10f) * 3.5f,
                        0f,
                        projectile.getVelocityX() < 0f,
                        1f,
                        1f,
                        1f,
                        1f);
                case BOSS_SEED -> drawTextureWithTrail(batch, bossSeed, projectile,
                        46f, 34f, rotationFor(projectile), 2, 14f, 1f, 0.42f, 0.18f, 0.16f);
                case BOSS_ACORN -> drawTextureWithTrail(batch, bossAcorn, projectile,
                        50f, 54f, rotationFor(projectile), 2, 14f, 0.8f, 0.48f, 0.18f, 0.18f);
                case BOSS_POLLEN -> {
                    float pulse = MathUtils.sin(projectile.getAge() * 8f);
                    drawTextureAt(batch, bossPollen, projectile.getCenterX(), projectile.getCenterY(),
                            72f + pulse * 5f, 72f - pulse * 3f,
                            projectile.getAge() * 48f, false, 0.7f, 0.32f, 1f, 0.22f);
                    drawTexture(batch, bossPollen, projectile,
                        58f + MathUtils.sin(projectile.getAge() * 8f) * 4f,
                        58f + MathUtils.cos(projectile.getAge() * 8f) * 4f,
                        projectile.getAge() * 80f);
                }
                case BOSS_THORN -> drawThornLane(batch, projectile);
                case BOSS_PETAL_BOMB -> drawTextureWithTrail(batch, bossPetalBomb, projectile, 54f, 70f,
                        MathUtils.sin(projectile.getAge() * 9f) * 12f, 2, 18f,
                        1f, 0.44f, 0.12f, 0.2f);
                case BOSS_WARNING -> {
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawTexture(SpriteBatch batch, Texture texture, Projectile projectile,
                             float width, float height, float rotation) {
        drawTextureAt(batch, texture, projectile.getCenterX(), projectile.getCenterY(), width, height,
                rotation, false, 1f, 1f, 1f, 1f);
    }

    private void drawTextureWithTrail(SpriteBatch batch, Texture texture, Projectile projectile,
                                      float width, float height, float rotation, int trailCount, float trailSpacing,
                                      float red, float green, float blue, float alpha) {
        drawTextureWithTrail(batch, texture, projectile, width, height, rotation, false, trailCount, trailSpacing,
                red, green, blue, alpha);
    }

    private void drawTextureWithTrail(SpriteBatch batch, Texture texture, Projectile projectile,
                                      float width, float height, float rotation, boolean flipX, int trailCount,
                                      float trailSpacing, float red, float green, float blue, float alpha) {
        float velocityAngle = MathUtils.atan2(projectile.getVelocityY(), projectile.getVelocityX());
        float trailX = -MathUtils.cos(velocityAngle) * trailSpacing;
        float trailY = -MathUtils.sin(velocityAngle) * trailSpacing;

        for (int i = trailCount; i >= 1; i--) {
            float progress = (float) i / (trailCount + 1f);
            float scale = 1f + progress * 0.18f;
            drawTextureAt(batch, texture,
                    projectile.getCenterX() + trailX * i,
                    projectile.getCenterY() + trailY * i,
                    width * scale,
                    height * scale,
                    rotation - i * 5f,
                    flipX,
                    red,
                    green,
                    blue,
                    alpha * (1f - progress * 0.45f));
        }

        drawTextureAt(batch, texture, projectile.getCenterX(), projectile.getCenterY(), width, height,
                rotation, flipX, 1f, 1f, 1f, 1f);
    }

    private void drawTextureAt(SpriteBatch batch, Texture texture, float centerX, float centerY,
                               float width, float height, float rotation, boolean flipX,
                               float red, float green, float blue, float alpha) {
        float x = centerX - width * 0.5f;
        float y = centerY - height * 0.5f;
        batch.setColor(red, green, blue, alpha);
        batch.draw(texture,
                x,
                y,
                width * 0.5f,
                height * 0.5f,
                width,
                height,
                1f,
                1f,
                rotation,
                0,
                0,
                texture.getWidth(),
                texture.getHeight(),
                flipX,
                false);
    }

    private void drawThornLane(SpriteBatch batch, Projectile projectile) {
        float drawWidth = projectile.getWidth() + 48f;
        float drawHeight = Math.max(170f, projectile.getHeight() * 4.1f);
        float rise = MathUtils.sin(projectile.getAge() * 18f) * 3f;
        drawTextureAt(batch, bossThorn,
                projectile.getCenterX(),
                projectile.getCenterY() + rise,
                drawWidth,
                drawHeight,
                0f,
                false,
                1f,
                1f,
                1f,
                1f);
    }

    private float rotationFor(Projectile projectile) {
        return MathUtils.atan2(projectile.getVelocityY(), projectile.getVelocityX()) * MathUtils.radiansToDegrees;
    }
}
