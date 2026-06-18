package com.bossfight.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bossfight.boss.AttackFiveState;
import com.bossfight.boss.AttackFourState;
import com.bossfight.boss.AttackOneState;
import com.bossfight.boss.AttackThreeState;
import com.bossfight.boss.AttackTwoState;
import com.bossfight.boss.BossState;
import com.bossfight.boss.BossSoundEvent;
import com.bossfight.boss.BossVisualState;
import com.bossfight.boss.DefeatedState;
import com.bossfight.boss.IdleState;
import com.bossfight.boss.PhaseTwoTransitionState;
import com.bossfight.systems.ProjectileSystem;
import com.bossfight.Constants;

import java.util.ArrayDeque;

public class Boss {
    private final Hitbox hitbox;
    private final ArrayDeque<BossSoundEvent> soundEvents = new ArrayDeque<>();
    private final int maxHealth;
    private BossState currentState;
    private int health;
    private int lastAttackIndex = -1;
    private int phaseTwoAttackCount;
    private float x;
    private float y;
    private float telegraphTimer;
    private float telegraphDuration = 1f;
    private boolean phaseTwoTransitionPlayed;
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
        telegraphTimer = Math.max(0f, telegraphTimer - delta);

        if (health <= 0 && !(currentState instanceof DefeatedState)) {
            setState(new DefeatedState());
        }

        if (isPhaseTwo()
                && !phaseTwoTransitionPlayed
                && !(currentState instanceof PhaseTwoTransitionState)
                && !(currentState instanceof DefeatedState)) {
            phaseTwoTransitionPlayed = true;
            setState(new PhaseTwoTransitionState());
        }

        currentState.update(this, delta, projectileSystem, player);
        hitbox.setPosition(x, y);
    }

    public boolean takeDamage(int amount) {
        if (isDefeated() || isInvulnerable()) {
            return false;
        }

        health = MathUtils.clamp(health - amount, 0, maxHealth);
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

    public BossState createNextAttackState() {
        int attackCount = isPhaseTwo() ? 5 : 3;
        int nextAttackIndex;

        if (isPhaseTwo() && phaseTwoAttackCount % 5 == 4 && lastAttackIndex != 4) {
            nextAttackIndex = 4;
        } else if (isPhaseTwo() && phaseTwoAttackCount % 4 == 3 && lastAttackIndex != 3) {
            nextAttackIndex = 3;
        } else {
            do {
                nextAttackIndex = MathUtils.random(attackCount - 1);
            } while (attackCount > 1 && nextAttackIndex == lastAttackIndex);
        }

        lastAttackIndex = nextAttackIndex;
        phaseTwoAttackCount++;

        if (nextAttackIndex == 0) {
            return new AttackOneState();
        } else if (nextAttackIndex == 1) {
            return new AttackTwoState();
        } else if (nextAttackIndex == 2) {
            return new AttackThreeState();
        } else if (nextAttackIndex == 3) {
            return new AttackFourState();
        }

        return new AttackFiveState();
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

    public boolean isInvulnerable() {
        return currentState instanceof PhaseTwoTransitionState;
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

    public BossVisualState getVisualState() {
        return currentState.getVisualState();
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
}
