package com.bossfight.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class ParticleSystem {
    private final Array<Particle> particles = new Array<>();

    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.life -= delta;
            if (particle.life <= 0f) {
                particles.removeIndex(i);
                continue;
            }

            particle.x += particle.velocityX * delta;
            particle.y += particle.velocityY * delta;
            particle.velocityY += particle.gravity * delta;
            particle.radius = Math.max(0f, particle.radius + particle.radiusVelocity * delta);
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        for (Particle particle : particles) {
            float alpha = MathUtils.clamp(particle.life / particle.maxLife, 0f, 1f);
            shapeRenderer.setColor(
                    particle.color.r,
                    particle.color.g,
                    particle.color.b,
                    particle.color.a * alpha
            );
            shapeRenderer.circle(particle.x, particle.y, particle.radius);
        }
    }

    public void clear() {
        particles.clear();
    }

    public void spawnDash(float x, float y, int direction) {
        for (int i = 0; i < 18; i++) {
            float speed = MathUtils.random(80f, 360f);
            float angle = MathUtils.random(-0.75f, 0.75f) + (direction > 0 ? MathUtils.PI : 0f);
            add(x, y + MathUtils.random(16f, 70f),
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    MathUtils.random(4f, 9f),
                    -18f,
                    0.28f,
                    new Color(0.34f, 0.92f, 1f, 0.85f),
                    -120f);
        }
    }

    public void spawnMuzzle(float x, float y, int direction, boolean special) {
        Color color = special ? new Color(1f, 0.74f, 0.18f, 0.9f) : new Color(0.42f, 0.94f, 1f, 0.88f);
        int count = special ? 22 : 9;
        for (int i = 0; i < count; i++) {
            float baseAngle = direction >= 0 ? 0f : MathUtils.PI;
            float angle = baseAngle + MathUtils.random(-0.45f, 0.45f);
            float speed = MathUtils.random(80f, special ? 520f : 260f);
            add(x, y,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    MathUtils.random(3f, special ? 10f : 6f),
                    -16f,
                    special ? 0.34f : 0.18f,
                    color,
                    -80f);
        }
    }

    public void spawnBossHit(float x, float y, boolean special) {
        Color color = special ? new Color(1f, 0.72f, 0.12f, 0.94f) : new Color(1f, 0.92f, 0.34f, 0.88f);
        int count = special ? 28 : 13;
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(70f, special ? 430f : 250f);
            add(x, y,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    MathUtils.random(3f, special ? 10f : 7f),
                    -18f,
                    special ? 0.42f : 0.25f,
                    color,
                    -220f);
        }
    }

    public void spawnBossDefeatBurst(float centerX, float centerY) {
        float anchorX = centerX + MathUtils.random(-145f, 90f);
        float anchorY = centerY + MathUtils.random(-128f, 118f);

        for (int i = 0; i < 11; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(55f, 230f);
            add(anchorX + MathUtils.random(-18f, 18f),
                    anchorY + MathUtils.random(-16f, 16f),
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed + MathUtils.random(30f, 140f),
                    MathUtils.random(7f, 16f),
                    MathUtils.random(-18f, 8f),
                    MathUtils.random(0.34f, 0.58f),
                    new Color(1f, 0.98f, 0.9f, 0.92f),
                    -360f);
        }

        for (int i = 0; i < 12; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(120f, 430f);
            Color color = MathUtils.randomBoolean()
                    ? new Color(1f, 0.84f, 0.16f, 0.9f)
                    : new Color(1f, 0.42f, 0.12f, 0.86f);
            add(anchorX,
                    anchorY,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    MathUtils.random(2.8f, 6.5f),
                    -14f,
                    MathUtils.random(0.25f, 0.42f),
                    color,
                    -260f);
        }
    }

    public void spawnPlayerDamage(float x, float y) {
        for (int i = 0; i < 18; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(90f, 320f);
            add(x, y,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    MathUtils.random(4f, 8f),
                    -20f,
                    0.34f,
                    new Color(1f, 0.2f, 0.18f, 0.92f),
                    -300f);
        }
    }

    public void spawnBossRoar(float x, float y) {
        for (int i = 0; i < 34; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(120f, 540f);
            add(x, y,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    MathUtils.random(5f, 12f),
                    -14f,
                    0.55f,
                    new Color(1f, 0.49f, 0.12f, 0.9f),
                    -180f);
        }
    }

    public void spawnBossAttack(float x, float y, Color color) {
        for (int i = 0; i < 14; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(70f, 260f);
            add(x, y,
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed,
                    MathUtils.random(3f, 8f),
                    -15f,
                    0.32f,
                    color,
                    -150f);
        }
    }

    private void add(float x, float y, float velocityX, float velocityY, float radius, float radiusVelocity,
                     float life, Color color, float gravity) {
        particles.add(new Particle(x, y, velocityX, velocityY, radius, radiusVelocity, life, color, gravity));
    }

    private static final class Particle {
        private final float maxLife;
        private final Color color;
        private final float gravity;
        private final float radiusVelocity;
        private float x;
        private float y;
        private float velocityX;
        private float velocityY;
        private float radius;
        private float life;

        private Particle(float x, float y, float velocityX, float velocityY, float radius, float radiusVelocity,
                         float life, Color color, float gravity) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.radius = radius;
            this.radiusVelocity = radiusVelocity;
            this.life = life;
            this.maxLife = life;
            this.color = color;
            this.gravity = gravity;
        }
    }
}
