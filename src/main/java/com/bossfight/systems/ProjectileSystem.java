package com.bossfight.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bossfight.Constants;
import com.bossfight.entities.Projectile;

public class ProjectileSystem {
    private static final String WARNING_BANNER_PATH = "sprites/ui/warning_banner.png";
    private static final float WARNING_VERTICAL_THRESHOLD = 2f;
    private static final int WARNING_HEAD_SOURCE_X = 0;
    private static final int WARNING_HEAD_SOURCE_Y = 0;
    private static final int WARNING_HEAD_SOURCE_WIDTH = 325;
    private static final int WARNING_HEAD_SOURCE_HEIGHT = 220;
    private static final int WARNING_BODY_SOURCE_X = 50;
    private static final int WARNING_BODY_SOURCE_Y = 260;
    private static final int WARNING_BODY_SOURCE_WIDTH = 225;
    private static final int WARNING_BODY_SOURCE_HEIGHT = 600;
    private static final float WARNING_HEAD_LENGTH_FACTOR = 2.55f;
    private static final float WARNING_HEAD_THICKNESS_FACTOR = 1.32f;
    private static final float WARNING_BODY_THICKNESS_FACTOR = 0.84f;
    private static final float WARNING_ALPHA = 0.72f;
    private static final float IMPACT_SHADOW_MIN_WIDTH = 50f;
    private static final float IMPACT_SHADOW_MAX_WIDTH = 82f;
    private static final float IMPACT_SHADOW_MIN_HEIGHT = 13f;
    private static final float IMPACT_SHADOW_MAX_HEIGHT = 23f;
    private static final float POLLEN_IMPACT_SHADOW_MIN_FALL_SPEED = -160f;

    private final Array<Projectile> playerProjectiles = new Array<>();
    private final Array<Projectile> bossProjectiles = new Array<>();
    private final Texture playerPea;
    private final Texture playerSpecial;
    private final Texture bossSeed;
    private final Texture bossAcorn;
    private final Texture bossPollen;
    private final Texture bossThorn;
    private final Texture bossPetalBomb;
    private final Texture warningBanner;

    public ProjectileSystem() {
        playerPea = load("sprites/projectiles/player_pea.png");
        playerSpecial = load("sprites/projectiles/player_special.png");
        bossSeed = load("sprites/projectiles/boss_seed.png");
        bossAcorn = load("sprites/projectiles/boss_acorn.png");
        bossPollen = load("sprites/projectiles/boss_pollen.png");
        bossThorn = load("sprites/projectiles/boss_thorn.png");
        bossPetalBomb = load("sprites/projectiles/boss_petal_bomb.png");
        warningBanner = load(WARNING_BANNER_PATH);
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
        renderWarnings(shapeRenderer, playerProjectiles);
        renderWarnings(shapeRenderer, bossProjectiles);
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
        warningBanner.dispose();
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
                    if (!isVerticalWarning(projectile)) {
                        drawWarningBanner(batch, projectile);
                    }
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void renderWarnings(ShapeRenderer shapeRenderer, Array<Projectile> projectiles) {
        for (Projectile projectile : projectiles) {
            if (projectile.getKind() == Projectile.Kind.BOSS_WARNING && isVerticalWarning(projectile)) {
                drawPollenWarningShadow(shapeRenderer, projectile);
            } else if (isFallingImpactProjectile(projectile)) {
                drawFallingImpactShadow(shapeRenderer, projectile);
            }
        }
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
                true,
                1f,
                1f,
                1f,
                1f);
    }

    private void drawWarningBanner(SpriteBatch batch, Projectile projectile) {
        batch.setColor(1f, 1f, 1f, WARNING_ALPHA);
        float hitboxThickness = projectile.getHeight();
        float bodyThickness = hitboxThickness * WARNING_BODY_THICKNESS_FACTOR;
        float headThickness = hitboxThickness * WARNING_HEAD_THICKNESS_FACTOR;
        float bodyY = projectile.getY() + (hitboxThickness - bodyThickness) * 0.5f;
        float headY = projectile.getY() + (hitboxThickness - headThickness) * 0.5f;
        float headLength = Math.min(projectile.getWidth(), hitboxThickness * WARNING_HEAD_LENGTH_FACTOR);
        float bodySegmentLength = bodyThickness * WARNING_BODY_SOURCE_HEIGHT / WARNING_BODY_SOURCE_WIDTH;
        float drawX = projectile.getX() + headLength;
        float bodyEndX = projectile.getX() + projectile.getWidth();

        drawWarningRegion(batch,
                WARNING_HEAD_SOURCE_X,
                WARNING_HEAD_SOURCE_Y,
                WARNING_HEAD_SOURCE_WIDTH,
                WARNING_HEAD_SOURCE_HEIGHT,
                projectile.getX(),
                headY,
                headLength,
                headThickness);

        while (drawX < bodyEndX - 0.1f) {
            float drawLength = Math.min(bodySegmentLength, bodyEndX - drawX);
            int sourceHeight = Math.max(1,
                    Math.round(WARNING_BODY_SOURCE_HEIGHT * drawLength / bodySegmentLength));
            drawWarningRegion(batch,
                    WARNING_BODY_SOURCE_X,
                    WARNING_BODY_SOURCE_Y,
                    WARNING_BODY_SOURCE_WIDTH,
                    sourceHeight,
                    drawX,
                    bodyY,
                    drawLength,
                    bodyThickness);
            drawX += drawLength;
        }
    }

    private void drawWarningRegion(SpriteBatch batch, int sourceX, int sourceY, int sourceWidth, int sourceHeight,
                                   float x, float y, float length, float thickness) {
        batch.draw(warningBanner,
                x + length,
                y,
                0f,
                0f,
                thickness,
                length,
                1f,
                1f,
                90f,
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                false,
                false);
    }

    private void drawPollenWarningShadow(ShapeRenderer shapeRenderer, Projectile projectile) {
        float progress = projectile.getWarningProgress();
        float pulse = (MathUtils.sin(projectile.getAge() * 13f) + 1f) * 0.5f;
        float width = 56f + progress * 25f + pulse * 5f;
        float height = 15f + progress * 7f + pulse * 2f;
        float centerX = projectile.getCenterX();
        float y = Constants.FLOOR_Y - 7f;
        float alpha = MathUtils.clamp(0.22f + progress * 0.34f + pulse * 0.08f, 0f, 0.72f);

        drawImpactShadow(shapeRenderer, centerX, y, width, height, alpha);
    }

    private void drawFallingImpactShadow(ShapeRenderer shapeRenderer, Projectile projectile) {
        float centerX = projectedImpactCenterX(projectile);
        float y = Constants.FLOOR_Y - 7f;
        float floorDistance = Math.max(0f, projectile.getY() - Constants.FLOOR_Y);
        float fallAreaHeight = Constants.WORLD_HEIGHT - Constants.FLOOR_Y;
        float proximity = 1f - MathUtils.clamp(floorDistance / fallAreaHeight, 0f, 1f);
        float pulse = (MathUtils.sin(projectile.getAge() * 14f) + 1f) * 0.5f;
        float width = MathUtils.lerp(IMPACT_SHADOW_MIN_WIDTH, IMPACT_SHADOW_MAX_WIDTH, proximity) + pulse * 4f;
        float height = MathUtils.lerp(IMPACT_SHADOW_MIN_HEIGHT, IMPACT_SHADOW_MAX_HEIGHT, proximity) + pulse * 1.5f;
        float alpha = MathUtils.clamp(0.16f + proximity * 0.36f + pulse * 0.06f, 0f, 0.64f);

        drawImpactShadow(shapeRenderer, centerX, y, width, height, alpha);
    }

    private void drawImpactShadow(ShapeRenderer shapeRenderer, float centerX, float y, float width, float height,
                                  float alpha) {
        shapeRenderer.setColor(0.04f, 0.018f, 0.01f, alpha * 0.62f);
        shapeRenderer.ellipse(centerX - width * 0.5f, y - 2f, width, height);
        shapeRenderer.setColor(0.82f, 0.28f, 0.08f, alpha * 0.22f);
        shapeRenderer.ellipse(centerX - width * 0.42f, y + 2f, width * 0.84f, height * 0.64f);
        shapeRenderer.setColor(1f, 0.78f, 0.22f, alpha * 0.16f);
        shapeRenderer.ellipse(centerX - width * 0.34f, y + 5f, width * 0.68f, height * 0.34f);
    }

    private float projectedImpactCenterX(Projectile projectile) {
        if (projectile.getVelocityY() >= -1f) {
            return projectile.getCenterX();
        }

        float timeToFloor = Math.max(0f, (projectile.getY() - Constants.FLOOR_Y) / -projectile.getVelocityY());
        float projectedX = projectile.getCenterX() + projectile.getVelocityX() * timeToFloor;
        return MathUtils.clamp(projectedX, Constants.ARENA_LEFT, Constants.ARENA_RIGHT);
    }

    private boolean isVerticalWarning(Projectile projectile) {
        return projectile.getHeight() > projectile.getWidth() * WARNING_VERTICAL_THRESHOLD;
    }

    private boolean isFallingImpactProjectile(Projectile projectile) {
        if (projectile.getVelocityY() >= -1f) {
            return false;
        }

        return projectile.getKind() == Projectile.Kind.BOSS_PETAL_BOMB
                || (projectile.getKind() == Projectile.Kind.BOSS_POLLEN
                && projectile.getVelocityY() <= POLLEN_IMPACT_SHADOW_MIN_FALL_SPEED);
    }

    private float rotationFor(Projectile projectile) {
        return MathUtils.atan2(projectile.getVelocityY(), projectile.getVelocityX()) * MathUtils.radiansToDegrees;
    }
}
