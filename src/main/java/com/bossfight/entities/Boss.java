package com.bossfight.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.boss.AttackOneState;
import com.bossfight.boss.AttackThreeState;
import com.bossfight.boss.AttackTwoState;
import com.bossfight.boss.BossState;
import com.bossfight.boss.BossSoundEvent;
import com.bossfight.boss.DefeatedState;
import com.bossfight.boss.IdleState;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.Constants;

import java.util.ArrayDeque;

public class Boss {
    private final Hitbox hitbox;
    private final ArrayDeque<BossSoundEvent> soundEvents = new ArrayDeque<>();
    private final int maxHealth;
    private BossState currentState;
    private int health;
    private int nextAttackIndex;
    private float x;
    private float y;
    private float entranceTimer;
    private float hurtFlashTimer;
    private float attackSquashTimer;
    private float attackMotionTimer;
    private float attackMotionDuration = 1f;
    private float attackMotionStrength;
    private float telegraphTimer;
    private float telegraphDuration = 1f;
    private final Color telegraphColor = new Color(1f, 0.24f, 0.1f, 1f);

    public Boss() {
        x = Constants.BOSS_START_X;
        y = Constants.BOSS_START_Y;
        maxHealth = Constants.BOSS_MAX_HEALTH;
        health = maxHealth;
        hitbox = new Hitbox(x, y, Constants.BOSS_WIDTH, Constants.BOSS_HEIGHT);
        currentState = new IdleState(1.2f);
    }

    public void update(float delta, ProjectileSystem projectileSystem, Player player) {
        entranceTimer += delta;
        hurtFlashTimer = Math.max(0f, hurtFlashTimer - delta);
        attackSquashTimer = Math.max(0f, attackSquashTimer - delta);
        attackMotionTimer = Math.max(0f, attackMotionTimer - delta);
        telegraphTimer = Math.max(0f, telegraphTimer - delta);

        if (health <= 0 && !(currentState instanceof DefeatedState)) {
            setState(new DefeatedState());
        }

        currentState.update(this, delta, projectileSystem, player);
        hitbox.setPosition(x, y);
    }

    public void updateEntrance(float delta) {
        entranceTimer += delta;
        hitbox.setPosition(x, y);
    }

    public void render(ShapeRenderer shapeRenderer) {
        renderFlower(shapeRenderer, entranceTimer, true);
    }

    public void renderEntrance(ShapeRenderer shapeRenderer) {
        renderFlower(shapeRenderer, entranceTimer, false);
    }

    public boolean takeDamage(int amount) {
        if (isDefeated()) {
            return false;
        }

        health = MathUtils.clamp(health - amount, 0, maxHealth);
        hurtFlashTimer = 0.12f;
        attackSquashTimer = 0.08f;
        if (health == 0) {
            soundEvents.clear();
            setState(new DefeatedState());
        }
        return true;
    }

    public void emitSound(BossSoundEvent soundEvent) {
        if (soundEvent != null && !isDefeated()) {
            soundEvents.offer(soundEvent);
        }
    }

    public BossSoundEvent pollSoundEvent() {
        return soundEvents.poll();
    }

    public void showTelegraph(Color color, float duration) {
        telegraphColor.set(color);
        telegraphDuration = Math.max(0.01f, duration);
        telegraphTimer = telegraphDuration;
    }

    public void playAttackMotion(float duration, float strength) {
        attackMotionDuration = Math.max(0.01f, duration);
        attackMotionTimer = attackMotionDuration;
        attackMotionStrength = Math.max(0f, strength);
        attackSquashTimer = Math.max(attackSquashTimer, duration * 0.45f);
    }

    public BossState createNextAttackState() {
        BossState nextState;
        if (nextAttackIndex == 0) {
            nextState = new AttackOneState();
        } else if (nextAttackIndex == 1) {
            nextState = new AttackTwoState();
        } else {
            nextState = new AttackThreeState();
        }

        nextAttackIndex = (nextAttackIndex + 1) % 3;
        return nextState;
    }

    public void finishCurrentAttack() {
        setState(new IdleState(isPhaseTwo() ? 0.55f : 0.9f));
    }

    public void setState(BossState nextState) {
        if (currentState != null) {
            currentState.exit(this);
        }

        currentState = nextState;
        currentState.enter(this);
    }

    public boolean isPhaseTwo() {
        return health <= maxHealth * 0.5f && health > 0;
    }

    public boolean isDefeated() {
        return health <= 0;
    }

    public float getCenterX() {
        return x + Constants.BOSS_WIDTH * 0.5f;
    }

    public float getCenterY() {
        return y + Constants.BOSS_HEIGHT * 0.5f;
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

    public String getStateName() {
        return currentState.getName();
    }

    public boolean isTelegraphing() {
        return telegraphTimer > 0f;
    }

    public float getTelegraphAlpha() {
        if (telegraphTimer <= 0f) {
            return 0f;
        }
        return MathUtils.clamp(telegraphTimer / telegraphDuration, 0f, 1f);
    }

    private void renderFlower(ShapeRenderer shapeRenderer, float timer, boolean battlePose) {
        boolean transformed = battlePose || timer > 1.08f;
        float reveal = battlePose ? 1f : MathUtils.clamp(timer / 0.85f, 0f, 1f);
        float roar = battlePose ? 0f : MathUtils.clamp((timer - 1.08f) / 0.42f, 0f, 1f);
        float motion = getAttackMotion();
        float idleWave = MathUtils.sin((timer + (battlePose ? 3f : 0f)) * 4.4f) * 9f;
        float squash = 1f + motion * 0.14f + (attackSquashTimer > 0f ? 0.08f : 0f) + roar * 0.18f;
        float centerX = getCenterX() - motion * 28f;
        float baseY = y;
        float flowerY = baseY + Constants.BOSS_HEIGHT - 38f + idleWave * 0.45f + motion * 10f;

        if (!battlePose) {
            centerX += (1f - reveal) * 120f;
        }

        renderTelegraph(shapeRenderer, centerX, flowerY);
        renderStemAndLeaves(shapeRenderer, centerX, baseY, idleWave, transformed, squash, motion);
        renderArms(shapeRenderer, centerX, baseY, idleWave, transformed, motion);
        renderPetals(shapeRenderer, centerX, flowerY, transformed, roar, squash, motion, timer);
        renderFace(shapeRenderer, centerX, flowerY, transformed, roar, motion);
    }

    private void renderTelegraph(ShapeRenderer shapeRenderer, float centerX, float flowerY) {
        if (!isTelegraphing()) {
            return;
        }

        float alpha = getTelegraphAlpha();
        float radius = 78f + (1f - alpha) * 34f;
        shapeRenderer.setColor(telegraphColor.r, telegraphColor.g, telegraphColor.b, 0.32f);
        shapeRenderer.circle(centerX, flowerY, radius);
        shapeRenderer.setColor(1f, 0.9f, 0.28f, 0.65f);
        shapeRenderer.circle(centerX, flowerY, radius * 0.72f);
    }

    private void renderStemAndLeaves(ShapeRenderer shapeRenderer, float centerX, float baseY, float idleWave,
                                     boolean transformed, float squash, float motion) {
        shapeRenderer.setColor(0.04f, 0.16f, 0.08f, 1f);
        shapeRenderer.rect(centerX - 23f * squash, baseY + 24f, 46f * squash, Constants.BOSS_HEIGHT - 78f);
        shapeRenderer.setColor(transformed ? new Color(0.08f, 0.38f, 0.16f, 1f) : new Color(0.16f, 0.58f, 0.24f, 1f));
        shapeRenderer.rect(centerX - 16f * squash, baseY + 28f, 32f * squash, Constants.BOSS_HEIGHT - 86f);

        shapeRenderer.setColor(0.04f, 0.18f, 0.08f, 1f);
        drawRotatedEllipse(shapeRenderer, centerX - 78f, baseY + 93f + idleWave * 0.45f, 112f, 42f, -18f - motion * 14f);
        drawRotatedEllipse(shapeRenderer, centerX + 74f, baseY + 112f - idleWave * 0.35f, 112f, 42f, 18f + motion * 12f);
        shapeRenderer.setColor(0.12f, 0.5f, 0.2f, 1f);
        drawRotatedEllipse(shapeRenderer, centerX - 80f, baseY + 95f + idleWave * 0.45f, 96f, 34f, -18f - motion * 14f);
        drawRotatedEllipse(shapeRenderer, centerX + 72f, baseY + 114f - idleWave * 0.35f, 96f, 34f, 18f + motion * 12f);
    }

    private void renderPetals(ShapeRenderer shapeRenderer, float centerX, float flowerY, boolean transformed,
                              float roar, float squash, float motion, float timer) {
        Color petalColor = hurtFlashTimer > 0f
                ? Color.WHITE
                : (isPhaseTwo() || transformed
                ? new Color(0.95f, 0.28f, 0.12f, 1f)
                : new Color(1f, 0.58f, 0.2f, 1f));

        int petals = transformed ? 8 : 6;
        float orbit = transformed ? 56f + roar * 12f + motion * 5f : 43f;
        float petalWidth = transformed ? 66f * squash : 58f;
        float petalHeight = transformed ? 42f / squash : 38f;
        float sway = MathUtils.sin(timer * 3.2f) * 5f + motion * 12f;
        for (int i = 0; i < petals; i++) {
            float angle = MathUtils.PI2 * i / petals + (transformed ? 0.22f : 0f) + MathUtils.sin(timer * 2.1f) * 0.04f;
            float px = centerX + MathUtils.cos(angle) * orbit;
            float py = flowerY + MathUtils.sin(angle) * orbit;
            float degrees = angle * MathUtils.radiansToDegrees + sway;
            shapeRenderer.setColor(0.28f, 0.06f, 0.05f, 1f);
            drawRotatedEllipse(shapeRenderer, px, py, petalWidth + 10f, petalHeight + 8f, degrees);
            shapeRenderer.setColor(petalColor);
            drawRotatedEllipse(shapeRenderer, px, py, petalWidth, petalHeight, degrees);
            shapeRenderer.setColor(1f, 0.58f, 0.16f, hurtFlashTimer > 0f ? 0.15f : 0.35f);
            drawRotatedEllipse(shapeRenderer, px - MathUtils.cos(angle) * 5f, py + MathUtils.sin(angle) * 4f,
                    petalWidth * 0.48f, petalHeight * 0.32f, degrees);
        }

        shapeRenderer.setColor(0.25f, 0.05f, 0.04f, 1f);
        shapeRenderer.circle(centerX, flowerY, transformed ? 57f * squash : 49f);
        shapeRenderer.setColor(transformed ? new Color(0.58f, 0.08f, 0.08f, 1f) : new Color(0.84f, 0.28f, 0.08f, 1f));
        shapeRenderer.circle(centerX, flowerY, transformed ? 50f * squash : 43f);
    }

    private void renderFace(ShapeRenderer shapeRenderer, float centerX, float flowerY, boolean transformed, float roar, float motion) {
        if (transformed) {
            shapeRenderer.setColor(0.98f, 0.92f, 0.72f, 1f);
            shapeRenderer.circle(centerX - 19f - motion * 2f, flowerY + 12f + motion * 3f, 11f + roar * 4f);
            shapeRenderer.circle(centerX + 19f - motion * 2f, flowerY + 12f + motion * 3f, 11f + roar * 4f);
            shapeRenderer.setColor(0.08f, 0.03f, 0.02f, 1f);
            shapeRenderer.circle(centerX - 17f - motion * 4f, flowerY + 9f, 4.5f + roar * 2f);
            shapeRenderer.circle(centerX + 17f - motion * 4f, flowerY + 9f, 4.5f + roar * 2f);
            shapeRenderer.ellipse(centerX - 29f - motion * 3f, flowerY - 30f - roar * 8f, 58f, 25f + roar * 30f);

            shapeRenderer.setColor(Color.WHITE);
            for (int i = 0; i < 5; i++) {
                float toothX = centerX - 23f + i * 11f;
                shapeRenderer.triangle(toothX, flowerY - 13f, toothX + 7f, flowerY - 13f, toothX + 3.5f, flowerY - 23f);
            }
        } else {
            shapeRenderer.setColor(0.98f, 0.92f, 0.72f, 1f);
            shapeRenderer.circle(centerX - 14f, flowerY + 8f, 7f);
            shapeRenderer.circle(centerX + 14f, flowerY + 8f, 7f);
            shapeRenderer.setColor(0.09f, 0.04f, 0.03f, 1f);
            shapeRenderer.circle(centerX - 13f, flowerY + 8f, 3f);
            shapeRenderer.circle(centerX + 13f, flowerY + 8f, 3f);
            shapeRenderer.rect(centerX - 18f, flowerY - 18f, 36f, 4f);
        }
    }

    private void renderArms(ShapeRenderer shapeRenderer, float centerX, float baseY, float idleWave,
                            boolean transformed, float motion) {
        if (!transformed) {
            return;
        }

        float leftHandX = centerX - 132f - motion * 12f;
        float leftHandY = baseY + 168f + idleWave * 0.35f + motion * 20f;
        float rightHandX = centerX + 126f - motion * 18f;
        float rightHandY = baseY + 180f - idleWave * 0.25f - motion * 8f;

        shapeRenderer.setColor(0.05f, 0.19f, 0.08f, 1f);
        shapeRenderer.rectLine(centerX - 18f, baseY + 158f, leftHandX + 12f, leftHandY - 10f, 15f);
        shapeRenderer.rectLine(centerX + 18f, baseY + 160f, rightHandX - 12f, rightHandY - 10f, 15f);
        shapeRenderer.setColor(0.1f, 0.42f, 0.17f, 1f);
        shapeRenderer.rectLine(centerX - 18f, baseY + 158f, leftHandX + 12f, leftHandY - 10f, 9f);
        shapeRenderer.rectLine(centerX + 18f, baseY + 160f, rightHandX - 12f, rightHandY - 10f, 9f);

        renderLeafHand(shapeRenderer, leftHandX, leftHandY, -18f - motion * 16f);
        renderLeafHand(shapeRenderer, rightHandX, rightHandY, 20f + motion * 8f);
    }

    private void renderLeafHand(ShapeRenderer shapeRenderer, float x, float y, float rotation) {
        shapeRenderer.setColor(0.04f, 0.17f, 0.08f, 1f);
        drawRotatedEllipse(shapeRenderer, x, y, 58f, 36f, rotation);
        shapeRenderer.setColor(0.16f, 0.54f, 0.2f, 1f);
        drawRotatedEllipse(shapeRenderer, x, y, 48f, 27f, rotation);
        shapeRenderer.setColor(0.66f, 0.88f, 0.32f, 1f);
        shapeRenderer.rectLine(x - 19f, y, x + 19f, y, 3f);
    }

    private float getAttackMotion() {
        if (attackMotionTimer <= 0f) {
            return 0f;
        }

        float progress = 1f - attackMotionTimer / attackMotionDuration;
        return MathUtils.sin(progress * MathUtils.PI) * attackMotionStrength;
    }

    private void drawRotatedEllipse(ShapeRenderer shapeRenderer, float x, float y, float width, float height, float degrees) {
        shapeRenderer.identity();
        shapeRenderer.translate(x, y, 0f);
        shapeRenderer.rotate(0f, 0f, 1f, degrees);
        shapeRenderer.ellipse(-width * 0.5f, -height * 0.5f, width, height);
        shapeRenderer.identity();
    }
}
